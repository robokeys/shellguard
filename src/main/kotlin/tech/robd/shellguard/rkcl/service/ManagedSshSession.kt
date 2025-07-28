package tech.robd.shellguard.rkcl.service

/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/ManagedSshSession.kt
 * description: Represents a managed SSH session for RKCL, with integrated WebSocket output and control command handling.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import net.schmizz.sshj.connection.channel.direct.Session

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import tech.robd.shellguard.engine.CommandIdUtil
import tech.robd.shellguard.rkcl.model.RkclCommand
import tech.robd.shellguard.rkcl.model.RkclResponse
import java.io.InputStream
import java.io.OutputStream
import kotlin.text.Charsets.UTF_8

/**
 * Encapsulates an SSH session with lifecycle management, output listeners, and control command support.
 *
 * Features:
 *  - Connect/disconnect to SSH host using SSHJ.
 *  - Reads and buffers terminal output, broadcasting to all subscribed WebSocket listeners.
 *  - Supports simple control commands: STATUS, PING, PAUSE, RESUME.
 *  - Serializes all terminal output and command results to [RkclResponse] for frontends/agents.
 *  - Handles concurrent listeners, buffered output, and clean error reporting.
 *
 * @property sessionId   Unique session identifier.
 * @property host        SSH server host.
 * @property port        SSH server port.
 * @property username    SSH user.
 * @property password    SSH password.
 * @property timeout     SSH connection timeout in ms.
 * @property translator  Translates RKCL commands to bytes for SSH terminal.
 * @property objectMapper JSON serializer for WebSocket events.
 */
class ManagedSshSession(
    val sessionId: String,
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val timeout: Int,
    private val translator: RkclTranslator,
    private val objectMapper: ObjectMapper
) {
    private val logger = KotlinLogging.logger {}

    private var sshClient: SSHClient? = null
    private var session: Session? = null
    private var shell: Session.Shell? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

   // private val outputListeners = mutableSetOf<WebSocketSession>()
    private val apiListeners = mutableSetOf<WebSocketSession>()
    private val interactiveListeners = mutableSetOf<WebSocketSession>()

    private val outputBuffer = mutableListOf<String>()
    private var isConnected = false
    private var isPaused = false

    // public getters
    fun getHost(): String = host
    fun getPort(): Int = port
    fun getUsername(): String = username

    /**
     * Establishes an SSH connection and shell session.
     * @return true if connected successfully, false otherwise.
     */
    fun connect(): Boolean {
        try {
            sshClient = SSHClient().apply {
                addHostKeyVerifier(PromiscuousVerifier())
                connectTimeout = timeout
                connect(host, port)
                authPassword(username, password)
            }

            session = sshClient!!.startSession().apply {
                allocateDefaultPTY() // This is crucial for interactive shells
            }
            shell = session!!.startShell()
            outputStream = shell!!.outputStream
            inputStream = shell!!.inputStream

            isConnected = true
            startOutputReader()
            // Give the shell a moment to initialize and show login messages
            Thread.sleep(500)
            // Force a new prompt by sending a newline
            forceNewPrompt()

            logger.info { "SSH session connected: $sessionId" }
            return true

        } catch (e: Exception) {
            isConnected = false
            logger.error(e) { "Failed to connect SSH session $sessionId" }
            disconnect()
            return false
        }
    }

    /**
     * Forces a new shell prompt to appear
     */
    private fun forceNewPrompt() {
        try {
            logger.debug { "Forcing new prompt for session $sessionId" }

            // Send a newline to get a fresh prompt
            outputStream?.write("\n".toByteArray())
            outputStream?.flush()

            // Wait a moment
            Thread.sleep(500)

            // Send another newline if needed
            outputStream?.write("\n".toByteArray())
            outputStream?.flush()

            logger.debug { "Sent prompt-forcing commands" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to force prompt" }
        }
    }

    /**
     * Cleanly disconnects and closes all resources.
     */
    fun disconnect() {
        try {
            isConnected = false
            shell?.close()
            session?.close()
            sshClient?.disconnect()
            logger.info { "SSH session disconnected: $sessionId" }
        } catch (e: Exception) {
            logger.error(e) { "Error disconnecting SSH session $sessionId" }
        }
    }

    /**
     * Registers a WebSocket listener for terminal output.
     * Sends last 10 lines of buffered output upon registration.
     */
    fun addOutputListener(webSocket: WebSocketSession, interactive: Boolean = false) {
        if (interactive) {
            interactiveListeners.add(webSocket)
            logger.info { "[SSH] Added interactive listener: ${webSocket.id}" }
        } else {
            apiListeners.add(webSocket)
            logger.info { "[SSH] Added API listener: ${webSocket.id}" }
        }

        // Send recent output buffer to new listener
        outputBuffer.takeLast(10).forEach { line ->
            sendCompleteOutput(webSocket, line)
        }
    }
    /**
     * Removes a WebSocket output listener.
     */
    fun removeOutputListener(webSocket: WebSocketSession) {
        val removedFromApi = apiListeners.remove(webSocket)
        val removedFromInteractive = interactiveListeners.remove(webSocket)

        if (removedFromApi || removedFromInteractive) {
            logger.info { "[SSH] Removed listener: ${webSocket.id}" }
        }
    }
    /**
     * Reads terminal output from SSH and broadcasts both complete lines and partial updates.
     * Enhanced to handle ANSI escape sequences, prompts, and incremental content.
     */
    private fun startOutputReader() {
        Thread {
            val buffer = ByteArray(1024)
            val lineBuffer = StringBuilder()
            var lastOutputTime = System.currentTimeMillis()
            var lastPartialSentTime = 0L
            var inEscapeSequence = false
            val escapeBuffer = StringBuilder()

            try {
                while (isConnected && inputStream != null) {
                    // Check if input is available
                    if (inputStream!!.available() > 0) {
                        val bytesRead = inputStream!!.read(buffer)
                        if (bytesRead > 0) {
                            lastOutputTime = System.currentTimeMillis()
                            val text = String(buffer, 0, bytesRead, UTF_8)
                            logger.debug { "[SSH] Raw input ($bytesRead bytes): '${text.replace('\n', '\\').replace('\r', '/').replace("\u001b", "[ESC]")}'" }
                            var i = 0
                            while (i < text.length) {
                                val char = text[i]

                                when {
                                    // Handle ANSI escape sequences
                                    char == '\u001b' || inEscapeSequence -> {
                                        if (char == '\u001b') {
                                            inEscapeSequence = true
                                            escapeBuffer.clear()
                                            escapeBuffer.append(char)
                                        } else if (inEscapeSequence) {
                                            escapeBuffer.append(char)
                                            // End escape sequence on letter or specific chars
                                            if (char.isLetter() || char in "~@") {
                                                inEscapeSequence = false
                                                logger.debug { "[SSH] Filtered ANSI sequence of length ${escapeBuffer.length}" }
                                            }
                                        }
                                    }

                                    char == '\n' -> {
                                        val line = lineBuffer.toString().trimEnd('\r')
                                        if (line.isNotEmpty()) {
                                            handleCompleteLine(line)
                                        }
                                        lineBuffer.clear()
                                        lastPartialSentTime = 0L // Reset partial tracking
                                    }

                                    char == '\r' -> {
                                        // Handle CR - for now, treat as potential line end
                                        val line = lineBuffer.toString()
                                        if (line.isNotEmpty()) {
                                            handleCompleteLine(line)
                                            lineBuffer.clear()
                                            lastPartialSentTime = 0L
                                        }
                                    }

                                    !inEscapeSequence -> {
                                        lineBuffer.append(char)

                                        // Send partial updates for long content
                                        val currentTime = System.currentTimeMillis()
                                        if (lineBuffer.length > 200 &&
                                            (lastPartialSentTime == 0L || currentTime - lastPartialSentTime > 500)) {

                                            handlePartialContent(lineBuffer.toString())
                                            lastPartialSentTime = currentTime
                                        }
                                    }
                                }
                                i++
                            }
                        }
                    } else {
                        // No new input - check for accumulated content
                        val currentTime = System.currentTimeMillis()

                        if (lineBuffer.isNotEmpty()) {
                            val timeSinceLastOutput = currentTime - lastOutputTime
                            val currentContent = lineBuffer.toString().trim()

                            when {
                                // Check for shell prompt (quick timeout)
                                timeSinceLastOutput > 300 && isLikelyPrompt(currentContent) -> {
                                    logger.debug { "[SSH] Detected prompt: '$currentContent'" }
                                    handleCompleteLine(currentContent)
                                    lineBuffer.clear()
                                    lastPartialSentTime = 0L
                                }

                                // Send partial update for accumulated content (longer timeout)
                                timeSinceLastOutput > 500 && currentContent.isNotEmpty() &&
                                        (lastPartialSentTime == 0L || currentTime - lastPartialSentTime > 1000) -> {
                                    handlePartialContent(currentContent)
                                    lastPartialSentTime = currentTime
                                }

                                // Final flush for really old content
                                timeSinceLastOutput > 2000 && currentContent.isNotEmpty() -> {
                                    logger.debug { "[SSH] Flushing old content: '$currentContent'" }
                                    handleCompleteLine(currentContent)
                                    lineBuffer.clear()
                                    lastPartialSentTime = 0L
                                }
                            }
                        }

                        // Small sleep to prevent busy waiting
                        Thread.sleep(100)
                    }
                }
            } catch (e: Exception) {
                if (isConnected) {
                    logger.error(e) { "Error reading SSH output for session $sessionId" }
                }
            }

            // Handle any remaining content when stream ends
            if (lineBuffer.isNotEmpty()) {
                val remaining = lineBuffer.toString().trim()
                if (remaining.isNotEmpty()) {
                    handleCompleteLine(remaining)
                }
            }
        }.apply {
            name = "SSH-Output-Reader-$sessionId"
            isDaemon = true
        }.start()
    }

    /**
     * Handles a complete line of output
     */
    private fun handleCompleteLine(line: String) {
        logger.debug { "[SSH] Complete line: '$line'" }
        logger.info { "[SSH] Output line for session $sessionId: $line" }

        // Add to buffer (keep last 100 lines)
        outputBuffer.add(line)
        if (outputBuffer.size > 100) {
            outputBuffer.removeAt(0)
        }

        broadcastToListeners(line, "terminal_output", complete = true)
    }

    /**
     * Handles partial content that's still being built up
     */
    private fun handlePartialContent(content: String) {
        logger.debug { "[SSH] Partial content: '$content'" }
        broadcastToListeners(content, "terminal_partial", complete = false)
    }

    /**
     * Broadcasts content to all WebSocket listeners
     */
    /**
     * Broadcasts content to all WebSocket listeners with mode-specific handling
     */
    private fun broadcastToListeners(content: String, messageType: String, complete: Boolean) {
        // Clean up closed API listeners
        val beforePruneApi = apiListeners.size
        apiListeners.removeIf { !it.isOpen }
        val afterPruneApi = apiListeners.size

        // Clean up closed interactive listeners
        val beforePruneInteractive = interactiveListeners.size
        interactiveListeners.removeIf { !it.isOpen }
        val afterPruneInteractive = interactiveListeners.size

        if (beforePruneApi != afterPruneApi) {
            logger.debug { "Pruned ${beforePruneApi - afterPruneApi} closed API listeners" }
        }
        if (beforePruneInteractive != afterPruneInteractive) {
            logger.debug { "Pruned ${beforePruneInteractive - afterPruneInteractive} closed interactive listeners" }
        }

        if (afterPruneApi == 0 && afterPruneInteractive == 0) {
            logger.debug { "[SSH] No listeners for session $sessionId" }
            return
        }

        logger.debug { "Broadcasting to $afterPruneApi API + $afterPruneInteractive interactive listeners: $messageType" }

        // Send to API listeners (only complete messages)
        if (complete || messageType == "terminal_output") {
            apiListeners.forEach { listener ->
                try {
                    sendContentToListener(listener, content, "terminal_output", true)
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to send to API listener ${listener.id}" }
                }
            }
        }

        // Send to interactive listeners (all message types)
        interactiveListeners.forEach { listener ->
            try {
                sendContentToListener(listener, content, messageType, complete)
            } catch (ex: Exception) {
                logger.error(ex) { "Failed to send to interactive listener ${listener.id}" }
            }
        }
    }
    /**
     * Sends content to a specific WebSocket listener
     */
    private fun sendContentToListener(webSocket: WebSocketSession, content: String, messageType: String, complete: Boolean) {
        if (!webSocket.isOpen) {
            return
        }

        synchronized(webSocket) {
            try {
                val response = RkclResponse(
                    type = messageType,
                    uuid = CommandIdUtil.generateId(),
                    sessionId = sessionId,
                    output = content,
                    metadata = mapOf(
                        "complete" to complete,
                        "timestamp" to System.currentTimeMillis()
                    )
                )

                val jsonMessage = objectMapper.writeValueAsString(response)
                webSocket.sendMessage(TextMessage(jsonMessage))

                logger.debug { "Sent $messageType to ${webSocket.id}: '${content.take(50)}${if(content.length > 50) "..." else ""}'" }

            } catch (e: Exception) {
                logger.error(e) { "Error sending to WebSocket ${webSocket.id}" }
            }
        }
    }

    /**
     * Enhanced prompt detection
     */
    private fun isLikelyPrompt(text: String): Boolean {
        val trimmed = text.trim()

        // Skip empty or very short strings
        if (trimmed.length < 2) return false

        // Common shell prompt patterns
        val promptPatterns = listOf(
            Regex(".*@.*:.*[$#]\\s*$"),       // user@host:path$ or user@host:path#
            Regex(".*@.*[$#]\\s*$"),          // user@host$ or user@host#
            Regex(".*[$#]\\s*$"),             // Just ends with $ or #
            Regex(".*:~[$#]\\s*$"),           // :~$ or :~#
            Regex(".*>\\s*$")                 // Windows-style > prompt
        )

        return promptPatterns.any { it.matches(trimmed) }
    }

    /**
     * Reads terminal output from SSH and broadcasts each complete line to listeners.
     * Runs in a daemon thread.
     */
    private fun startOutputReaderSimple() {
        Thread {
            val buffer = ByteArray(1024)
            val lineBuffer = StringBuilder()

            try {
                while (isConnected && inputStream != null) {
                    val bytesRead = inputStream!!.read(buffer)
                    if (bytesRead > 0) {
                        val text = String(buffer, 0, bytesRead, UTF_8)

                        text.forEach { char ->
                            when (char) {
                                '\n' -> {
                                    if (lineBuffer.isNotEmpty()) {
                                        val line = lineBuffer.toString().trimEnd('\r')
                                        handleOutputLine(line)
                                        lineBuffer.clear()
                                    }
                                }

                                '\r' -> {
                                    // Handle CR - might be followed by LF
                                }

                                else -> {
                                    lineBuffer.append(char)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (isConnected) {
                    logger.error(e) { "Error reading SSH output for session $sessionId" }
                }
            }
        }.apply {
            name = "SSH-Output-Reader-$sessionId"
            isDaemon = true
        }.start()
    }

    /**
     * Buffers and broadcasts a line of output to all active listeners.
     */
    /**
     * Buffers and broadcasts a line of output to all active listeners.
     * Handles both API mode (complete lines only) and interactive mode (with potential partials).
     */
    private fun handleOutputLine(line: String) {
        logger.debug { "[DEBUG] handleOutputLine: $line" }
        logger.info { "[SSH] Output line for session $sessionId: $line" }
        println("[DEBUG] handleOutputLine: $line")

        // Add to buffer (keep last 100 lines)
        outputBuffer.add(line)
        if (outputBuffer.size > 100) {
            outputBuffer.removeAt(0)
        }

        // Clean up closed API listeners
        val beforePruneApi = apiListeners.size
        apiListeners.removeIf {
            val closed = !it.isOpen
            if (closed) {
                println("[DEBUG] Pruning closed API listener: ${it.id}")
                logger.info { "[SSH] Pruning closed API listener: ${it.id}" }
            }
            closed
        }
        val afterPruneApi = apiListeners.size

        // Clean up closed interactive listeners
        val beforePruneInteractive = interactiveListeners.size
        interactiveListeners.removeIf {
            val closed = !it.isOpen
            if (closed) {
                println("[DEBUG] Pruning closed interactive listener: ${it.id}")
                logger.info { "[SSH] Pruning closed interactive listener: ${it.id}" }
            }
            closed
        }
        val afterPruneInteractive = interactiveListeners.size

        println("[DEBUG] API listeners: $beforePruneApi before, $afterPruneApi after pruning for session $sessionId")
        println("[DEBUG] Interactive listeners: $beforePruneInteractive before, $afterPruneInteractive after pruning for session $sessionId")
        logger.debug { "[DEBUG] API listeners: $beforePruneApi before, $afterPruneApi after pruning" }
        logger.debug { "[DEBUG] Interactive listeners: $beforePruneInteractive before, $afterPruneInteractive after pruning" }

        if (afterPruneApi == 0 && afterPruneInteractive == 0) {
            println("[DEBUG] No output listeners for session $sessionId")
            logger.warn { "[SSH] No output listeners for session $sessionId" }
            return
        }

        // Send to API listeners (complete lines only)
        apiListeners.forEach { listener ->
            try {
                println("[DEBUG] sendCompleteOutput (API): $line -> ${listener.id}")
                logger.debug { "[DEBUG] sendCompleteOutput (API): $line -> ${listener.id}" }
                logger.info { "[SSH] sendCompleteOutput (API): $line -> ${listener.id}" }
                sendCompleteOutput(listener, line)
                println("[DEBUG] sendCompleteOutput (API): DONE for ${listener.id}")
            } catch (ex: Exception) {
                println("[ERROR] sendCompleteOutput (API) failed for ${listener.id}: $ex")
                logger.error(ex) { "[SSH] sendCompleteOutput (API) failed for ${listener.id}" }
            }
        }

        // Send to interactive listeners (with potential partials - for now same as API)
        interactiveListeners.forEach { listener ->
            try {
                println("[DEBUG] sendCompleteOutput (Interactive): $line -> ${listener.id}")
                logger.debug { "[DEBUG] sendCompleteOutput (Interactive): $line -> ${listener.id}" }
                logger.info { "[SSH] sendCompleteOutput (Interactive): $line -> ${listener.id}" }
                sendCompleteOutput(listener, line)
                // TODO: Add partial update logic here later
                println("[DEBUG] sendCompleteOutput (Interactive): DONE for ${listener.id}")
            } catch (ex: Exception) {
                println("[ERROR] sendCompleteOutput (Interactive) failed for ${listener.id}: $ex")
                logger.error(ex) { "[SSH] sendCompleteOutput (Interactive) failed for ${listener.id}" }
            }
        }
    }

    /**
     * Sends a complete output line to a WebSocket listener (used for both API and interactive modes)
     */
    private fun sendCompleteOutput(webSocket: WebSocketSession, line: String) {
        synchronized(webSocket) {
            try {
                if (webSocket.isOpen) {
                    val response = RkclResponse(
                        type = "terminal_output",
                        uuid = CommandIdUtil.generateId(),
                        sessionId = sessionId,
                        output = line
                    )

                    val jsonMessage = objectMapper.writeValueAsString(response)
                    webSocket.sendMessage(TextMessage(jsonMessage))

                    logger.debug { "Sent complete output to ${webSocket.id}: '$line'" }
                } else {
                    logger.debug { "WebSocket ${webSocket.id} is closed, skipping" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error sending complete output to WebSocket ${webSocket.id}: ${e.message}" }
            }
        }
    }

    /**
     * Sends a line of output to a specific WebSocket listener as a serialized [RkclResponse].
     */
    private fun sendOutputToListener(webSocket: WebSocketSession, line: String) {
        synchronized(webSocket) {
            try {
                if (webSocket.isOpen) {
                    logger.info { "[SSH] sendOutputToListener: $line -> ${webSocket.id}" }
                    val response = RkclResponse(
                        type = "terminal_output",
                        uuid = CommandIdUtil.generateId(),
                        sessionId = sessionId,
                        output = line
                    )
                    logger.info { "[WS] Attempting to send output to session ${webSocket.id} for : $line" }
                    webSocket.sendMessage(TextMessage(objectMapper.writeValueAsString(response)))
                    logger.info {
                        "[WS] Successfully sent output to session ${webSocket.id}  ${
                            objectMapper.writeValueAsString(
                                response
                            )
                        }"
                    }
                } else {
                    logger.warn { "[WS] Listener session ${webSocket.id} is not open!" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error sending output to WebSocket: ${e.message}" }
            }
        }
    }

    /**
     * Executes a command via SSH shell, handling both control and user commands.
     * Returns a serialized [RkclResponse] for the UI/agent.
     */
    fun executeCommand(command: RkclCommand): RkclResponse {
        logger.info { "[SSH] Executing command: ${command.command} (param=${command.parameter}) " }
        if (!isConnected) {
            return RkclResponse(
                type = "error",
                uuid = command.uuid,
                sessionId = sessionId,
                success = false,
                message = "SSH session not connected"
            )
        }

        if (isPaused && !isControlCommand(command.command)) {
            return RkclResponse(
                type = "error",
                uuid = command.uuid,
                sessionId = sessionId,
                success = false,
                message = "Session is paused"
            )
        }

        return try {
            when (command.command.uppercase()) {
                "STATUS" -> handleStatus(command.uuid)
                "PING" -> handlePing(command.uuid)
                "PAUSE" -> handlePause(command.uuid)
                "RESUME" -> handleResume(command.uuid)
                else -> {
                    val bytes = translator.translateToBytes(command.command, command.parameter)
                    if (bytes.isNotEmpty()) {
                        sendToTerminal(bytes)
                        RkclResponse(
                            type = "command_result",
                            uuid = command.uuid,
                            sessionId = sessionId,
                            success = true,
                            message = "Command executed",
                            command = command.command,
                            parameter = command.parameter
                        )
                    } else {
                        RkclResponse(
                            type = "error",
                            uuid = command.uuid,
                            sessionId = sessionId,
                            success = false,
                            message = "Unknown command: ${command.command}",
                            command = command.command
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing RKCL command" }
            RkclResponse(
                type = "error",
                uuid = command.uuid,
                sessionId = sessionId,
                success = false,
                message = "Command execution failed: ${e.message}",
                command = command.command
            )
        }
    }

suspend fun executeCommandAsync(command: RkclCommand): RkclResponse {
    logger.info("[SSH] Executing async command: ${command.command} (param=${command.parameter})")

    if (!isConnected) {
        logger.warn { "[SSH] SSH session not connected" }
        return RkclResponse(
            type = "error",
            uuid = command.uuid,
            sessionId = sessionId,
            success = false,
            message = "SSH session not connected"
        )
    }

    // Handle control commands synchronously
    if (isControlCommand(command.command)) {
        logger.info { "[SSH] Executing control command: ${command.command}" }
        return executeCommand(command)
    }

    try {
        // Simple approach: send command and return immediately
        logger.info { "[SSH] Sending command to terminal: ${command.command} (param=${command.parameter})" }
        val bytes = translator.translateToBytes(command.command, command.parameter)
        sendToTerminal(bytes)

        // For now, just return success immediately
        // The workflow engine will handle actual completion tracking
        val response = RkclResponse(
            type = "command_result",
            uuid = command.uuid,
            sessionId = sessionId,
            success = true,
            message = "Command sent to terminal",
            output = "Command executed: ${command.command} ${command.parameter ?: ""}",
            command = command.command,
            parameter = command.parameter
        )

        logger.info { "[SSH] Command sent successfully: ${command.command}" }
        return response

    } catch (e: Exception) {
        logger.error(e) { "[SSH] Error executing async command: ${command.command}" }
        return RkclResponse(
            type = "error",
            uuid = command.uuid,
            sessionId = sessionId,
            success = false,
            message = "Command execution failed: ${e.message}"
        )
    }
}

    /**
     * Returns true if the command string is a control command (PAUSE, RESUME, STATUS, PING).
     */
    private fun isControlCommand(command: String): Boolean {
        return command.uppercase() in setOf("PAUSE", "RESUME", "STATUS", "PING")
    }

    /**
     * Builds a status response for the session.
     */
    /**
     * Builds a status response for the session with dual mode listener info.
     */
    private fun handleStatus(uuid: String): RkclResponse {
        return RkclResponse(
            type = "status",
            uuid = uuid,
            sessionId = sessionId,
            success = true,
            message = "Session status",
            metadata = mapOf(
                "connected" to isConnected,
                "paused" to isPaused,
                "host" to host,
                "port" to port,
                "username" to username,
                "apiListeners" to apiListeners.size,
                "interactiveListeners" to interactiveListeners.size,
                "totalListeners" to (apiListeners.size + interactiveListeners.size),
                "outputBufferSize" to outputBuffer.size
            )
        )
    }
    /**
     * Returns a basic PONG result for liveness checks.
     */
    private fun handlePing(uuid: String): RkclResponse {
        return RkclResponse(
            type = "command_result",
            uuid = uuid,
            sessionId = sessionId,
            success = true,
            message = "PONG"
        )
    }

    /**
     * Pauses the session and returns a status response.
     */
    private fun handlePause(uuid: String): RkclResponse {
        isPaused = true
        return RkclResponse(
            type = "status",
            uuid = uuid,
            sessionId = sessionId,
            success = true,
            message = "Session paused",
            metadata = mapOf("paused" to true)
        )
    }

    /**
     * Resumes a paused session and returns a status response.
     */
    private fun handleResume(uuid: String): RkclResponse {
        isPaused = false
        return RkclResponse(
            type = "status",
            uuid = uuid,
            sessionId = sessionId,
            success = true,
            message = "Session resumed",
            metadata = mapOf("paused" to false)
        )
    }

    /**
     * Sends the provided bytes to the SSH terminal output stream.
     */
    private fun sendToTerminal(bytes: ByteArray) {
        try {
            outputStream?.write(bytes)
            outputStream?.flush()
            logger.debug { "Sent ${bytes.size} bytes to terminal: ${String(bytes)}" }
        } catch (e: Exception) {
            logger.error(e) { "Error sending to terminal" }
        }
    }

    /**
     * Returns the last [lines] lines of output for this session.
     */
    fun getRecentOutput(lines: Int = 10): List<String> {
        return outputBuffer.takeLast(lines)
    }
    fun isConnected(): Boolean {
        return try {
            // Use whatever your internal session field is called
            // This might be 'session', 'sshSession', 'connection', etc.
            val sessionConnected = session?.isOpen ?: false
            return isConnected && sessionConnected // has to be marked as connected too
        } catch (e: Exception) {
            false
        }
    }


}