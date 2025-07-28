package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/WebSocketOutputMessage.kt
 * description: Envelope for outbound messages queued to WebSocket clients in RKCL. Supports typed payloads (e.g., terminal output).
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.web.socket.WebSocketSession

/**
 * Envelope for a message to be sent to a WebSocket client in RKCL.
 *
 * Designed for outbound queueing and thread-safe delivery via a single-worker queue per session.
 * Supports type discrimination (`type`) for future extensibility—currently only TerminalOutput.
 *
 * @property webSocketId   Unique ID for the WebSocket session
 * @property webSocket     Spring WebSocket session instance (target)
 * @property sessionId     RKCL SSH session ID this output belongs to
 * @property type          Type of the message (enum, for future extension)
 * @property output        Terminal output payload (can extend for other types in future)
 * @property timestamp     Message enqueue time (ms since epoch)
 */
data class WebSocketOutputMessage(
    val webSocketId: String,
    val webSocket: WebSocketSession,
    val sessionId: String,
    val type: WebSocketOutputMessageType = WebSocketOutputMessageType.TerminalOutput,
    val output: TerminalOutputEnvelope? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class WebSocketOutputMessageType {
    TerminalOutput,

}