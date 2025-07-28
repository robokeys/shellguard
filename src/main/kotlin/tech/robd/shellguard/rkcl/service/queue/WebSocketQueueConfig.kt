package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/WebSocketQueueConfig.kt
 * description: Configuration class for per-session WebSocket message queue—controls queue size, cleanup, and shutdown.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * Configuration parameters for the WebSocket message queue.
 *
 * @property queueCapacity      Maximum number of outbound messages buffered in memory.
 * @property cleanupInterval    How often (in messages) to check for and remove closed WebSocket listeners.
 * @property shutdownTimeoutMs  Max time to wait for the worker thread to shut down gracefully (ms).
 */
data class WebSocketQueueConfig(
    val queueCapacity: Int = 10000,
    val cleanupInterval: Long = 50L, // Clean up closed listeners every N messages
    val shutdownTimeoutMs: Long = 2000L
)