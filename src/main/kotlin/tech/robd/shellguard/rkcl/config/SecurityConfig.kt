// src/main/kotlin/tech/robd/rkcl/config/SecurityConfig.kt
package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/SecurityConfig.kt
 * description: Spring Security configuration for RKCL APIs and WebSocket endpoints. Permissive by default for development; stricter config is commented for production.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Configures security filters for RKCL Spring Boot APIs and WebSocket endpoints.
 *
 * - By default, *all* requests are permitted (suitable for local dev and trusted networks).
 * - CSRF is disabled for compatibility with WebSocket and local UI dev.
 * - A stricter production-ready config is provided (commented) for future hardening.
 *
 * **IMPORTANT:** Do not use the fully-permissive setup in untrusted production environments!
 * At least Uncomment and adapt the stricter version for secure deployments.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {
    /**
     * Permissive filter chain for all endpoints. Disables CSRF and allows any request.
     * Intended for development and local testing only!
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
        return http.build()
    }
//    @Bean
//    fun filterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .csrf { it.disable() }
//            .authorizeHttpRequests { auth ->
//                auth
//                    .requestMatchers("/api/rkcl/health").permitAll()
//                    .requestMatchers("/rkcl-terminal/**").permitAll() // WebSocket endpoint
//                    .requestMatchers("/api/rkcl/**").permitAll() // For now, allow all API access
//                    .anyRequest().authenticated()
//            }
//        return http.build()
//    }
}