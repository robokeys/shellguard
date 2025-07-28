package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/DebugController.kt
 * description: REST controller for debugging infrastructure—provides endpoints for SSH sessions and execution sink status.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.robd.shellguard.bus.sinks.ShellGuardAwareExecutionSink
import tech.robd.shellguard.rkcl.service.SshSessionManager
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

// [🧩 Section: Debug/infrastructure endpoints]

/**
 * Endpoints for inspecting and debugging infrastructure: execution sinks and SSH sessions.
 * Expand here for additional diagnostics, stats, or operational dashboards.
 */
@RestController
@RequestMapping("/api/debug/infrastructure")
class DebugController(
    private val sshSessionManager: SshSessionManager,
    private val applicationContext: org.springframework.context.ApplicationContext,
)  {

    @GetMapping("/debug/execution-sink")
    fun getExecutionSinkDebug(): Map<String, Any?> {
        return try {
            val sink = applicationContext.getBean(ShellGuardAwareExecutionSink::class.java)
            mapOf(
                "sinkFound" to true,
                "sinkClass" to sink::class.simpleName,
                "stats" to sink.getExecutionStats()  // This method needs to exist in the sink
            )
        } catch (e: Exception) {
            mapOf(
                "sinkFound" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }

    @GetMapping("/debug/ssh-sessions")
    fun getSshSessionsDebug(): Map<String, Any> {
        val sessions = sshSessionManager.getAllSessions()
        return mapOf(
            "sessionCount" to sessions.size,
            "sessions" to sessions.map { session ->
                mapOf(
                    "sessionId" to session.sessionId,
                    "connected" to session.isConnected(),
                    "host" to session.getHost(),
                    "port" to session.getPort(),
                    "username" to session.getUsername()
                )
            }
        )
    }
}
// [/🧩 Section: Debug/infrastructure endpoints]
