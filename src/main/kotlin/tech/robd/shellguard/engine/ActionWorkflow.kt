package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/ActionWorkflow.kt
 * description: Represents the full workflow state for an AI action, including events, approval, risk, and audit data.
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */

import mu.KotlinLogging
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * [📌 Point: Workflow state and audit]
 * Tracks the complete workflow state for an AI action, including all events (submit, approval, execution, etc).
 * Expand here for additional audit fields, event tags (robokeytags v2), approval logic, or analytics.
 *
 * - `actionId`: Unique workflow/action identifier.
 * - `action`: Canonical CommandMessage (the actual command/action).
 * - `events`: All workflow events for this action.
 * - `currentPhase`: Latest phase (SUBMITTED, APPROVED, etc).
 * - `createdAt`: Workflow start time.
 * - `completedAt`: When workflow finished (if applicable).
 *
 * Methods provide access to approval status, risk, result, and audit details.
 */
data class ActionWorkflow @OptIn(ExperimentalTime::class) constructor(
    val actionId: String,
    var action: CommandMessage,
    val events: MutableList<BusEvent> = mutableListOf(),
    var currentPhase: CommandEventPhase = CommandEventPhase.SUBMITTED,
    val createdAt: Instant = Clock.System.now(),
    var completedAt: Instant? = null
) {

    private val logger = KotlinLogging.logger {}

    @OptIn(ExperimentalTime::class)
    fun addEvent(event: BusEvent) {
        logger.info("Adding event to workflow: ${event.toString()}\n ")
        events.add(event)
        currentPhase = event.phase
        if (event.phase in listOf(
                CommandEventPhase.COMPLETED,
                CommandEventPhase.FAILED,
                CommandEventPhase.REJECTED
            )) {
            logger.info("Workflow completed for action: $actionId\n")
            completedAt = Clock.System.now()
        }
        else {
            logger.info("Workflow still active! (not yet completed) for action: $actionId\n")
        }
    }

    @OptIn(ExperimentalTime::class)
    fun isCompleted(): Boolean = completedAt != null

    fun getRiskAssessmentLevel(): RiskAssessmentLevel? = events.find { it.riskAssessmentLevel != null }?.riskAssessmentLevel

    fun getApprover(): String? = events.find { it.approvedBy != null }?.approvedBy

    fun getRejector(): String? = events.find { it.rejectedBy != null }?.rejectedBy

    fun getRejectionReason(): String? = events.find { it.rejectionReason != null }?.rejectionReason

    fun getExecutionResult(): CommandResult? = events.find { it.result != null }?.result

    fun requiresApproval(): Boolean = events.find { it.requiresApproval != null }?.requiresApproval ?: false

    @OptIn(ExperimentalTime::class)
    fun getDurationMs(): Long? = completedAt?.let {
        it.toEpochMilliseconds() - createdAt.toEpochMilliseconds()
    }

    override fun toString(): String {
        return "ActionWorkflow(id=$actionId, phase=$currentPhase, action='${action.command}')"
    }
}
