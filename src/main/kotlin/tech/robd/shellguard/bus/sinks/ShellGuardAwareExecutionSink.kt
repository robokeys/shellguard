package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/sinks/ShellGuardAwareExecutionSink.kt
 * description: CommandExecutionSink implementation that reports results and output to the workflow engine, integrating SSH session execution.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.TerminalOutput
import tech.robd.shellguard.engine.WorkflowEngine

/**
 * [📌 Point: shellguard-aware execution sink]
 * Executes commands, reports output/results to the workflow engine, and integrates with SSH session management.
 * Used to bridge RKCL command execution and workflow approval/status system.
 * Handles async execution, emits terminal output, and updates workflow state.
 *
 * Expand this section for additional execution modes, output handling, or monitoring.
 */
@Component
@Suppress("unused")
class ShellGuardAwareExecutionSink(
    private val workflowEngine: WorkflowEngine,
    private val sshSessionManager: tech.robd.shellguard.rkcl.service.SshSessionManager,
    private val eventBus: tech.robd.shellguard.bus.workflow.WorkflowEventBus
) : CommandExecutionSink {

    private val logger = KotlinLogging.logger {}

    private var executionCount = 0
    private var completionCount = 0
    private var failureCount = 0

    // [📌 Point: Coroutine execution scope]
    // Scoped for async command handling; expand with metrics or tracing if needed.
    private val executionScope = CoroutineScope(
        Dispatchers.IO + // Use IO dispatcher for network/file operations
                SupervisorJob() + // Don't cancel all coroutines if one fails
                CoroutineName("shellguard-Executor")
    )

    override fun onCommandExecute(command: CommandMessage) {
        logger.info { "[shellguard] Executing command: ${command.command}" }
        executionCount++

        executionScope.launch {
            try {
                val result = executeCommand(command)
                logger.debug("[shellguard] Executed Command ${command.uuid}")
                // 1. Emit output via workflow engine
                if (!result.stdout.isNullOrEmpty()) {
                    val output = TerminalOutput(
                        sessionId = command.sessionId ?: "default",
                        output = result.stdout
                    )
                    logger.debug("[shellguard] About to emit output: ${command.uuid}")
                    workflowEngine.emitOutput(command.uuid, output)
                    logger.info("[shellguard] Output emitted: ${command.uuid}")
                }

                // 2. Complete the workflow
                logger.debug("[shellguard] About to complete action: ${command.uuid}")
                workflowEngine.completeAction(command.uuid, result)
                logger.info("[shellguard] Action completed: ${command.uuid}")


                // ✅ ADD: Debug logging
                logger.debug("[shellguard] Workflow after completion - checking state...")
                val workflow = workflowEngine.getWorkflow(command.uuid)
                logger.info("[shellguard] Workflow state: completed=${workflow?.isCompleted()}, phase=${workflow?.currentPhase}")


            } catch (e: Exception) {
                logger.error(e) { "[shellguard] Command execution failed: ${e.message}" }
                workflowEngine.failAction(command.uuid, e.message ?: "Unknown error")
            }
        }
    }

    // [📌 Point: Execution sink metrics]
    fun getExecutionStats(): Map<String, Any> {
        return mapOf(
            "executionCount" to executionCount,
            "completionCount" to completionCount,
            "failureCount" to failureCount,
            "isRegistered" to true
        )
    }

    override fun onCommandCompleted(result: CommandResult) {
        logger.info { "[shellguard] Command completed called: ${result.uuid}" }
        completionCount++
    }

    override fun onCommandFailed(command: CommandMessage, error: String) {
        logger.error { "[shellguard] Command failed: ${command.uuid} - $error" }
        failureCount++
    }

    // [📌 Point: Command execution logic]
    private suspend fun executeCommand(command: CommandMessage): CommandResult {
        val startTime = System.currentTimeMillis()
        val sessionId = command.sessionId ?: "default"

        logger.info { "[shellguard] Starting execution for: ${command.command}" }

        val rkclCommand = tech.robd.shellguard.rkcl.model.RkclCommand(
            command = command.command,
            parameter = command.parameter,
            uuid = command.uuid,
            sessionId = sessionId
        )

        logger.info { "[shellguard] About to call sshSessionManager.executeCommandAsynch" }
        return try {
            val rkclResponse = sshSessionManager.executeCommandAsync(sessionId, rkclCommand)

            logger.debug { "[shellguard] Got response: $rkclResponse" }
            val executionTime = System.currentTimeMillis() - startTime

            val result = CommandResult(
                uuid = command.uuid,
                sessionId = sessionId,
                success = rkclResponse.success ?: false,
                message = rkclResponse.message ?: "Command executed",
                exitCode = if (rkclResponse.success == true) 0 else 1,
                stdout = rkclResponse.output,
                stderr = if (rkclResponse.success == false) rkclResponse.message else null,
                executionTimeMs = executionTime
            )
            logger.info { "[shellguard] Created CommandResult: $result" }
            return result
        } catch (e: Exception) {
            logger.error(e) { "[shellguard] ❌ Exception in executeCommand: ${e.message}" }
            CommandResult(
                uuid = command.uuid,
                sessionId = sessionId,
                success = false,
                message = "Exception: ${e.message}",
                exitCode = 1,
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }

}