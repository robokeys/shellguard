// src/main/kotlin/tech/robd/rkcl/handler/RkclTerminalHandler.kt
package tech.robd.shellguard.rkcl.handler

/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/handler/RkclTerminalHandler.kt
 * description: WebSocket handler for RKCL Terminal. Orchestrates session actions and RKCL command execution.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.sinks.WebSocketTerminalOutputSink
import tech.robd.shellguard.engine.CommandUuidGenerator
import tech.robd.shellguard.engine.WorkflowEngine
import tech.robd.shellguard.rkcl.model.RkclCommand
import tech.robd.shellguard.rkcl.model.RkclResponse
import tech.robd.shellguard.rkcl.model.SessionCreateRequest
import tech.robd.shellguard.rkcl.service.SshSessionManager
import java.util.concurrent.ConcurrentHashMap
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * RKCL Terminal WebSocket handler.
 *
 * - Accepts client WebSocket connections.
 * - Supports session management actions ("create_session", "connect_session", etc) via JSON.
 * - Accepts RKCL terminal commands (JSON or text).
 * - Registers/deregisters listeners for real-time terminal output streaming.
 * - Handles output, error, and session lifecycle messages.
 *
 * @property sshSessionManager   Central SSH session and listener manager.
 * @property objectMapper        Jackson mapper for payload and response handling.
 */
@Component
class RkclTerminalHandler(
    private val sshSessionManager: SshSessionManager,
    private val objectMapper: ObjectMapper,
    private val workflowEngine: WorkflowEngine,
    private val commandUuidGenerator: CommandUuidGenerator
) : WebSocketHandler {

    private val logger = KotlinLogging.logger {}
    private val clientSessions = ConcurrentHashMap<String, WebSocketSession>()

    lateinit var webSocketTerminalOutputSink: WebSocketTerminalOutputSink

    init {
        webSocketTerminalOutputSink = WebSocketTerminalOutputSink { sessionId ->
            clientSessions[sessionId]
        }
    }

    override fun supportsPartialMessages(): Boolean = false

    /** Called when a WebSocket connection is established. Sends a welcome/status message. */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info { "RKCL Terminal client connected: ${session.id}" }
        logger.info { "✅ WebSocket connection established: ${session.id}" }
        logger.info { "   Remote address: ${session.remoteAddress}" }
        logger.info { "   URI: ${session.uri}" }
        logger.debug { "[WS] afterConnectionEstablished called for session=${session.id}" }
        clientSessions[session.id] = session

        // Send welcome message
        val welcome = RkclResponse(
            type = "status",
            uuid = commandUuidGenerator.generateId(),
            sessionId = "system",
            success = true,
            message = "Connected to RKCL Terminal Bridge",
            metadata = mapOf(
                "version" to "0.1.0",
                "capabilities" to listOf("rkcl", "ssh", "output_streaming"),
                "supported_commands" to listOf(
                    "TEXT", "LINE", "KEY", "COMBO", "EDIT",
                    "STATUS", "PING", "PAUSE", "RESUME"
                )
            )
        )

        sendToClient(session, welcome)
        logger.info { "[WS] Registered WebSocket session: ${session.id} (clientSessions=${clientSessions.size})" }
    }

    /** Handles messages sent to this WebSocket (either a session action or a terminal command). */
    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.info { "[WS] Received payload: $message (session=${session.id})" }
        if (message is TextMessage) {
            try {
                val payload = message.payload
                logger.info { "[WS] Received payload: ${message} (session=${session.id})" }
                logger.debug { "Received message: $payload" }

                // Enforce single-line JSON - reject messages with newlines
                if (payload.contains('\n') || payload.contains('\r')) {
                    sendError(session, "JSON messages must be single-line only - no newlines allowed")
                    return
                }

                // Parse JSON first to determine message type properly
                if (payload.trim().startsWith("{")) {
                    try {
                        // Let Jackson validate the JSON properly - if it parses, it's valid
                        val jsonNode = objectMapper.readTree(payload)

                        // Check if this is a session management message (has "action" at root level)
                        if (jsonNode.has("action")) {
                            handleSessionAction(session, payload)
                        } else {
                            // Assume it's an RKCL command
                            handleRkclCommand(session, payload)
                        }
                    } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
                        logger.error(e) { "[WS] Invalid JSON received: $payload" }
                        sendError(
                            session,
                            "Invalid JSON format - must be complete single-line JSON object: ${e.message}"
                        )
                    } catch (e: Exception) {
                        logger.error(e) { "[WS] Failed to parse JSON message: $payload" }
                        sendError(session, "Failed to parse JSON message: ${e.message}")
                    }
                } else {
                    // Text format - assume RKCL command
                    handleRkclCommand(session, payload)
                }

            } catch (e: Exception) {
                logger.error(e) { "Error processing message from ${session.id}" }
                sendError(session, "Message processing failed: ${e.message}")
            }
        }
    }

    /**
     * Handles JSON messages for session management (create, connect, list, disconnect).
     */
    private fun handleSessionAction(session: WebSocketSession, payload: String) {
        try {
            val actionData = objectMapper.readTree(payload)
            val action = actionData.get("action")?.asText()

            when (action) {
                "create_session" -> {
                    val request = objectMapper.treeToValue(actionData, SessionCreateRequest::class.java)
                    val createResponse = sshSessionManager.createSession(request)
                    val sessionId = createResponse.sessionId
                    logger.info { "[WS] create_session: $sessionId by client ${session.id}" }

                    // Attach this WebSocket as output listener
                    val added = sshSessionManager.addOutputListener(sessionId, session, request.interactive)
                    // logger.info { "[WS] Added output listener to session $sessionId: $added" }
                    logger.info { "[WS] Added ${if (request.interactive) "interactive" else "API"} listener to session $sessionId: $added" }

                    val response = RkclResponse(
                        type = "session_created",
                        uuid = commandUuidGenerator.generateId(),
                        sessionId = sessionId,
                        success = createResponse.success,
                        message = "SSH session created",
                        metadata = mapOf(
                            "host" to request.host,
                            "port" to request.port,
                            "username" to request.username,
                            "interactive" to request.interactive
                        )
                    )
                    sendToClient(session, response)
                }

                "connect_session" -> {
                    val sessionId = actionData.get("sessionId")?.asText()
                    val interactive = actionData.get("interactive")?.asBoolean() ?: false  // ADD THIS

                    if (sessionId != null) {
                        logger.info { "[WS] connect_session: $sessionId by client ${session.id} (interactive=$interactive)" }
                        val attached = sshSessionManager.addOutputListener(sessionId, session, interactive)
                        logger.info { "[WS] Added ${if (interactive) "interactive" else "API"} listener to session $sessionId: $attached" }

                        if (attached) {
                            val response = RkclResponse(
                                type = "session_connected",
                                uuid = commandUuidGenerator.generateId(),
                                sessionId = sessionId,
                                success = true,
                                message = "Connected to existing session",
                                metadata = mapOf("interactive" to interactive)
                            )
                            sendToClient(session, response)
                        } else {
                            sendError(session, "Session not found: $sessionId")
                        }
                    }
                }

                "disconnect_session" -> {
                    val sessionId = actionData.get("sessionId")?.asText() ?: "default"

                    sshSessionManager.removeSession(sessionId)

                    // Remove this WebSocket as output listener
                    sshSessionManager.removeOutputListener(sessionId, session)

                    logger.info { "[WS] disconnect_session: $sessionId by client ${session.id}" }

                    RkclResponse(
                        type = "session_disconnected",
                        uuid = commandUuidGenerator.generateId(),
                        sessionId = sessionId,
                        success = true,
                        message = "SSH session disconnected"
                    )
                }

                "list_sessions" -> {
                    val sessions = sshSessionManager.getAllSessions().map { sshSession ->
                        mapOf(
                            "sessionId" to sshSession.sessionId
                        )
                    }

                    val response = RkclResponse(
                        type = "sessions_list",
                        uuid = commandUuidGenerator.generateId(),
                        sessionId = "system",
                        success = true,
                        message = "Available sessions",
                        metadata = mapOf("sessions" to sessions)
                    )
                    sendToClient(session, response)
                }

                else -> {
                    sendError(session, "Unknown action: $action")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling session action" }
            sendError(session, "Session action failed: ${e.message}")
        }
    }

    /**
     * Handles incoming RKCL terminal commands (JSON or text format).
     */
    private fun handleRkclCommand(session: WebSocketSession, payload: String) {
        try {
            // Check for empty payload
            if (payload.isBlank()) {
                sendError(session, "Empty command payload received")
                return
            }
            val command = if (payload.startsWith("{")) {
                // JSON format
                //   objectMapper.readValue<RkclCommand>(payload)
                try {
                    val jsonNode = objectMapper.readTree(payload)

                    // Check for required fields
                    if (!jsonNode.has("command")) {
                        sendError(session, "Missing required 'command' field in JSON payload")
                        return
                    }

                    // Check for unsupported fields that indicate wrong format
                    if (jsonNode.has("type") || jsonNode.has("meta")) {
                        sendError(
                            session,
                            "Unsupported JSON format - use {\"command\": \"...\", \"parameter\": \"...\", \"uuid\": \"...\", \"sessionId\": \"...\"}"
                        )
                        return
                    }
                    // Attempt to deserialize to RkclCommand
                    objectMapper.readValue<RkclCommand>(payload)

                } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
                    sendError(session, "Invalid JSON format: ${e.message}")
                    return
                } catch (e: Exception) {
                    sendError(session, "Failed to parse JSON command: ${e.message}")
                    return
                }
            } else {
                // Text format parsing
                parseTextCommand(payload)
            }


            // Validate the parsed command
            if (command.command.isBlank()) {
                sendError(session, "Command cannot be empty")
                return
            }

            // Validate command length (prevent extremely long commands)
            if (command.command.length > 1000) {
                sendError(session, "Command too long (max 1000 characters)")
                return
            }

            val sessionId = command.sessionId ?: "default"
            logger.info { "[WS] Received RKCL command: $command (from client ${session.id}, sessionId=$sessionId)" }

            // Submit to workflow engine
            val cmdMsg = CommandMessage(
                origin = "websocket",
                sessionId = sessionId,
                command = command.command,
                parameter = command.parameter,
                reason = "User submitted via WebSocket"
            )
            workflowEngine.submitAction(cmdMsg)
            logger.debug { "[WS] CommandMessage submitted to bus: $cmdMsg" }
            // Send acknowledgment back to client
            val ack = RkclResponse(
                type = "command_accepted",
                uuid = command.uuid,
                sessionId = sessionId,
                success = true,
                message = "Command submitted for approval"
            )
            sendToClient(session, ack)

        } catch (e: Exception) {
            logger.error(e) { "[WS] Unexpected error processing RKCL command: $payload" }
            sendError(session, "Unexpected error processing command: ${e.message}")
        }
    }

    /**
     * Parses a text-based command line, supporting optional UUID prefix and command parameters.
     */
    private fun parseTextCommand(text: String): RkclCommand {
        // Define valid RKCL commands - core terminal automation commands only
        val validCommands = setOf("TEXT", "LINE", "KEY", "COMBO", "EDIT")
        val trimmed = text.trim()

        // Check for UUID prefix: id=uuid,command:parameter
        val uuidPattern = Regex("^id=([^,]+),\\s*(.+)$")
        val matcher = uuidPattern.find(trimmed)

        val (uuid, commandText) = if (matcher != null) {
            matcher.groupValues[1] to matcher.groupValues[2]
        } else {
            null to trimmed
        }

        // Split command and parameter
        val separators = arrayOf(":", "-", "_", ".", ",", " ")
        var command = commandText
        var parameter: String? = null

        for (sep in separators) {
            val index = commandText.indexOf(sep)
            if (index > 0) {
                command = commandText.substring(0, index).trim()
                // Validate that command is a known RKCL command
                if (command.uppercase() !in validCommands) {
                    throw IllegalArgumentException("Invalid RKCL command: '$command'. Valid commands: ${validCommands.joinToString()}")
                }
                parameter = commandText.substring(index + 1).trim()
                break
            }
        }

        return RkclCommand(
            command = command,
            parameter = parameter,
            uuid = uuid ?: commandUuidGenerator.generateId()
        )
    }

    /**
     * Sends a response (as JSON) to the connected WebSocket client.
     */
    private fun sendToClient(session: WebSocketSession, response: RkclResponse) {
        try {
            if (session.isOpen) {
                logger.info { "[WS] Sending response: $response (session=${session.id})" }
                val json = objectMapper.writeValueAsString(response)
                session.sendMessage(TextMessage(json))
                logger.debug { "[WS] Successfully sent response to session ${session.id}" }

            } else {
                logger.warn { "[WS] Tried to send to closed session: ${session.id}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error sending response to client ${session.id}: ${e.message}" }
        }
    }

    /**
     * Sends an error response to the client.
     */
    private fun sendError(session: WebSocketSession, error: String) {
        logger.warn { "[WS] Sending error to client ${session.id}: $error" }
        val response = RkclResponse(
            type = "error",
            uuid = commandUuidGenerator.generateId(),
            sessionId = "system",
            success = false,
            message = error,
            metadata = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "expectedFormat" to mapOf(
                    "command" to "string (required)",
                    "parameter" to "string (optional)",
                    "uuid" to "string (optional)",
                    "sessionId" to "string (optional)"
                )
            )
        )
        sendToClient(session, response)
    }

    /**
     * Removes this client from all output listeners on disconnect.
     */
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.info { "RKCL Terminal client disconnected: ${session.id} (${closeStatus.code} - ${closeStatus.reason})" }
        clientSessions.remove(session.id)

        // Remove this session from all SSH session listeners
        sshSessionManager.getAllSessions().forEach { sshSession ->
            sshSession.removeOutputListener(session)
            logger.info { "[WS] Removed output listener for disconnected session: ${session.id} on SSH session ${sshSession.sessionId}" }

        }
        logger.info { "[WS] WebSocket session closed: ${session.id} " }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(exception) { "RKCL Terminal WebSocket error for ${session.id}" }
    }

    fun lookupWebSocketSession(sessionId: String?): WebSocketSession? {
        // You may need to map RKCL sessionId <-> websocket session.id in the future.
        // For now, try using websocket session.id if that's what you set as sessionId in CommandMessage.
        return sessionId?.let { clientSessions[it] }
    }


}
