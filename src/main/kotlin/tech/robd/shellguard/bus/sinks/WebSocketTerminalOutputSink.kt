package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/sinks/WebSocketTerminalOutputSink.kt
 * description: TerminalOutputSink that routes output to WebSocket sessions, using a sessionId-based lookup.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import tech.robd.shellguard.bus.core.TerminalOutput

/**
 * [📌 Point: WebSocket terminal output sink]
 * Sends TerminalOutput to the corresponding WebSocket session, using a lookup function.
 * Expand for advanced routing, broadcasting, or richer output formatting.
 *
 * @param sessionLookup Function to map sessionId to a live WebSocketSession.
 */
class WebSocketTerminalOutputSink(
    private val sessionLookup: (String?) -> WebSocketSession?
) : TerminalOutputSink {

    private val logger = KotlinLogging.logger {}
    override fun onTerminalOutput(output: TerminalOutput) {
        val ws = sessionLookup(output.sessionId)
        logger.info(
            "[WS-SINK] onTerminalOutput called for sessionId=${output.sessionId} (has session? ${ws != null}) output='${
                output.output.take(
                    100
                )
            }'"
        )
        ws?.let {
            if (it.isOpen) {
                logger.info("[WS-SINK] Sending message to sessionId=${output.sessionId} via WebSocket")
                it.sendMessage(TextMessage(output.output))
                logger.info("[WS-SINK] Successfully sent output to sessionId=${output.sessionId}")
            } else {
                logger.info("[WS-SINK] WebSocket session for ${output.sessionId} is not open!")

            }
        } ?: logger.info("[WS-SINK] No WebSocket session found for sessionId=${output.sessionId}")
    }
}