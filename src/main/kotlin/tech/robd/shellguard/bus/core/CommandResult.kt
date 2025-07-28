package tech.robd.shellguard.bus.core
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/CommandResult.kt
 * description: Result and metadata for completed or failed command execution in the CommandBus pipeline.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Represents the outcome of a command execution, including status, output, and timing.
 *
 * This result is returned by execution sinks to the bus, and can be routed to UI, agents, audit logs, etc.
 *
 * @property uuid           The unique identifier for the command (matches [CommandMessage.uuid]).
 * @property sessionId      Session or execution context, if any.
 * @property success        True if the command succeeded, false if it failed or was rejected.
 * @property message        Human-readable result message (success, error, rejection, etc.).
 * @property metadata       Arbitrary metadata (timings, resource usage, annotations, etc.).
 * @property exitCode       Process exit code, if relevant (e.g., shell/system commands).
 * @property stdout         Standard output from execution (if any).
 * @property stderr         Standard error output (if any).
 * @property executionTimeMs Time taken to execute, in milliseconds (if available).
 */
// [📌 Point: Agent command result: Command responses (sent to agents, UI, etc.]
data class CommandResult(
    val uuid: String,
    val sessionId: String?,
    val success: Boolean,
    val message: String,
    val metadata: Map<String, Any?> = emptyMap(),
    val exitCode: Int? = null, // Added for command execution
    val stdout: String? = null, // Added for output
    val stderr: String? = null, // Added for errors
    val executionTimeMs: Long? = null // Added for timing
)