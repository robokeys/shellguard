package tech.robd.shellguard.integration
/**
 * [File Info]
 * path: tech/robd/shellguard/integration/SimpleWorkflowIntegrationTest.kt
 * description: Integration test for shellGuardEngineController REST endpoints, verifying end-to-end workflow via a mocked WorkflowEngine.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import tech.robd.shellguard.rkcl.controller.ShellGuardEngineController
import tech.robd.shellguard.engine.WorkflowEngine
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.engine.ActionWorkflow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SimpleWorkflowIntegrationTest {

    private lateinit var controller: ShellGuardEngineController
    private val workflowEngine: WorkflowEngine = mockk()

    @BeforeEach
    fun setUp() {
        controller = ShellGuardEngineController(workflowEngine)
    }

    @Test
    fun `complete workflow from submit to approve`() {
        // Setup mocks
        val command = CommandMessage(
            origin = "test",
            sessionId = "session",
            command = "echo hello"
        )
        val workflow = ActionWorkflow("action-123", command)

        every { workflowEngine.submitAction(any()) } returns workflow
        every { workflowEngine.approveAction("action-123", "admin") } returns true
        every { workflowEngine.getWorkflow("action-123") } returns workflow
        every { workflowEngine.engineType } returns "TestEngine"

        // Step 1: Submit action
        val submitResponse = controller.submitAction("echo hello", "test", "session")
        assertEquals("action-123", submitResponse.body!!.actionId)

        // Step 2: Approve action
        val approveResponse = controller.approveAction("action-123", "admin")
        assertTrue(approveResponse.body!!.success)

        // Step 3: Check workflow status
        val workflowResponse = controller.getWorkflow("action-123")
        assertEquals("action-123", workflowResponse.body!!.actionId)
    }
}