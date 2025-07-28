package tech.robd.shellguard.bus.workflow.stores
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/stores/WorkflowSessionManager.kt
 * description: Manages per-session WorkflowStore instances for workflow command handling.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component

/**
 * WorkflowSessionManager is responsible for managing the lifecycle of per-session [WorkflowStore] instances.
 *
 * Each session (identified by a unique session ID) is assigned its own WorkflowStore,
 * created via [WorkflowStoreFactory] to ensure proper dependency injection (e.g., access to the global [WorkflowEventBus]).
 *
 * Typical usage:
 *   - On session start: call [getStore] with the session ID to allocate or retrieve a WorkflowStore for the session.
 *   - On session end: call [removeStore] to clean up session-specific resources.
 *
 * All stores share the same [WorkflowEventBus] for centralized event handling.
 * This class is a Spring-managed singleton, thread-safe via ConcurrentHashMap.
 */
@Component
class WorkflowSessionManager(
    private val workflowStoreFactory: WorkflowStoreFactory // see below
) {
    private val sessionStores = ConcurrentHashMap<String, WorkflowStore>()

    /**
     * Retrieves the WorkflowStore associated with [sessionId], or creates a new one if not present.
     * The WorkflowStore is created via [workflowStoreFactory] to ensure dependencies are injected.
     */
    fun getStore(sessionId: String): WorkflowStore =
        sessionStores.computeIfAbsent(sessionId) { workflowStoreFactory.create() }

    /**
     * Removes and discards the WorkflowStore for [sessionId].
     * Call this when a session ends to free resources.
     */
    fun removeStore(sessionId: String) {
        sessionStores.remove(sessionId)
    }
}
