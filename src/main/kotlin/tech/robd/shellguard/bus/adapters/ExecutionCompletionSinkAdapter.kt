package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/ExecutionCompletionSinkAdapter.kt
 * description: Adapter converting SshellGuard BusEvents into CommandExecutionSink Completion and Failure operations.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase

/**
 * Adapter that converts shellguard BusEvents to CommandExecutionSink calls
 */
class ExecutionCompletionSinkAdapter(
    private val sink: CommandExecutionSink
) {
    // [🧩 Section: Event Handling]
    // Main event dispatch logic: handles COMPLETED, FAILED.
    // Expand here for new event phases, richer error handling, or async support.
    fun handleEvent(event: BusEvent) {
        when (event.phase) {

            CommandEventPhase.COMPLETED -> {
                val result = event.result ?: createDummyResult(event, true)
                sink.onCommandCompleted(result)
            }

            CommandEventPhase.FAILED -> {
                val result = event.result ?: createDummyResult(event, false)
                val error = result.message
                sink.onCommandFailed(event.command, error)
            }

            else -> {
                // Ignore other phases
            }
        }
    }
    // [/🧩 Section: Event Handling]

    // If new phases or richer CommandResult creation are added, extend this helper.
    private fun createDummyResult(event: BusEvent, success: Boolean): CommandResult {
        return CommandResult(
            uuid = event.command.uuid,
            sessionId = event.command.sessionId,
            success = success,
            message = if (success) "Command completed" else "Command failed"
        )
    }
}