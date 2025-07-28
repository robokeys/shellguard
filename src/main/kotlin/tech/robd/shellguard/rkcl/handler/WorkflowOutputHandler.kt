package tech.robd.shellguard.rkcl.handler
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/handler/WorkflowOutputHandler.kt
 * description: Handles workflow OUTPUT events, routing terminal output to all registered approval dashboards (WebSocket).
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import tech.robd.shellguard.bus.core.TerminalOutput
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import tech.robd.shellguard.rkcl.model.RkclResponse
import com.fasterxml.jackson.databind.ObjectMapper
import tech.robd.shellguard.engine.CommandUuidGenerator
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
@Component
class WorkflowOutputHandler(
    private val eventBus: WorkflowEventBus,
    private val objectMapper: ObjectMapper,
    private val commandUuidGenerator: CommandUuidGenerator
) {
    private val approvalDashboards = mutableSetOf<WebSocketSession>()

    init {
        // [🧩 Point: workflow output subscribe: Subscribe to OUTPUT events from workflow]
        eventBus.subscribe(CommandEventPhase.OUTPUT) { event ->
            sendToApprovalDashboards(event.output!!)
        }
    }

    fun addApprovalDashboard(session: WebSocketSession) {
        approvalDashboards.add(session)
    }

    private fun sendToApprovalDashboards(output: TerminalOutput) {
        approvalDashboards.removeIf { !it.isOpen }
        approvalDashboards.forEach { session ->
            // Send to approval dashboard
            val response = RkclResponse(
                type = "terminal_output",
                uuid = commandUuidGenerator.generateId(),
                sessionId = output.sessionId,
                output = output.output
            )
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(response)))
        }
    }
}