package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/ActionWorkflowTest.kt
 * description: Unit tests for ActionWorkflow—verifies event addition, phase transitions, completion, and data extraction (risk, approver, duration).
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ActionWorkflowTest {

    private lateinit var workflow: ActionWorkflow

    @BeforeEach
    fun setUp() {
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo hello"
        )
        workflow = ActionWorkflow("test-123", command)
    }

    @Test
    fun `workflow starts with correct initial state`() {
        assertEquals("test-123", workflow.actionId)
        assertEquals("echo hello", workflow.action.command)
        assertEquals(CommandEventPhase.SUBMITTED, workflow.currentPhase)
        assertFalse(workflow.isCompleted())
        assertTrue(workflow.events.isEmpty())
    }

    @Test
    fun `adding event updates current phase`() {
        val event = BusEvent(CommandEventPhase.APPROVED, workflow.action)

        workflow.addEvent(event)

        assertEquals(CommandEventPhase.APPROVED, workflow.currentPhase)
        assertEquals(1, workflow.events.size)
        assertFalse(workflow.isCompleted())
    }

    @Test
    fun `completing workflow sets completion time`() {
        val event = BusEvent(CommandEventPhase.COMPLETED, workflow.action)

        workflow.addEvent(event)

        assertTrue(workflow.isCompleted())
        assertNotNull(workflow.completedAt)
        assertNotNull(workflow.getDurationMs())
    }

    @Test
    fun `can extract risk level from events`() {
        val riskLevel = RiskAssessmentLevel(75)
        val event = BusEvent(CommandEventPhase.RISK_ASSESSED, workflow.action, riskAssessmentLevel = riskLevel)

        workflow.addEvent(event)

        assertEquals(riskLevel, workflow.getRiskAssessmentLevel())
    }

    @Test
    fun `can extract approver from events`() {
        val event = BusEvent(CommandEventPhase.APPROVED, workflow.action, approvedBy = "admin")

        workflow.addEvent(event)

        assertEquals("admin", workflow.getApprover())
    }

    @Test
    fun `returns null for missing data`() {
        assertNull(workflow.getRiskAssessmentLevel())
        assertNull(workflow.getApprover())
        assertNull(workflow.getRejector())
        assertNull(workflow.getDurationMs())
    }
}
