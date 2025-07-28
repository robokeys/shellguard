package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/HealthController.kt
 * description: REST controller for health checks and readiness endpoints, especially for WebSocket configuration.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tech.robd.shellguard.rkcl.service.SshSessionManager
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Simple health check controller for confirming WebSocket setup and endpoint availability.
 *
 * Can be used for readiness/liveness checks, UI sanity tests, or dev debugging.
 *
 * @property sshSessionManager Service for session management and command execution.
 */
@RestController
class HealthController(
    private val sshSessionManager: SshSessionManager
)  {
    /**
     * Returns a JSON map indicating that the WebSocket subsystem is configured and which endpoint is active.
     *
     * Example response:
     * ```
     * {
     *   "websocket": "configured",
     *   "endpoint": "/terminal"
     * }
     * ```
     */
    @GetMapping("/health/websocket")
    fun websocketHealth(): Map<String, String> {
        return mapOf("websocket" to "configured", "endpoint" to "/terminal")
    }

    /**
     * Basic health and info endpoint for the RKCL service.
     *
     * @return Service health status, version, and active session count.
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "healthy",
            "version" to "0.1.0",
            "activeSessions" to sshSessionManager.getAllSessions().size
        ))
    }
}