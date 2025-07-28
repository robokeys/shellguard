package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/InMemoryWorkflowEventBusTest.kt
 * description: Unit tests for InMemoryWorkflowEventBus—verifies event publishing, subscription (all/specific phase), and listener tracking.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import tech.robd.shellguard.bus.core.CommandMessage

class InMemoryWorkflowEventBusTest {

    private lateinit var eventBus: InMemoryWorkflowEventBus

    @BeforeEach
    fun setUp() {
        eventBus = InMemoryWorkflowEventBus()
    }

    @Test
    fun `can publish and receive events`() {
        var receivedEvent: BusEvent? = null

        // Subscribe to all events
        eventBus.subscribe { event -> receivedEvent = event }

        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )
        val event = BusEvent(CommandEventPhase.SUBMITTED, command)

        eventBus.publish(event)

        assertNotNull(receivedEvent)
        assertEquals(event.phase, receivedEvent!!.phase)
        assertEquals(event.command.command, receivedEvent!!.command.command)
    }

    @Test
    fun `can subscribe to specific phases`() {
        var submittedEvent: BusEvent? = null
        var approvedEvent: BusEvent? = null

        // Subscribe to specific phases
        eventBus.subscribe(CommandEventPhase.SUBMITTED) { event -> submittedEvent = event }
        eventBus.subscribe(CommandEventPhase.APPROVED) { event -> approvedEvent = event }

        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )

        // Publish submitted event
        eventBus.publish(BusEvent(CommandEventPhase.SUBMITTED, command))
        assertNotNull(submittedEvent)
        assertNull(approvedEvent)

        // Publish approved event
        eventBus.publish(BusEvent(CommandEventPhase.APPROVED, command))
        assertNotNull(approvedEvent)
    }

    @Test
    fun `tracks listener counts correctly`() {
        assertEquals(0, eventBus.getTotalListenerCount())

        eventBus.subscribe { }
        assertEquals(1, eventBus.getTotalListenerCount())

        eventBus.subscribe(CommandEventPhase.SUBMITTED) { }
        assertEquals(2, eventBus.getTotalListenerCount())
        assertEquals(1, eventBus.getListenerCount(CommandEventPhase.SUBMITTED))
        assertEquals(0, eventBus.getListenerCount(CommandEventPhase.APPROVED))
    }

    @Test
    fun `handles multiple listeners for same phase`() {
        var listener1Called = false
        var listener2Called = false

        eventBus.subscribe(CommandEventPhase.SUBMITTED) { listener1Called = true }
        eventBus.subscribe(CommandEventPhase.SUBMITTED) { listener2Called = true }

        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo test"
        )
        eventBus.publish(BusEvent(CommandEventPhase.SUBMITTED, command))

        assertTrue(listener1Called)
        assertTrue(listener2Called)
    }
}