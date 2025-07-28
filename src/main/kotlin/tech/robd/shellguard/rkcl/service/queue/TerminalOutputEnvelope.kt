package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/TerminalOutputEnvelope.kt
 * description: Envelope for a single line of terminal output to be sent via WebSocket in RKCL.
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
 * Envelope for terminal output events in RKCL WebSocket flows.
 *
 * Used for sending a single line of terminal output (stdout/stderr) along with metadata
 * to frontend clients or agents. Designed for use as the payload in outbound WebSocket
 * messages. Supports future extension (add fields for source, output type, etc).
 *
 * @property sessionId  ID of the SSH session this output belongs to.
 * @property output     The raw output line (stdout/stderr) from the terminal.
 * @property timestamp  Time the output was captured, in milliseconds since epoch.
 */
data class TerminalOutputEnvelope(
    val sessionId: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis()
)