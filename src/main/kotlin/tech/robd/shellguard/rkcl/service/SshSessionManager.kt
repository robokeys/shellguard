// src/main/kotlin/tech/robd/rkcl/service/SshSessionManager.kt
package tech.robd.shellguard.rkcl.service

/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/SshSessionManager.kt
 * description: Manages RKCL SSH sessions, orchestrates command execution and terminal output over WebSocket.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import tech.robd.shellguard.engine.CommandUuidGenerator
import tech.robd.shellguard.rkcl.model.RkclCommand
import tech.robd.shellguard.rkcl.model.RkclResponse
import tech.robd.shellguard.rkcl.model.SessionCreateRequest
import java.util.concurrent.ConcurrentHashMap

/**
 * Service responsible for managing multiple concurrent SSH sessions in RKCL.
 *
 * - Handles creation, lookup, removal, and tracking of [ManagedSshSession]s.
 * - Supports registering WebSocket output listeners for real-time terminal streaming.
 * - Executes commands directly via ssh session (agent/web), when approval/risk flows are enabled.
 * - Designed for extensibility and easy integration of auditing/approval in the future.
 *
 * @property rkclTranslator    Utility to translate agent/UI commands to SSH/terminal bytes.
 * @property sshBridgeService  Optional bridge for audit/approval/risk-aware SSH command execution (not yet live).
 * @property objectMapper      Jackson instance for response serialization.
 */
@Service
class SshSessionManager(
    private val rkclTranslator: RkclTranslator,
    //private val sshBridgeService: SshBridgeService,
    private val objectMapper: ObjectMapper,
    private val commandUuidGenerator: CommandUuidGenerator
) {
    private val logger = KotlinLogging.logger {}
    private val sessions = ConcurrentHashMap<String, ManagedSshSession>()

    /**
     * Creates a new managed SSH session from request params.
     * @param request  SSH connection params and optional session ID.
     * @return         Assigned session ID (generated if not supplied).
     */
    fun createSession(request: SessionCreateRequest): SshSessionCreateResponse {
        val sessionId = request.sessionId ?: commandUuidGenerator.generateId()

        val session = ManagedSshSession(
            sessionId = sessionId,
            host = request.host,
            port = request.port,
            username = request.username,
            password = request.password,
            timeout = request.timeout,
            translator = rkclTranslator,
            objectMapper = objectMapper
        )

        //sessions[sessionId] = session
        val isConnected = session.connect()
        if (isConnected) {
            sessions[sessionId] = session
            logger.info { "Created SSH session: $sessionId -> ${request.username}@${request.host}:${request.port}" }
        } else {
            logger.error { "Failed to create SSH session: $sessionId -> ${request.username}@${request.host}:${request.port}" }
        }


        logger.info { "Created SSH session: $sessionId -> ${request.username}@${request.host}:${request.port}" }
        return SshSessionCreateResponse(sessionId,isConnected)
    }

    /**
     * Look up a session by its ID.
     */
    fun getSession(sessionId: String): ManagedSshSession? = sessions[sessionId]

    /**
     * Disconnects and removes a session.
     */
    fun removeSession(sessionId: String) {
        sessions[sessionId]?.disconnect()
        sessions.remove(sessionId)
        logger.info { "Removed SSH session: $sessionId" }
    }

    /**
     * Returns a collection of all active sessions.
     */
    fun getAllSessions(): Collection<ManagedSshSession> = sessions.values

    suspend fun executeCommandAsync(sessionId: String, command: RkclCommand): RkclResponse {
             logger.info { "[Manager] executeCommandAsync: $command for session: $sessionId" }
             val session = getSession(sessionId)
                 ?: return RkclResponse(
                         type = "error",
                         uuid = command.uuid,
                         sessionId = sessionId,
                         success = false,
                         message = "Session not found: $sessionId"
                             )

             return session.executeCommandAsync(command)
         }

    /**
     * Executes a command in the specified SSH session.
     * Returns a [RkclResponse] with command result, or error if session not found.
     *
     * You may later swap the implementation to use [sshBridgeService] for auditing/approval.
     */
    fun executeCommand(sessionId: String, command: RkclCommand): RkclResponse {
        logger.info { "[Manager] executeCommand: $command for session: $sessionId" }
        val session = getSession(sessionId)
            ?: return RkclResponse(
                type = "error",
                uuid = command.uuid,
                sessionId = sessionId,
                success = false,
                message = "Session not found: $sessionId"
            )
        logger.info { "[Bus] About to execute command on ManagedSshSession: $command" }
        val response = session.executeCommand(command)
        logger.info { "[Manager] executeCommand response: $response for session: $sessionId" }
        return response
    }

    /**
     * Adds a WebSocket session as a real-time output listener for a session.
     * Returns true if successful.
     */
    fun addOutputListener(sessionId: String, webSocketSession: WebSocketSession, interactive: Boolean = false): Boolean {
        val session = getSession(sessionId)
        return if (session != null) {
            session.addOutputListener(webSocketSession, interactive)  // Pass interactive flag
            true
        } else {
            false
        }
    }

    /**
     * Removes a WebSocket session from the session's output listeners.
     * Returns true if successful.
     */
    fun removeOutputListener(sessionId: String, webSocketSession: WebSocketSession): Boolean {
        val session = getSession(sessionId)
        return if (session != null) {
            session.removeOutputListener(webSocketSession)
            true
        } else {
            false
        }
    }

}

data class SshSessionCreateResponse(
    val sessionId: String,
    val success: Boolean,
)