package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/bus/core/CommandExecutionSink.kt
 * description: Interface for handling command execution events (start, completion, failure) in the CommandBus pipeline.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult

/**
 * Sink for command execution events within the [tech.robd.shellguard.bus.core.CommandBus] pipeline.
 *
 * Allows decoupled handling of command start, successful completion, and failure.
 * Typical uses: executing system/remote commands, tracking execution state, and updating UIs or logs.
 */
interface CommandExecutionSink {
    /**
     * Called when a command is ready for execution (after approval/risk checks).
     *
     * @param command The [tech.robd.shellguard.bus.core.CommandMessage] to execute.
     */
    fun onCommandExecute(command: CommandMessage)

    /**
     * Called when a command completes successfully.
     *
     * @param result The [tech.robd.shellguard.bus.core.CommandResult] object containing status, output, etc.
     */
    fun onCommandCompleted(result: CommandResult)

    /**
     * Called if a command execution fails.
     *
     * @param command The original [CommandMessage] that failed.
     * @param error   Human-readable error message or stack trace.
     */
    fun onCommandFailed(command: CommandMessage, error: String)
}