// src/main/kotlin/tech/robd/rkcl/controller/RkclController.kt
package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/RkclController.kt
 * description: REST controller for RKCL API. Manages SSH sessions, command execution, and basic health checks.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.robd.shellguard.rkcl.model.RkclCommand
import tech.robd.shellguard.rkcl.model.RkclResponse
import tech.robd.shellguard.rkcl.model.SessionCreateRequest
import tech.robd.shellguard.rkcl.service.SshSessionManager
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * REST controller exposing RKCL backend functionality for managing SSH sessions and remote command execution.
 *
 * Endpoints:
 * - `POST /api/rkcl/sessions`: Create a new SSH session.
 * - `GET /api/rkcl/sessions`: List all active sessions.
 * - `DELETE /api/rkcl/sessions/{sessionId}`: Remove an active session.
 * - `POST /api/rkcl/sessions/{sessionId}/execute`: Execute a command in a session.
 *
 * @property sshSessionManager Service for session management and command execution.
 */
@RestController
@RequestMapping("/api/rkcl")
class RkclController(
    private val sshSessionManager: SshSessionManager
) {
    /**
     * Create a new SSH session with the given request details.
     *
     * @param request The session creation parameters (host, user, password, etc).
     * @return JSON with success flag, new session ID, and a status message.
     */
    @PostMapping("/sessions")
    fun createSession(@RequestBody request: SessionCreateRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val sessionId = sshSessionManager.createSession(request)
            ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "sessionId" to sessionId,
                "message" to "Session created successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf<String, Any>(
                "success" to false,
                "error" to (e.message ?: "Unknown error")
            ))
        }
    }

    /**
     * List all currently active SSH sessions.
     *
     * @return JSON array of sessions, each with session ID and connection status.
     */
    @GetMapping("/sessions")
    fun listSessions(): ResponseEntity<Map<String, Any>> {
        val sessions = sshSessionManager.getAllSessions().map { session ->
            mapOf(
                "sessionId" to session.sessionId,
                "connected" to true
            )
        }
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "sessions" to sessions
        ))
    }

    /**
     * Delete (close) a session by its ID.
     *
     * @param sessionId The session identifier to remove.
     * @return JSON indicating success and message.
     */
    @DeleteMapping("/sessions/{sessionId}")
    fun deleteSession(@PathVariable sessionId: String): ResponseEntity<Map<String, Any>> {
        sshSessionManager.removeSession(sessionId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Session deleted"
        ))
    }

    /**
     * Execute a command in a given SSH session.
     *
     * @param sessionId The session to run the command in.
     * @param command   The command to execute (command string, parameters, etc).
     * @return [RkclResponse] with output, status, and any error info.
     */
    @PostMapping("/sessions/{sessionId}/execute")
    fun executeCommand(
        @PathVariable sessionId: String,
        @RequestBody command: RkclCommand
    ): ResponseEntity<RkclResponse> {
        val response = sshSessionManager.executeCommand(sessionId, command.copy(sessionId = sessionId))
        return ResponseEntity.ok(response)
    }

}

