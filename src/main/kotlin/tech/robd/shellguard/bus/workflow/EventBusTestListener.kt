package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/EventBusTestListener.kt
 * description: Test-only component for verifying event flow on the workflow event bus.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */

// [🧩 Region: Testing Only: DO NOT USE IN PRODUCTION]
/**
 * Simple listener for development/test environments to verify that workflow events are emitted and received.
 *
 * Not for use in production. Intended for local development, debugging, and test harnesses.
 */
class EventBusTestListener {
    private val receivedEvents = mutableListOf<BusEvent>()

    fun onEvent(event: BusEvent) {
        receivedEvents.add(event)
        println("[TestListener] Received: $event")
    }

    fun getReceivedEvents(): List<BusEvent> = receivedEvents.toList()
    fun clearEvents() = receivedEvents.clear()
    fun getEventCount(): Int = receivedEvents.size
}
// [/🧩 Region: Testing Only: DO NOT USE IN PRODUCTION]