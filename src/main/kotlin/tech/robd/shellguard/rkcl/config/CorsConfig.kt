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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
