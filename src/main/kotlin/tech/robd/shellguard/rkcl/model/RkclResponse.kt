package tech.robd.shellguard.rkcl.model
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/model/RkclResponse.kt
 * description: API response model for RKCL commands. Supports command results, status, error, and terminal output events.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * API response object for all RKCL command and terminal events.
 *
 * Used to return results, status, or errors for command execution and session management endpoints.
 *
 * @property type       The event or response type ("command_result", "terminal_output", "error", "status", etc.).
 * @property uuid       The UUID of the command or event this response relates to.
 * @property sessionId  The session ID (if applicable).
 * @property timestamp  Milliseconds since epoch (auto-populated).
 * @property success    Optional: Indicates if the command or event was successful.
 * @property message    Optional: Human-readable status or error message.
 * @property output     Optional: Standard output or result from the command.
 * @property command    Optional: The original command string.
 * @property parameter  Optional: The command's parameter or argument string.
 * @property metadata   Optional: Arbitrary key-value metadata.
 */
data class RkclResponse @OptIn(ExperimentalTime::class) constructor(
    val type: String, // "command_result", "terminal_output", "error", "status"
    val uuid: String,
    val sessionId: String?,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val success: Boolean? = null,
    val message: String? = null,
    val output: String? = null,
    val command: String? = null,
    val parameter: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)
