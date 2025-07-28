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