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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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