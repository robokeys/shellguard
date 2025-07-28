package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/WebSocketMessageQueue.kt
 * description: Single-threaded outbound queue for serializing and delivering terminal output to WebSocket clients per SSH session.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * WebSocketMessageQueue – serializes outbound terminal output to multiple WebSocket listeners per SSH session.
 *
 * This class manages a single-threaded queue for each SSH session to ensure thread-safe delivery of output
 * to all active WebSocket listeners. It prevents concurrency bugs (such as TEXT_PARTIAL_WRITING errors),
 * auto-cleans up closed sockets, and tracks queue stats for diagnostics and monitoring.
 *
 * Features:
 *  - One queue per SSH session
 *  - Single worker thread for all message deliveries (per session)
 *  - Non-blocking message enqueue (auto-drops if full)
 *  - Safe to use from multiple threads for message injection
 *  - Only outbound message type is `WebSocketOutputMessage` (use helpers for TerminalOutput)
 *  - Built-in metrics (processed, dropped, enqueued, etc)
 *
 * In future: extend to support additional message/event types if needed.
 *
 * @property sessionId       The SSH session this queue is attached to
 * @property objectMapper    Jackson mapper for JSON serialization
 * @property config          Queue configuration
 */
class WebSocketMessageQueue(
    private val sessionId: String,
    private val objectMapper: ObjectMapper,
    private val config: WebSocketQueueConfig = WebSocketQueueConfig()
) {
    private val logger = KotlinLogging.logger {}

    // Core queue and SINGLE worker thread
    private val messageQueue = LinkedBlockingQueue<WebSocketOutputMessage>(config.queueCapacity)
    private var workerThread: Thread? = null
    private val isShuttingDown = AtomicBoolean(false)

    // WebSocket tracking (no locks needed since single-threaded)
    private val trackedWebSockets = ConcurrentHashMap.newKeySet<String>()

    // Statistics
    private val messagesProcessed = AtomicLong(0)
    private val messagesDropped = AtomicLong(0)
    private val messagesEnqueued = AtomicLong(0)

    /**
     * Starts the single worker thread. Should be called once during initialization.
     */
    fun start() {
        if (workerThread != null) {
            logger.warn { "Queue worker already started for session $sessionId" }
            return
        }

        workerThread = Thread({
            logger.info { "Started SINGLE WebSocket queue worker for session $sessionId" }

            while (!isShuttingDown.get() && !Thread.currentThread().isInterrupted) {
                try {
                    // Block for up to 1 second waiting for messages
                    val message = messageQueue.poll(1000, TimeUnit.MILLISECONDS)

                    if (message != null) {
                        processMessage(message)

                        // Periodic cleanup of closed listeners
                        val processed = messagesProcessed.incrementAndGet()
                        if (processed % config.cleanupInterval == 0L) {
                            cleanupClosedListeners()
                        }
                    }

                } catch (e: InterruptedException) {
                    logger.info { "Queue worker interrupted for session $sessionId" }
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    logger.error(e) { "Error in queue worker for session $sessionId" }
                    // Continue processing other messages
                }
            }

            logger.info { "Single queue worker stopped for session $sessionId" }
        }, "WebSocket-Worker-$sessionId").apply {
            isDaemon = true
        }

        workerThread!!.start()
        logger.info { "Started single WebSocket queue worker for session $sessionId" }
    }

    fun enqueueTerminalOutput(
        webSocketId: String,
        webSocket: WebSocketSession,
        sessionId: String,
        output: String
    ): Boolean {
        val message = WebSocketOutputMessage(
            webSocketId = webSocketId,
            webSocket = webSocket,
            sessionId = sessionId,
            type = WebSocketOutputMessageType.TerminalOutput,
            output = TerminalOutputEnvelope(
                sessionId = sessionId,
                output = output
            )
        )
        return enqueueMessage(message)
    }

    // Internal, private. Used by all enqueue methods.
    private fun enqueueMessage(message: WebSocketOutputMessage): Boolean {
        if (isShuttingDown.get()) return false
        if (!trackedWebSockets.contains(message.webSocketId)) return false
        val enqueued = messageQueue.offer(message)
        if (enqueued) messagesEnqueued.incrementAndGet() else messagesDropped.incrementAndGet()
        return enqueued
    }

    /**
     * Processes a single message from the queue.
     * Since this runs in a single thread, no synchronization is needed for WebSocket calls.
     */
    private fun processMessage(message: WebSocketOutputMessage) {
        if (!trackedWebSockets.contains(message.webSocketId)) {
            logger.debug { "WebSocket ${message.webSocketId} no longer tracked, skipping message" }
            return
        }
        try {
            if (message.webSocket.isOpen && message.type == WebSocketOutputMessageType.TerminalOutput) {
                val jsonMessage = objectMapper.writeValueAsString(message.output)
                message.webSocket.sendMessage(TextMessage(jsonMessage))
                logger.debug { "[WS-Queue] Sent terminal output to ${message.webSocketId}: ${message.output?.toString()}" }
            } else {
                removeWebSocket(message.webSocketId)
            }
        } catch (e: Exception) {
            logger.warn(e) { "[WS-Queue] Error sending to ${message.webSocketId}: ${e.message}" }
            removeWebSocket(message.webSocketId)
            messagesDropped.incrementAndGet()
        }
    }

    /**
     * Registers a WebSocket for message delivery.
     *
     * @param webSocketId The WebSocket session ID
     * @return true if registered successfully, false if already registered
     */
    fun addWebSocket(webSocketId: String): Boolean {
        val added = trackedWebSockets.add(webSocketId)
        if (added) {
            logger.debug { "Registered WebSocket $webSocketId for queue delivery" }
        } else {
            logger.debug { "WebSocket $webSocketId already registered" }
        }
        return added
    }

    /**
     * Unregisters a WebSocket from message delivery.
     *
     * @param webSocketId The WebSocket session ID
     * @return true if removed, false if not found
     */
    fun removeWebSocket(webSocketId: String): Boolean {
        val removed = trackedWebSockets.remove(webSocketId)
        if (removed) {
            logger.debug { "Removed WebSocket $webSocketId from queue delivery" }
        }
        return removed
    }

    /**
     * Cleans up closed WebSocket sessions that are no longer valid.
     * This is called periodically by the worker thread.
     *
     * Note: Since we're single-threaded, we don't need to worry about
     * concurrent modification of the trackedWebSockets set.
     */
    private fun cleanupClosedListeners() {
        val currentSize = trackedWebSockets.size
        logger.debug { "Queue cleanup check for session $sessionId - tracking $currentSize WebSockets" }

        // The actual cleanup happens in processMessage() when we detect closed WebSockets
        // This method could be extended to do more thorough cleanup if needed
    }

    /**
     * Returns current queue statistics for monitoring.
     */
    fun getStatistics(): WebSocketQueueStatistics {
        return WebSocketQueueStatistics(
            queueSize = messageQueue.size,
            queueCapacity = config.queueCapacity,
            messagesEnqueued = messagesEnqueued.get(),
            messagesProcessed = messagesProcessed.get(),
            messagesDropped = messagesDropped.get(),
            activeWebSockets = trackedWebSockets.size,
            workerThreads = if (workerThread?.isAlive == true) 1 else 0,
            isRunning = !isShuttingDown.get() && workerThread?.isAlive == true
        )
    }

    /**
     * Gracefully shuts down the queue and the single worker thread.
     */
    fun shutdown() {
        logger.info { "Shutting down WebSocket queue for session $sessionId..." }
        isShuttingDown.set(true)

        // Interrupt the single worker thread
        workerThread?.interrupt()

        // Wait for worker to finish
        workerThread?.let { worker ->
            try {
                worker.join(config.shutdownTimeoutMs)
            } catch (e: InterruptedException) {
                logger.warn { "Timeout waiting for worker to stop: ${worker.name}" }
            }
        }

        // Clear resources
        workerThread = null
        messageQueue.clear()
        trackedWebSockets.clear()

        val stats = getStatistics()
        logger.info {
            "WebSocket queue shut down for session $sessionId - " +
                    "Processed: ${stats.messagesProcessed}, Dropped: ${stats.messagesDropped}"
        }
    }

    /**
     * Returns true if the queue is currently running.
     */
    fun isRunning(): Boolean = !isShuttingDown.get() && workerThread?.isAlive == true

    /**
     * Returns the current queue size (number of pending messages).
     */
    fun getQueueSize(): Int = messageQueue.size
}