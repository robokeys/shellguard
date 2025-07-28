package tech.robd.shellguard.rkcl.model
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/model/SessionCreateRequest.kt
 * description: Model for REST/API request to create a new SSH session in RKCL.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * API request payload for creating a new SSH session.
 *
 * Used by clients to initiate a session with the specified SSH host and credentials.
 *
 * @property sessionId  Optional: Client-supplied session ID (if null, backend will generate one).
 * @property host       Hostname or IP address of the SSH server to connect to.
 * @property port       Port number for SSH (default: 22).
 * @property username   Username for SSH authentication.
 * @property password   Password for SSH authentication.
 * @property timeout    Connection timeout in milliseconds (default: 30000).
 */
data class SessionCreateRequest(
    val sessionId: String? = null,
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String,
    val timeout: Int = 30000,
    val interactive: Boolean = false
)
