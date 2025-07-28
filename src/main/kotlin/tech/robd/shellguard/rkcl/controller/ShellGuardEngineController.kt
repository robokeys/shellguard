package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/ShellGuardEngineController.kt
 * description: REST controller for workflow engine operations—submit, approve, reject, status, and detailed workflow endpoints.
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import tech.robd.shellguard.engine.WorkflowEngine
import kotlin.time.ExperimentalTime
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

// [🧩 Section: API endpoints]
/**
 * REST controller that works with both engine types:
 * - SingleActionWorkflowEngine (ActionWorkflowEngineBase)
 * - SimpleWorkflowEngine (WorkflowEngine)
 */
@RestController
@RequestMapping("/api/shellguard/engine")
class ShellGuardEngineController(
    private val workflowEngine: WorkflowEngine  // ← Now uses the interface!
) {

    @PostMapping("/submit")
    fun submitAction(
        @RequestParam action: String,
        @RequestParam(defaultValue = "ai-agent") origin: String,
        @RequestParam(defaultValue = "shellguard-session") sessionId: String
    ): ResponseEntity<ActionSubmissionResponse> {
        val commandMessage = CommandMessage(
            origin = origin,
            sessionId = sessionId,
            command = action
        )

        val workflow = workflowEngine.submitAction(commandMessage)

        val response = ActionSubmissionResponse(
            system = "shellguard Engine",
            actionId = workflow.actionId,
            action = workflow.action.command,
            currentPhase = workflow.currentPhase.name,
            riskAssessmentLevel = workflow.getRiskAssessmentLevel(),
            requiresApproval = workflow.requiresApproval(),
            status = "submitted",
            engineType = workflowEngine.engineType
        )
        return ResponseEntity.ok(response)
    }


    @PostMapping("/approve/{actionId}")
    fun approveAction(
        @PathVariable actionId: String,
        @RequestParam approvedBy: String
    ): ResponseEntity<ActionApprovalResponse> {

        val success = workflowEngine.approveAction(actionId, approvedBy)

        val response = ActionApprovalResponse(
            actionId = actionId,
            success = success,
            approvedBy = approvedBy,
            engineType = workflowEngine.engineType,
            message = if (success) "Action approved" else "Failed to approve action"
        )

        // You can set HTTP 200 or 400 depending on success if you want
        return if (success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }



    @PostMapping("/reject/{actionId}")
    fun rejectAction(
        @PathVariable actionId: String,
        @RequestParam rejectedBy: String,
        @RequestParam reason: String
    ): ResponseEntity<ActionRejectionResponse> {

        val success = workflowEngine.rejectAction(actionId, rejectedBy, reason)

        val response = ActionRejectionResponse(
            actionId = actionId,
            success = success,
            rejectedBy = rejectedBy,
            reason = reason,
            engineType = workflowEngine.engineType,
            message = if (success) "Action rejected" else "Failed to reject action"
        )

        // Choose status based on success
        return if (success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }


    @OptIn(ExperimentalTime::class)
    @GetMapping("/pending")
    fun getPendingApprovals(): ResponseEntity<PendingApprovalsResponse> {
        val pending = workflowEngine.getPendingApprovals()
        val response = PendingApprovalsResponse(
            system = "shellguard Engine",
            engineType = workflowEngine.engineType,
            count = pending.size,
            actions = pending.map { workflow ->
                PendingAction(
                    actionId = workflow.actionId,
                    action = workflow.action.command,
                    origin = workflow.action.origin,
                    sessionId = workflow.action.sessionId ?: "",
                    riskAssessmentLevel = workflow.getRiskAssessmentLevel(),
                    submittedAt = workflow.createdAt.toString()
                )
            }
        )
        return ResponseEntity.ok(response)
    }


    @OptIn(ExperimentalTime::class)
    @GetMapping("/workflow/{actionId}")
    fun getWorkflow(@PathVariable actionId: String): ResponseEntity<WorkflowResponse> {
        val workflow = workflowEngine.getWorkflow(actionId)
            ?: return ResponseEntity.notFound().build()

        val response = WorkflowResponse(
            actionId = workflow.actionId,
            action = workflow.action.command,
            origin = workflow.action.origin,
            sessionId = workflow.action.sessionId ?: "",
            currentPhase = workflow.currentPhase.name,
            isCompleted = workflow.isCompleted(),
            riskAssessmentLevel = workflow.getRiskAssessmentLevel(),
            requiresApproval = workflow.requiresApproval(),
            approvedBy = workflow.getApprover() ?: "",
            rejectedBy = workflow.getRejector() ?: "",
            rejectionReason = workflow.getRejectionReason() ?: "",
            createdAt = workflow.createdAt.toString(),
            completedAt = workflow.completedAt?.toString() ?: "",
            durationMs = workflow.getDurationMs() ?: 0,
            engineType = workflowEngine.engineType,
            events = workflow.events.map { event ->
                WorkflowEventInfo(
                    phase = event.phase.name,
                    timestamp = event.timestamp,
                    riskLevel = event.riskAssessmentLevel?.level?.name ?: "UNKNOWN",
                    approvedBy = event.approvedBy ?: "",
                    rejectedBy = event.rejectedBy ?: ""
                )
            }
        )
        return ResponseEntity.ok(response)
    }



    @GetMapping("/stats")
    fun getWorkflowStats(): ResponseEntity<WorkflowStatsResponse> {
        val stats = workflowEngine.getWorkflowStats()
        val response = WorkflowStatsResponse(
            engineType = workflowEngine.engineType,
            stats = StatsBlock(
                total = stats.total,
                active = stats.active,
                completed = stats.completed,
                pendingApproval = stats.pendingApproval,
                approved = stats.approved,
                rejected = stats.rejected,
                failed = stats.failed,
                averageDurationMs = stats.averageDurationMs
            ),
            supportsDetailedStats = true
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/info")
    fun getEngineInfo(): ResponseEntity<EngineInfoResponse> {
        val response = EngineInfoResponse(
            engineType = workflowEngine.engineType,
            implementsActionWorkflowBase = true,
            supportedOperations = listOf("submit", "approve", "reject", "pending", "workflow", "stats", "execute"),
            description = "Stateful action-based workflow engine with approval/rejection support"
        )
        return ResponseEntity.ok(response)
    }


    data class EngineInfoResponse(
        val engineType: String,
        val implementsActionWorkflowBase: Boolean,
        val supportedOperations: List<String>,
        val description: String
    )
}
// [🧩 Section: API endpoints]

// Response types for controller
data class ActionSubmissionResponse(
    val system: String,
    val actionId: String,
    val action: String,
    val currentPhase: String,
    val riskAssessmentLevel: RiskAssessmentLevel?,
    val requiresApproval: Boolean,
    val status: String,
    val engineType: String
)

data class ActionApprovalResponse(
    val actionId: String,
    val success: Boolean,
    val approvedBy: String,
    val engineType: String,
    val message: String
)

data class ActionRejectionResponse(
    val actionId: String,
    val success: Boolean,
    val rejectedBy: String,
    val reason: String,
    val engineType: String,
    val message: String
)

data class PendingAction(
    val actionId: String,
    val action: String,
    val origin: String,
    val sessionId: String,
    val riskAssessmentLevel: RiskAssessmentLevel?,
    val submittedAt: String
)

data class PendingApprovalsResponse(
    val system: String,
    val engineType: String,
    val count: Int,
    val actions: List<PendingAction>
)

data class WorkflowEventInfo(
    val phase: String,
    val timestamp: Long,
    val riskLevel: String,
    val approvedBy: String,
    val rejectedBy: String
)

data class WorkflowResponse(
    val actionId: String,
    val action: String,
    val origin: String,
    val sessionId: String,
    val currentPhase: String,
    val isCompleted: Boolean,
    val riskAssessmentLevel: RiskAssessmentLevel?,
    val requiresApproval: Boolean,
    val approvedBy: String,
    val rejectedBy: String,
    val rejectionReason: String,
    val createdAt: String,
    val completedAt: String,
    val durationMs: Long,
    val engineType: String,
    val events: List<WorkflowEventInfo>
)

data class WorkflowStatsResponse(
    val engineType: String,
    val stats: StatsBlock,
    val supportsDetailedStats: Boolean = true
)

data class StatsBlock(
    val total: Int,
    val active: Int,
    val completed: Int,
    val pendingApproval: Int,
    val approved: Int,
    val rejected: Int,
    val failed: Int,
    val averageDurationMs: Double
)