package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/BusEventTest.kt
 * description: Unit tests for BusEvent model—verifies event creation with various phases and approval/rejection data, and toString output.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import tech.robd.shellguard.bus.core.CommandMessage

class BusEventTest {

    @Test
    fun `can create basic bus event`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo hello"
        )

        val event = BusEvent(CommandEventPhase.SUBMITTED, command)

        assertEquals(CommandEventPhase.SUBMITTED, event.phase)
        assertEquals("echo hello", event.command.command)
        assertNotNull(event.timestamp)
    }

    @Test
    fun `can create event with approval data`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "dangerous command"
        )

        val event = BusEvent(
            phase = CommandEventPhase.APPROVED,
            command = command,
            approvedBy = "admin",
            requiresApproval = true
        )

        assertEquals(CommandEventPhase.APPROVED, event.phase)
        assertEquals("admin", event.approvedBy)
        assertEquals(true, event.requiresApproval)
    }

    @Test
    fun `can create event with rejection data`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "bad command"
        )

        val event = BusEvent(
            phase = CommandEventPhase.REJECTED,
            command = command,
            rejectedBy = "security",
            rejectionReason = "Too dangerous"
        )

        assertEquals(CommandEventPhase.REJECTED, event.phase)
        assertEquals("security", event.rejectedBy)
        assertEquals("Too dangerous", event.rejectionReason)
    }

    @Test
    fun `toString includes basic info`() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "test-session",
            command = "ls"
        )

        val event = BusEvent(CommandEventPhase.COMPLETED, command)
        val result = event.toString()

        assertTrue(result.contains("COMPLETED"))
        assertTrue(result.contains("ls"))
        assertTrue(result.contains("test-session"))
    }
}