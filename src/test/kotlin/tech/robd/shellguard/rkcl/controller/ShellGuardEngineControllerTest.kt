package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/shellGuardEngineControllerTest.kt
 * description: Unit and contract tests for shellGuardEngineController REST API—verifies endpoints for submit, approve, reject, workflow queries, and stats using a mock WorkflowEngine.
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
import org.springframework.http.HttpStatus
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.engine.ActionWorkflow
import tech.robd.shellguard.engine.WorkflowEngine
import tech.robd.shellguard.engine.WorkflowStats
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ShellGuardEngineControllerTest {

    private lateinit var controller: ShellGuardEngineController
    private val workflowEngine: WorkflowEngine = mockk()

    @BeforeEach
    fun setUp() {
        controller = ShellGuardEngineController(workflowEngine)
    }

    @Test
    fun `submit action returns success response`() {
        // Given
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo hello"
        )
        val workflow = ActionWorkflow("action-123", command)
        every { workflowEngine.submitAction(any()) } returns workflow
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.submitAction("echo hello", "test", "session")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals("action-123", body.actionId)
        assertEquals("echo hello", body.action)
        assertEquals("SUBMITTED", body.currentPhase)
        assertEquals("TestEngine", body.engineType)
        verify { workflowEngine.submitAction(any()) }
    }

    @Test
    fun `approve action returns success when engine approves`() {
        // Given
        every { workflowEngine.approveAction("action-123", "admin") } returns true
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.approveAction("action-123", "admin")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body.success)
        assertEquals("admin", body.approvedBy)
        assertEquals("Action approved", body.message)
        verify { workflowEngine.approveAction("action-123", "admin") }
    }

    @Test
    fun `approve action returns bad request when engine fails`() {
        // Given
        every { workflowEngine.approveAction("action-123", "admin") } returns false
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.approveAction("action-123", "admin")

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body!!
        assertFalse(body.success)
        assertEquals("Failed to approve action", body.message)
    }

    @Test
    fun `reject action works correctly`() {
        // Given
        every { workflowEngine.rejectAction("action-123", "admin", "too risky") } returns true
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.rejectAction("action-123", "admin", "too risky")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body.success)
        assertEquals("admin", body.rejectedBy)
        assertEquals("too risky", body.reason)
    }

    @Test
    fun `get pending approvals returns list`() {
        // Given
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "dangerous command"
        )
        val workflow = ActionWorkflow("action-123", command)
        every { workflowEngine.getPendingApprovals() } returns listOf(workflow)
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.getPendingApprovals()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals(1, body.count)
        assertEquals("action-123", body.actions[0].actionId)
        assertEquals("dangerous command", body.actions[0].action)
    }

    @Test
    fun `get workflow returns workflow when found`() {
        // Given
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo hello"
        )
        val workflow = ActionWorkflow("action-123", command)
        every { workflowEngine.getWorkflow("action-123") } returns workflow
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.getWorkflow("action-123")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals("action-123", body.actionId)
        assertEquals("echo hello", body.action)
    }

    @Test
    fun `get workflow returns 404 when not found`() {
        // Given
        every { workflowEngine.getWorkflow("missing") } returns null

        // When
        val response = controller.getWorkflow("missing")

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `get stats returns workflow statistics`() {
        // Given
        val stats = WorkflowStats(10, 3, 7, 2, 5, 1, 1, 1500.0)
        every { workflowEngine.getWorkflowStats() } returns stats
        every { workflowEngine.engineType } returns "TestEngine"

        // When
        val response = controller.getWorkflowStats()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals("TestEngine", body.engineType)
        assertEquals(10, body.stats.total)
        assertEquals(3, body.stats.active)
        assertEquals(7, body.stats.completed)
    }
}