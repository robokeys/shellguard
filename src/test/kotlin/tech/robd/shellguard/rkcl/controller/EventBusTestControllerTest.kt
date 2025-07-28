
package tech.robd.shellguard.rkcl.controller
/**
* [File Info]
* path: tech/robd/shellguard/rkcl/controller/EventBusTestControllerTest.kt
* description: Unit and contract tests for EventBusTestController—verifies event firing, error paths, event query, and clear logic with mocked WorkflowEventBus and EventBusTestListener.
* license: GPL-3.0
* editable: yes
* structured: no
* [/File Info]
*/
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import tech.robd.shellguard.bus.workflow.EventBusTestListener
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.core.CommandMessage

class EventBusTestControllerTest {

    private lateinit var controller: EventBusTestController
    private val eventBus: WorkflowEventBus = mockk()
    private val testListener: EventBusTestListener = mockk()

    @BeforeEach
    fun setUp() {
        controller = EventBusTestController(eventBus, testListener)
    }

    @Test
    fun `fire test event with valid phase works`() {
        // Given
        every { eventBus.publish(any()) } returns Unit

        // When
        val result = controller.fireTestEvent("SUBMITTED")

        // Then
        assertTrue(result.contains("Fired event"))
        verify { eventBus.publish(any()) }
    }

    @Test
    fun `fire test event with invalid phase returns error`() {
        // When
        val result = controller.fireTestEvent("INVALID")

        // Then
        assertTrue(result.contains("Invalid phase"))
    }

    @Test
    fun `get received events returns event data`() {
        // Given
        val event = BusEvent(
            CommandEventPhase.SUBMITTED,
            CommandMessage(origin = "test", sessionId = "session", command = "echo")
        )
        every { testListener.getEventCount() } returns 1
        every { testListener.getReceivedEvents() } returns listOf(event)

        // When
        val result = controller.getReceivedEvents()

        // Then
        assertEquals(1, result["count"])
        val events = result["events"] as List<*>
        assertEquals(1, events.size)
    }

    @Test
    fun `clear events calls listener clear`() {
        // Given
        every { testListener.clearEvents() } returns Unit

        // When
        val result = controller.clearEvents()

        // Then
        assertEquals("Cleared all received events", result)
        verify { testListener.clearEvents() }
    }
}