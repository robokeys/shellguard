package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/EventBusTestListenerTest.kt
 * description: Unit tests for EventBusTestListener—verifies event tracking, clear logic, and order handling for bus events.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import tech.robd.shellguard.bus.core.CommandMessage

class EventBusTestListenerTest {

    private lateinit var listener: EventBusTestListener

    @BeforeEach
    fun setUp() {
        listener = EventBusTestListener()
    }

    @Test
    fun `starts with no events`() {
        assertEquals(0, listener.getEventCount())
        assertTrue(listener.getReceivedEvents().isEmpty())
    }

    @Test
    fun `can receive and track events`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )
        val event = BusEvent(CommandEventPhase.SUBMITTED, command)

        listener.onEvent(event)

        assertEquals(1, listener.getEventCount())
        assertEquals(event, listener.getReceivedEvents()[0])
    }

    @Test
    fun `can clear events`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )
        val event = BusEvent(CommandEventPhase.SUBMITTED, command)

        listener.onEvent(event)
        assertEquals(1, listener.getEventCount())

        listener.clearEvents()
        assertEquals(0, listener.getEventCount())
        assertTrue(listener.getReceivedEvents().isEmpty())
    }

    @Test
    fun `can handle multiple events`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )

        listener.onEvent(BusEvent(CommandEventPhase.SUBMITTED, command))
        listener.onEvent(BusEvent(CommandEventPhase.APPROVED, command))
        listener.onEvent(BusEvent(CommandEventPhase.COMPLETED, command))

        assertEquals(3, listener.getEventCount())
        val events = listener.getReceivedEvents()
        assertEquals(CommandEventPhase.SUBMITTED, events[0].phase)
        assertEquals(CommandEventPhase.APPROVED, events[1].phase)
        assertEquals(CommandEventPhase.COMPLETED, events[2].phase)
    }
}