package tech.robd.shellguard.bus.workflow.stores
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/stores/WorkflowStoreFactory.kt
 * description: Factory for creating WorkflowStore instances with Spring-managed dependencies.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 *
 */
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
/**
 * Factory class for creating new [WorkflowStore] instances, each wired with the shared [WorkflowEventBus].
 *
 * This is used by [WorkflowSessionManager] to ensure each session receives its own independent workflow store
 * with all required dependencies injected by Spring.
 *
 * Usage:
 *   val store = workflowStoreFactory.create()
 *   // Use the store for managing per-session workflows.
 *
 * This pattern ensures that all [WorkflowStore] instances publish events to the global [WorkflowEventBus],
 * allowing for centralized event handling, logging, and dashboard updates.
 */
@Component
class WorkflowStoreFactory @Autowired constructor(
    private val eventBus: WorkflowEventBus
) {
    fun create(): WorkflowStore = WorkflowStore(eventBus)
}
