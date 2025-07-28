// src/main/kotlin/tech/robd/rkcl/model/RkclCommand.kt
package tech.robd.shellguard.rkcl.model
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/model/RkclCommand.kt
 * description: Data model for API/REST command requests in RKCL. Used for session-based remote execution.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tech.robd.shellguard.engine.CommandIdUtil
import java.time.Instant

/**
 * API-facing command object for session-based remote execution via RKCL.
 *
 * Used in REST payloads to request execution of a command within a specific SSH/WebSocket session.
 *
 * @property command     The main command to run (e.g., "ls", "cat file.txt").
 * @property parameter   Optional parameter or argument string for the command.
 * @property uuid        Globally unique command identifier (auto-generated if not provided).
 * @property sessionId   Optional: The session in which to execute the command.
 * @property timestamp   Milliseconds since epoch for when this command was created.
 */
data class RkclCommand @JsonCreator constructor(
    @param:JsonProperty("command") val command: String,
    @param:JsonProperty("parameter") val parameter: String? = null,
    @param:JsonProperty("uuid") val uuid: String =  CommandIdUtil.generateId(),
    @param:JsonProperty("sessionId") val sessionId: String? = null,
    @param:JsonProperty("timestamp") val timestamp: Long = Instant.now().toEpochMilli()
)