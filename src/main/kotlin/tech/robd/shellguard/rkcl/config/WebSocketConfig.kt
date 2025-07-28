// src/main/kotlin/tech/robd/rkcl/config/WebSocketConfig.kt
package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/WebSocketConfig.kt
 * description: Spring WebSocket configuration for RKCL. Registers WebSocket handlers and allowed frontend origins for agent/UI communication.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import tech.robd.shellguard.rkcl.handler.RkclTerminalHandler

/**
 * Spring WebSocket configuration for RKCL.
 *
 * Registers the main [RkclTerminalHandler] at `/terminal` and specifies which frontend origins are allowed
 * to connect (typically your local dev UIs). Extend this list as needed for additional UIs or test agents.
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val rkclTerminalHandler: RkclTerminalHandler
) : WebSocketConfigurer {

    /**
     * Registers the [RkclTerminalHandler] at `/terminal` and configures allowed origins.
     *
     * @param registry Spring's WebSocket handler registry.
     */
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(rkclTerminalHandler, "/terminal")
            .setAllowedOrigins(
                "http://localhost:5173",
                "http://localhost:5500",
                "http://127.0.0.1:5500"
                // Add any additional frontend origins as needed
            )
    }
}
