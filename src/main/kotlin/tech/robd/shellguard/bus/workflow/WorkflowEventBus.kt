package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/WorkflowEventBus.kt
 * description: Interface for the workflow event bus. Publishes and subscribes to BusEvent instances by phase or globally.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */

/**
 * publish/subscribe interface for workflow events (BusEvent).
 * Expand or extend for distributed bus, policy enforcement, or audit hooks.
 *
 * - `publish(event)`: Emit a workflow event.
 * - `subscribe(phase, listener)`: Listen for events at a specific phase.
 * - `subscribe(listener)`: Listen for all events.
 * - `unsubscribe(phase, listener)`: Remove a phase-based subscription.
 */
interface WorkflowEventBus {
    // [📌 Section: Workflow event bus API]
    fun publish(event: BusEvent)
    fun subscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit)
    fun subscribe(listener: (BusEvent) -> Unit) // Subscribe to all events
    fun unsubscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit)
    // [/📌 Section: Workflow event bus API]
}