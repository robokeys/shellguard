package tech.robd.shellguard.bus.workflow.stores
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/stores/WorkflowStore.kt
 * description: Session-local store for managing ActionWorkflows and command queues, with stateful approval logic and event publication.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.engine.ActionWorkflow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

import org.springframework.stereotype.Component

import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import tech.robd.shellguard.bus.workflow.BusEvent
/*
 * [📌 Note: Overview]
 * WorkflowStore is a per-session component that manages the lifecycle of workflow commands (ActionWorkflow)
 * for a single session, connection, or user. It provides:
 *   - Ordered queuing of workflow actions (using ConcurrentLinkedQueue for IDs)
 *   - Fast lookup by ID (using ConcurrentHashMap)
 *   - Fine-grained state management: submission, approval, execution, completion, cleanup
 *   - Integration with a global WorkflowEventBus for event-driven UI/log updates
 *   - Support for both strict in-order and out-of-order execution via IMMEDIATE_EXECUTE_APPROVAL
 *
 * [📌 Note: Key Concepts]
 * - Only the next actionable workflow (not blocked) is eligible for execution, unless one is manually
 *   escalated via IMMEDIATE_EXECUTE_APPROVAL.
 * - Pop/removal always keeps the map and queue in sync.
 * - Periodic cleanup removes old/completed workflows from both structures.
 * - All commands/events are session-scoped; no state is shared between sessions.
 *
 * [📌 Note: Typical Usage]
 * val store = sessionManager.getStore(sessionId)
 * store.add(workflow)                // Queue new workflow for approval/execution
 * val next = store.popNextActionable() // Pop the next actionable workflow for processing
 * store.cleanupCompleted(olderThan)  // Cleanup workflows older than a timestamp
 * store.getById(actionId)            // Lookup workflow by ID
 *
 * [📌 Note: Thread Safety]
 * - All methods are thread-safe via concurrent collections.
 * - popNextActionable() is synchronized for atomic pop-and-remove semantics.
 * - If only ever accessed from a single thread per session, can use standard (non-concurrent) collections for performance.
 *
 * [📌 Note: Extension Points]
 * - To support additional workflow states or custom event handling, modify CommandEventPhase and the event emission logic.
 * - To track more workflow metadata, extend ActionWorkflow or BusEvent as needed.
 *
 * [📌 Note: References]
 * - Used by [WorkflowSessionManager] to provide per-session state and queue isolation.
 * - Publishes to [WorkflowEventBus] for integration with dashboards, logs, or distributed systems.
 */
/**
 * Manages a session-local collection of [ActionWorkflow]s,
 * supporting queueing, fast lookup, approval workflow, and event-driven integration.
 *
 * @property workflowsById Fast ID-based lookup of workflows in this session.
 * @property workflowQueue Ordered queue of workflow IDs for processing.
 * @property eventBus Publishes workflow events to the global bus.
 */
@Component
class WorkflowStore(
    private val eventBus: WorkflowEventBus
) {


    companion object {
        private val logger = mu.KotlinLogging.logger {}
    }
    /**
     * Map of workflow IDs to workflow objects for this session.
     * Allows O(1) lookup of workflows by actionId.
     * An item could be in here but not in the queue
     */
    private val workflowsById = ConcurrentHashMap<String, ActionWorkflow>()

    /**
     * Queue of workflow IDs representing the order of submission/execution.
     * Used to enforce strict or in-order execution of workflows.
     */
    private val workflowQueue = ConcurrentLinkedQueue<String>()
    private val statusesWhichBlock: Set<CommandEventPhase> = setOf(CommandEventPhase.PENDING_APPROVAL)

    /**
     * Adds a new [ActionWorkflow] to the queue and ID map.
     *
     * @param workflow The workflow to add.
     */
    fun add(workflow: ActionWorkflow) {
        workflowsById[workflow.actionId] = workflow
        workflowQueue.add(workflow.actionId)
    }

    fun removeById(id: String) {
        workflowQueue.remove(id)
        workflowsById.remove(id)
    }

    /**
     * Removes all workflows that are completed and have a completion time before [olderThan].
     *
     * Removes from both the map and the queue to maintain consistency.
     *
     * @param olderThan Only workflows completed before this [Instant] will be removed.
     */
    @OptIn(ExperimentalTime::class)
    fun cleanupCompleted(olderThan: Instant) {
        // Gather IDs to remove
        val toRemove = workflowsById
            .filter { (_, workflow) ->
                workflow.isCompleted() && workflow.completedAt?.let { it < olderThan } == true
            }
            .map { it.key }
            .toSet() // For fast lookup

        // Remove from map
        toRemove.forEach { workflowsById.remove(it) }

        // Remove from queue
        workflowQueue.removeIf { it in toRemove }
    }

    /**
     * Remove completed workflows from BOTH store and queue
     * This handles both cases:
     * - Workflows that were popped from queue during execution
     * - Workflows that completed/failed before being popped (e.g., during risk assessment)
     */
    fun removeCompletedWorkflow(actionId: String) {
        val workflow = workflowsById[actionId]
        if (workflow?.isCompleted() == true) {
            workflowsById.remove(actionId)
            val removedFromQueue = workflowQueue.remove(actionId)  // Safe to call even if already removed
            logger.info {
                "Removed completed workflow $actionId from store" +
                        if (removedFromQueue) " and queue" else " (already removed from queue)"
            }
        } else {
            logger.debug { "Workflow $actionId not removed - either not found or not completed" }
        }
    }
    /**
     * Returns the workflow with the specified [actionId], or null if not found.
     *
     * @param actionId The ID of the workflow to retrieve.
     * @return The [ActionWorkflow], or null if absent.
     */
    fun getById(id: String): ActionWorkflow? = workflowsById[id]
    /**
     * Returns all workflows managed by this store.
     *
     * @return A list of all [ActionWorkflow]s.
     */
    fun getAll(): List<ActionWorkflow> = workflowsById.values.toList()

    /**
     * Atomically finds and removes the next actionable workflow.
     * NOtE: It MUST leave in store but remove from queue, to give the workflow time to be processed
     *
     * @return The next actionable [ActionWorkflow], or null if none found.
     */
    @Synchronized
    fun popNextActionable(): ActionWorkflow? {
        val next = nextActionable() ?: return null
        workflowQueue.remove(next.actionId)
        return next
    }
    /**
     * Atomically finds but leave in place the next actionable workflow.
     * NOte: It MUST  not remove the workflow, so if you run it multiple times you
     * could get the same workflow item returned
     *
     * @return The next actionable [ActionWorkflow], or null if none found.
     */
    fun nextActionable(): ActionWorkflow? {
        //  This stops at the FIRST blocked workflow and blocks everything after it
        for (id in workflowQueue) {
            val wf = workflowsById[id] ?: continue

            // If this workflow is blocking, stop here - nothing can execute
            if (wf.currentPhase in statusesWhichBlock) {
                logger.info { "Queue blocked by workflow ${wf.actionId} in phase ${wf.currentPhase}" }
                break  // Stop processing - queue is blocked
            }

            // If this workflow is ready to run, execute it
            if (wf.currentPhase !in statusesWhichBlock) {
                logger.info { "Command ${wf.actionId} is ready to execute" }
                if (wf.currentPhase != CommandEventPhase.READY_TO_RUN) {
                    emitReadyToRunEvent(wf)
                }
                return wf
            }
        }
        // 2. If nothing found, look for first IMMEDIATE_EXECUTE_APPROVAL
        for (id in workflowQueue) {
            val wf = workflowsById[id]
            if (wf != null && wf.currentPhase == CommandEventPhase.IMMEDIATE_EXECUTE_APPROVAL) {
                logger.warn { "Command ${wf.actionId} transitioned to READY_TO_RUN via IMMEDIATE_EXECUTE_APPROVAL" }
                if (wf.currentPhase != CommandEventPhase.READY_TO_RUN) {
                    emitReadyToRunEvent(wf)
                }
                return wf

            }
        }
        return null
    }

    fun all(): List<ActionWorkflow> = workflowQueue.mapNotNull { workflowsById[it] }

    private fun emitReadyToRunEvent(workflow: ActionWorkflow) {
        val readyEvent = BusEvent(
            phase = CommandEventPhase.READY_TO_RUN,
            command = workflow.action
        )
        workflow.addEvent(readyEvent)
        eventBus.publish(readyEvent)
    }

}
