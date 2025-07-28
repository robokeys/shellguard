package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/EventBusTestController.kt
 * description: REST controller for testing and debugging the workflow event bus. Supports firing test events and viewing event logs.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.web.bind.annotation.*
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.EventBusTestListener
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

// [🧩 Section: Event bus debug/test endpoints]
/**
 * Simple endpoints for manual event bus testing.
 * Use to inject/fake events or inspect the current received event log.
 * Extend here for more granular test events, event payloads, or history inspection.
 */
@RestController
@RequestMapping("/api/test/events")
class EventBusTestController(
    private val eventBus: WorkflowEventBus,
    private val testListener: EventBusTestListener
) {

    @PostMapping("/fire")
    fun fireTestEvent(@RequestParam phase: String): String {
        val commandMessage = CommandMessage(
            origin = "test",
            sessionId = "test-session",
            command = "echo test"
        )

        val eventPhase = try {
            CommandEventPhase.valueOf(phase.uppercase())
        } catch (e: IllegalArgumentException) {
            return "Invalid phase: $phase. Valid phases: ${CommandEventPhase.entries.joinToString()}"
        }

        val event = BusEvent(
            phase = eventPhase,
            command = commandMessage,
            riskAssessmentLevel = RiskAssessmentLevel(20)
        )

        eventBus.publish(event)

        return "Fired event: $event"
    }

    @GetMapping("/received")
    fun getReceivedEvents(): Map<String, Any> {
        return mapOf(
            "count" to testListener.getEventCount(),
            "events" to testListener.getReceivedEvents().map { event ->
                mapOf(
                    "phase" to event.phase.name,
                    "command" to event.command.command,
                    "sessionId" to event.command.sessionId,
                    "timestamp" to event.timestamp
                )
            }
        )
    }

    @PostMapping("/clear")
    fun clearEvents(): String {
        testListener.clearEvents()
        return "Cleared all received events"
    }
}
// [/🧩 Section: Event bus debug/test endpoints]