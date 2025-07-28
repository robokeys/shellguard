package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/EventHistoryDebugController.kt
 * description: Controller for submitting, completing, and batch-processing test commands, triggering event bus and history entries for demo/testing.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import tech.robd.shellguard.rkcl.service.CommandHistoryService
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.engine.WorkflowEngine
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

// [🧩 Section: Test/history event endpoints]
/**
 * Endpoints for driving demo/test traffic into the workflow engine and event bus, and for querying history/status.
 * Extend here for scenario testing, performance tests, or complex demo scenarios.
 */
@RestController
@RequestMapping("/api/test")
class EventHistoryDebugController(
    private val workflowEngine: WorkflowEngine,
    private val commandHistoryService: CommandHistoryService
) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/submit-test-command")
    fun submitTestCommand(
        @RequestParam(defaultValue = "echo") command: String,
        @RequestParam(defaultValue = "test message") parameter: String
    ): Map<String, Any> {
        logger.info { "[Test] Submitting test command: $command $parameter" }

        // Create and submit command
        val testCommand = CommandMessage(
            origin = "test-api",
            sessionId = "test-session",
            command = command,
            parameter = parameter
        )

        val workflow = workflowEngine.submitAction(testCommand)

        return mapOf(
            "submitted" to true,
            "actionId" to workflow.actionId,
            "command" to workflow.action.command,
            "parameter" to (workflow.action.parameter ?: ""),
            "currentPhase" to workflow.currentPhase.name,
            "isCompleted" to workflow.isCompleted(),
            "message" to "Command submitted - check history after it completes"
        )
    }

    @PostMapping("/complete-test-command/{actionId}")
    fun completeTestCommand(@PathVariable actionId: String): Map<String, Any> {
        logger.info { "[Test] Manually completing command: $actionId" }

        val workflowBefore = workflowEngine.getWorkflow(actionId)
        if (workflowBefore == null) {
            return mapOf(
                "error" to "Workflow not found",
                "actionId" to actionId
            )
        }

        // Create test result
        val testResult = CommandResult(
            uuid = actionId,
            sessionId = "test-session",
            success = true,
            message = "Test completion",
            exitCode = 0,
            stdout = "Test output for command: ${workflowBefore.action.command}",
            executionTimeMs = 150
        )

        // Complete the workflow - this should trigger history event
        workflowEngine.completeAction(actionId, testResult)

        // Check if it appeared in history
        val historyStats = commandHistoryService.getStats()
        val recentHistory = commandHistoryService.getRecentHistory(5)

        return mapOf(
            "actionId" to actionId,
            "completed" to true,
            "historyStats" to historyStats,
            "recentHistory" to recentHistory.map {
                mapOf(
                    "command" to it.getDisplayCommand(),
                    "status" to it.status.name,
                    "actionId" to it.actionId.take(8)
                )
            },
            "message" to "Command completed - should appear in history now"
        )
    }

    @PostMapping("/submit-and-complete")
    fun submitAndCompleteCommand(
        @RequestParam(defaultValue = "pwd") command: String,
        @RequestParam(defaultValue = "") parameter: String
    ): Map<String, Any> {
        logger.info { "[Test] Submit and complete: $command $parameter" }

        // Submit
        val testCommand = CommandMessage(
            origin = "test-api",
            sessionId = "test-session",
            command = command,
            parameter = parameter.ifEmpty { null }
        )

        val workflow = workflowEngine.submitAction(testCommand)
        val actionId = workflow.actionId

        // Complete immediately
        val testResult = CommandResult(
            uuid = actionId,
            sessionId = "test-session",
            success = true,
            message = "Auto-completed test",
            exitCode = 0,
            stdout = "Output from: $command ${parameter.ifEmpty { "" }}".trim(),
            executionTimeMs = 75
        )

        workflowEngine.completeAction(actionId, testResult)

        // Check history immediately
        val historyAfter = commandHistoryService.getRecentHistory(3)
        val stats = commandHistoryService.getStats()

        return mapOf(
            "actionId" to actionId,
            "workflow" to mapOf(
                "phase" to workflow.currentPhase.name,
                "completed" to workflow.isCompleted()
            ),
            "history" to mapOf(
                "totalEntries" to stats["total"],
                "recentEntries" to historyAfter.map {
                    mapOf(
                        "command" to it.getDisplayCommand(),
                        "status" to it.status.name,
                        "success" to it.isSuccessful(),
                        "timestamp" to it.timestamp.toString(),
                        "duration" to it.getFormattedDuration()
                    )
                }
            ),
            "success" to true,
            "message" to "Command submitted and completed - check /api/shellguard/engine/command-history"
        )
    }

    @PostMapping("/test-failure")
    fun testFailure(): Map<String, Any> {
        logger.info { "[Test] Testing command failure scenario" }

        val testCommand = CommandMessage(
            origin = "test-api",
            sessionId = "test-session",
            command = "test-fail",
            parameter = "simulate failure"
        )

        val workflow = workflowEngine.submitAction(testCommand)

        // Simulate failure
        workflowEngine.failAction(workflow.actionId, "Simulated test failure")

        val historyAfter = commandHistoryService.getRecentHistory(3)

        return mapOf(
            "actionId" to workflow.actionId,
            "failed" to true,
            "recentHistory" to historyAfter.map {
                mapOf(
                    "command" to it.getDisplayCommand(),
                    "status" to it.status.name,
                    "icon" to it.getStatusIcon(),
                    "errorMessage" to (it.result?.message ?: "Unknown error")
                )
            },
            "message" to "Failure test completed"
        )
    }

    @PostMapping("/test-rejection")
    fun testRejection(): Map<String, Any> {
        logger.info { "[Test] Testing command rejection scenario" }

        val testCommand = CommandMessage(
            origin = "test-api",
            sessionId = "test-session",
            command = "dangerous-command",
            parameter = "rm -rf /"
        )

        val workflow = workflowEngine.submitAction(testCommand)

        // Simulate rejection
        workflowEngine.rejectAction(workflow.actionId, "test-user", "Too dangerous for testing")

        val historyAfter = commandHistoryService.getRecentHistory(3)

        return mapOf(
            "actionId" to workflow.actionId,
            "rejected" to true,
            "recentHistory" to historyAfter.map {
                mapOf(
                    "command" to it.getDisplayCommand(),
                    "status" to it.status.name,
                    "icon" to it.getStatusIcon(),
                    "rejectedBy" to it.rejectedBy,
                    "rejectionReason" to it.rejectionReason
                )
            },
            "message" to "Rejection test completed"
        )
    }

    @GetMapping("/history-status")
    fun getHistoryStatus(): Map<String, Any> {
        val stats = commandHistoryService.getStats()
        val recent = commandHistoryService.getRecentHistory(5)

        return mapOf(
            "historyService" to "active",
            "stats" to stats,
            "recentEntries" to recent.map {
                mapOf(
                    "id" to it.actionId.take(8),
                    "command" to it.getDisplayCommand(),
                    "status" to it.status.name,
                    "icon" to it.getStatusIcon(),
                    "timestamp" to it.timestamp.toString(),
                    "duration" to it.getFormattedDuration(),
                    "successful" to it.isSuccessful()
                )
            },
            "endpoints" to mapOf(
                "viewHistory" to "/api/shellguard/engine/command-history",
                "historyJson" to "/api/shellguard/engine/command-history-json",
                "historyStats" to "/api/shellguard/engine/command-history-stats"
            )
        )
    }

    @PostMapping("/test-multiple-commands")
    fun testMultipleCommands(): Map<String, Any> {
        logger.info { "[Test] Testing multiple commands" }

        val commands = listOf(
            "echo" to "hello world",
            "pwd" to null,
            "ls" to "-la",
            "whoami" to null,
            "date" to null
        )

        val results = mutableListOf<String>()

        commands.forEach { (cmd, param) ->
            val testCommand = CommandMessage(
                origin = "test-batch",
                sessionId = "test-session",
                command = cmd,
                parameter = param
            )

            val workflow = workflowEngine.submitAction(testCommand)

            val testResult = CommandResult(
                uuid = workflow.actionId,
                sessionId = "test-session",
                success = true,
                message = "Batch test completed",
                exitCode = 0,
                stdout = "Output from: $cmd ${param ?: ""}".trim(),
                executionTimeMs = (50..200).random().toLong()
            )

            workflowEngine.completeAction(workflow.actionId, testResult)
            results.add(workflow.actionId.take(8))
        }

        val finalStats = commandHistoryService.getStats()
        val recentHistory = commandHistoryService.getRecentHistory(10)

        return mapOf(
            "commandsSubmitted" to commands.size,
            "actionIds" to results,
            "finalStats" to finalStats,
            "recentHistory" to recentHistory.map {
                mapOf(
                    "command" to it.getDisplayCommand(),
                    "status" to it.status.getDisplayName(),
                    "duration" to it.getFormattedDuration()
                )
            },
            "message" to "Batch test completed - ${commands.size} commands processed"
        )
    }
}
// [/🧩 Section: Test/history event endpoints]