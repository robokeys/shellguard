package tech.robd.shellguard.engine

/**
 * [File Info]
 * path: tech/robd/shellguard/engine/SingleActionWorkflowEngine.kt
 * description: Stage 1 event-driven workflow engine for RKCL/shellguard actions. Handles submit, approve, reject, execute, complete, and output events.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.TerminalOutput
import tech.robd.shellguard.bus.core.risk.RiskAssessor
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import tech.robd.shellguard.bus.workflow.stores.WorkflowSessionManager
import tech.robd.shellguard.bus.workflow.stores.WorkflowStore
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [🧩 Point: Stage 1 workflow engine]
 * Event-driven workflow engine for AI actions, using the Stage 1 in-memory event bus.
 * Handles the full lifecycle: submit → risk assess → approval (if needed) → execute → complete/fail/output.
 * Extend or replace for Stage 2+ engines with distributed/stateful event bus.
 */
class SingleActionWorkflowEngine(
    private val eventBus: WorkflowEventBus,  // Correct type from Stage 1
    private val riskAssessor: RiskAssessor,
    private val workflowSessionManager: WorkflowSessionManager
) : WorkflowEngine {

    private val logger = KotlinLogging.logger {}

    // Add the required engineType property
    override val engineType: String = "single"

    // private val workflows = ConcurrentHashMap<String, ActionWorkflow>()
    private val workflowStore: WorkflowStore = workflowSessionManager.getStore("DEFAULT_STORE")

    /**
     * [📌 Point: Submit new AI action]
     * Submits a new action to the workflow, risk assesses, and routes to approval or execution.
     */
    @OptIn(ExperimentalTime::class)
    override fun submitAction(command: CommandMessage): ActionWorkflow {
        val workflow = ActionWorkflow(command.uuid, command)
        workflowStore.add(workflow)

        // Phase 1: SUBMITTED
        val submittedEvent = BusEvent(
            phase = CommandEventPhase.SUBMITTED,
            command = command
        )
        workflow.addEvent(submittedEvent)
        eventBus.publish(submittedEvent)

        // Phase 2: RISK_ASSESSED
        val riskAssessmentLevel = command.riskAssessmentLevel ?: riskAssessor.assessRisk(command)
        val requiresApproval = riskAssessor.requiresApproval(riskAssessmentLevel)

        val enhancedAction =
            command.copy(riskAssessmentLevel = riskAssessmentLevel, requiresApproval = requiresApproval)
        workflow.action = enhancedAction

        val riskAssessedEvent = BusEvent(
            phase = CommandEventPhase.RISK_ASSESSED,
            command = enhancedAction,
            riskAssessmentLevel = riskAssessmentLevel,
            requiresApproval = requiresApproval
        )
        workflow.addEvent(riskAssessedEvent)
        eventBus.publish(riskAssessedEvent)

        // Phase 3: Determine next step
        if (requiresApproval) {
            transitionToPendingApproval(workflow)
        } else {
            transitionToApproved(workflow)
            processQueue()
            //transitionToExecutionStarted(workflow)
        }

        return workflow
    }

    /**
     * [📌 Point: Approve AI action]
     * Approves a pending action and transitions to execution.
     */
    override fun approveAction(actionId: String, approvedBy: String): Boolean {
        val workflow = workflowStore.getById(actionId) ?: return false

        if (workflow.currentPhase != CommandEventPhase.PENDING_APPROVAL) {
            return false
        }

        val approvedEvent = BusEvent(
            phase = CommandEventPhase.APPROVED,
            command = workflow.action,
            approvedBy = approvedBy
        )
        workflow.addEvent(approvedEvent)
        eventBus.publish(approvedEvent)
        processQueue()
        return true
    }

    /**
     * Reject a pending AI action
     */
    override fun rejectAction(actionId: String, rejectedBy: String, reason: String): Boolean {
        val workflow = workflowStore.getById(actionId) ?: return false

        if (workflow.currentPhase != CommandEventPhase.PENDING_APPROVAL) {
            return false
        }

        val rejectedEvent = BusEvent(
            phase = CommandEventPhase.REJECTED,
            command = workflow.action,
            rejectedBy = rejectedBy,
            rejectionReason = reason
        )
        workflow.addEvent(rejectedEvent)
        eventBus.publish(rejectedEvent)
        processQueue()

        return true
    }

    /**
     * [📌 Point: Complete AI action]
     * Completes an action with the given result, publishes event, and logs.
     */
    @OptIn(ExperimentalTime::class)
    override fun completeAction(actionId: String, result: CommandResult) {
        val workflow = workflowStore.getById(actionId) ?: return
        logger.info("[WorkflowEngine] Found workflow for completion: actionId=$actionId")
        // ✅ ADD: Before completion logging
        logger.debug(
            "[WorkflowEngine] BEFORE completion: actionId={}, currentPhase={}, isCompleted={}",
            actionId,
            workflow.currentPhase,
            workflow.isCompleted()
        )

        val completedEvent = BusEvent(
            phase = CommandEventPhase.COMPLETED,
            command = workflow.action,
            result = result
        )
        workflow.addEvent(completedEvent)
        logger.info("[WorkflowEngine] Event added: actionId=$actionId, currentPhase=${workflow.currentPhase}}")
        eventBus.publish(completedEvent)
        logger.debug("[WorkflowEngine] Total workflows in memory (Before Removal): ${workflowStore.getAll().size}")
        workflowStore.removeCompletedWorkflow(actionId)
        // ✅ ADD: After completion logging
        logger.info("[WorkflowEngine] AFTER completion: actionId=$actionId, currentPhase=${workflow.currentPhase}, isCompleted=${workflow.isCompleted()}")
        logger.info("[WorkflowEngine] Workflow completedAt: ${workflow.completedAt}")
        logger.info("[WorkflowEngine] Total workflows in memory: ${workflowStore.getAll().size}")

        processQueue()
    }

    /**
     * [📌 Point: Fail AI action]
     * Fails an action, publishes event, and records error details.
     */
    override fun failAction(actionId: String, error: String) {
        val workflow = workflowStore.getById(actionId) ?: return
        logger.info("[WorkflowEngine] Found workflow for failure: actionId=$actionId")
        val errorResult = CommandResult(
            uuid = actionId,
            sessionId = workflow.action.sessionId,
            success = false,
            message = error
        )
        val failedEvent = BusEvent(
            phase = CommandEventPhase.FAILED,
            command = workflow.action,
            result = errorResult
        )
        workflow.addEvent(failedEvent)
        logger.debug(
            "[WorkflowEngine] Preparing to publish: actionId={}, currentPhase={}}",
            actionId,
            workflow.currentPhase
        )
        eventBus.publish(failedEvent)
        logger.info("[WorkflowEngine] Event added: actionId=$actionId, currentPhase=${workflow.currentPhase}}")
        processQueue()
    }

    /**
     * Emit terminal output for an AI action
     */
    override fun emitOutput(actionId: String, output: TerminalOutput) {
        val workflow = workflowStore.getById(actionId) ?: return

        val outputEvent = BusEvent(
            phase = CommandEventPhase.OUTPUT,
            command = workflow.action,
            output = output
        )
        workflow.addEvent(outputEvent)
        eventBus.publish(outputEvent)
    }

    // [🧩 Section: Internal state transitions]
    private fun transitionToPendingApproval(workflow: ActionWorkflow) {
        val pendingEvent = BusEvent(
            phase = CommandEventPhase.PENDING_APPROVAL,
            command = workflow.action
        )
        workflow.addEvent(pendingEvent)
        eventBus.publish(pendingEvent)
    }

    /**
     *     Public API method -- calls transitionToExecutionStarted
     */
    fun markExecutionStarted(actionId: String): Boolean {
        val workflow = workflowStore.getById(actionId) ?: return false
        transitionToExecutionStarted(workflow) // calls private method
        return true
    }

    private fun transitionToExecutionStarted(workflow: ActionWorkflow) {
        val executionEvent = BusEvent(
            phase = CommandEventPhase.EXECUTION_STARTED,
            command = workflow.action
        )
        workflow.addEvent(executionEvent)
        eventBus.publish(executionEvent)
    }


    private fun transitionToApproved(workflow: ActionWorkflow) {
        val approvedEvent = BusEvent(
            phase = CommandEventPhase.APPROVED,
            command = workflow.action,
            approvedBy = "SYSTEM_AUTO"
        )
        workflow.addEvent(approvedEvent)
        eventBus.publish(approvedEvent)
    }

    /**
     * Approve an action out of order, bypassing queue blocking.
     * This allows emergency execution of commands even when earlier commands are pending approval.
     *
     * @param actionId The workflow to approve out of order
     * @param approvedBy Who is approving this action
     * @param reason Why this needs out-of-order execution
     * @return true if successfully approved out of order, false if not found or invalid state
     */
    /**
     * Approve an action out of order, bypassing queue blocking.
     * This allows emergency execution of commands even when earlier commands are pending approval.
     *
     * @param actionId The workflow to approve out of order
     * @param approvedBy Who is approving this action
     * @param reason Why this needs out-of-order execution
     * @return true if successfully approved out of order, false if not found or invalid state
     */
    fun approveActionOutOfOrder(actionId: String, approvedBy: String, reason: String): Boolean {
        val workflow = workflowStore.getById(actionId) ?: return false

        // Only allow out-of-order approval for pending workflows
        if (workflow.currentPhase != CommandEventPhase.PENDING_APPROVAL) {
            logger.warn { "[OutOfOrder] Cannot approve out of order - workflow $actionId is in phase ${workflow.currentPhase}, not PENDING_APPROVAL" }
            return false
        }

        logger.info { "[OutOfOrder] Approving workflow $actionId out of order by $approvedBy: $reason" }

        // Transition to IMMEDIATE_EXECUTE_APPROVAL phase
        val immediateApprovalEvent = BusEvent(
            phase = CommandEventPhase.IMMEDIATE_EXECUTE_APPROVAL,
            command = workflow.action,
            approvedBy = "$approvedBy (OUT_OF_ORDER: $reason)"
        )

        workflow.addEvent(immediateApprovalEvent)
        eventBus.publish(immediateApprovalEvent)

        // Immediately try to process this workflow
        processQueue()

        return true
    }

    // [/🧩 Section: Internal state transitions]

    /**
     * Processes the workflow queue to execute ready commands.
     * The READY_TO_RUN event will be emitted by WorkflowStore.popNextActionable()
     * and will be handled by ReadyToRunListener.
     */
    fun processQueue() {
        // [🧩 Point: workflow-queue-processing]
        // Pop the next actionable workflow from the queue. This operation will:
        // 1. Find the first workflow that's ready to execute (not blocked by pending approvals)
        // 2. Emit a READY_TO_RUN event for that workflow
        // 3. Remove the workflow from the execution queue
        // 4. Return the workflow object for logging/tracking
        // The ReadyToRunListener will catch the READY_TO_RUN event and trigger actual execution.
        val nextWorkflow = workflowStore.popNextActionable()
        if (nextWorkflow != null) {
            logger.info("[Queue] Popped next actionable workflow for execution: ${nextWorkflow.actionId} (command: '${nextWorkflow.action.command}', phase: ${nextWorkflow.currentPhase})")
            logger.info("[Queue] READY_TO_RUN event emitted, ReadyToRunListener will handle execution")
            // The READY_TO_RUN event has been emitted by popNextActionable()
            // The ReadyToRunListener will handle it - we don't need to do anything else
        } else {
            logger.debug("[Queue] No actionable workflows found")
        }
    }

    // [🧩 Section: Query methods and statistics]
    override fun getWorkflow(actionId: String): ActionWorkflow? = workflowStore.getById(actionId)

    fun getWorkflowsByPhase(phase: CommandEventPhase): List<ActionWorkflow> {
        return workflowStore.getAll().filter { it.currentPhase == phase }
    }

    override fun getPendingApprovals(): List<ActionWorkflow> {
        return getWorkflowsByPhase(CommandEventPhase.PENDING_APPROVAL)
    }

    fun getActiveWorkflows(): List<ActionWorkflow> {
        return workflowStore.getAll().filter { !it.isCompleted() }
    }

    override fun getAllWorkflows(): List<ActionWorkflow> = workflowStore.getAll().toList()

    fun getCompletedWorkflows(): List<ActionWorkflow> {
        return workflowStore.getAll().filter { it.isCompleted() }
    }

    @OptIn(ExperimentalTime::class)
    fun cleanupCompletedWorkflows(olderThan: Instant) {
        workflowStore.cleanupCompleted(olderThan)
    }

    // Statistics
    override fun getWorkflowStats(): WorkflowStats {
        val allWorkflows = workflowStore.getAll()
        return WorkflowStats(
            total = allWorkflows.size,
            active = allWorkflows.count { !it.isCompleted() },
            completed = allWorkflows.count { it.isCompleted() },
            pendingApproval = allWorkflows.count { it.currentPhase == CommandEventPhase.PENDING_APPROVAL },
            approved = allWorkflows.count { it.getApprover() != null },
            rejected = allWorkflows.count { it.getRejector() != null },
            failed = allWorkflows.count { it.currentPhase == CommandEventPhase.FAILED },
            averageDurationMs = allWorkflows.mapNotNull { it.getDurationMs() }.average().let {
                if (it.isNaN()) 0.0 else it
            }
        )
    }
    // [/🧩 Section: Query methods and statistics]
}