package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/CorsConfig.kt
 * description: Spring Boot CORS configuration for development/testing UI origins. Allows cross-origin API/WebSocket access from local dev servers.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
/**
 * Configures Cross-Origin Resource Sharing (CORS) to allow browser-based UIs and agents
 * to connect to the API/WebSocket endpoints from specified localhost origins.
 *
 * **WARNING:** This configuration is only intended for local development and testing.
 * In production, restrict allowed origins and/or use a more secure setup!
 */
@Configuration
class CorsConfig {
    /**
     * Registers a [CorsFilter] bean with custom origin/method/header rules.
     *
     * @return Configured CORS filter for Spring Boot.
     */
    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(
            "http://localhost:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500"
        )
        config.allowCredentials = true
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
