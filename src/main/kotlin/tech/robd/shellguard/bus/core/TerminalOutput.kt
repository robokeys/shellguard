package tech.robd.shellguard.bus.core
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/TerminalOutput.kt
 * description: Represents a line or chunk of terminal output to be routed to UI, agent, or logs.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

import java.time.Instant

/**
 * Represents a single line or block of terminal output from command execution.
 *
 * Used by [tech.robd.shellguard.bus.sinks.TerminalOutputSink]s to route output to UIs, agents, logs, or other consumers.
 *
 * @property sessionId  The session or connection context for this output (may be null for stateless output).
 * @property output     The raw terminal output string (may include line breaks).
 * @property timestamp  The time this output was generated or received (defaults to now).
 */
// [📌 Point: TerminalOutput: Terminal that may be routed to UI/agent/human]
data class TerminalOutput(
    val sessionId: String?,
    val output: String,
    val timestamp: Instant = Instant.now()
)