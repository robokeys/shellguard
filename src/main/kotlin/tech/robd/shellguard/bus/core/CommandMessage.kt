package tech.robd.shellguard.bus.core
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/CommandMessage.kt
 * description: Data class representing a single command event/message in the CommandBus pipeline.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import tech.robd.shellguard.engine.CommandIdUtil
import java.time.Instant
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Encapsulates all metadata for a single command submission to the [CommandBus].
 *
 * This is the core message passed through risk assessment, approval, execution, and output pipelines.
 * Additional fields support auditing, risk analysis, and full context-aware workflows.
 *
 * @property uuid             Globally unique ID for this command (auto-generated if not provided).
 * @property origin           Origin of the command (e.g., "web-client", "agent:alpha").
 * @property timestamp        Time the command was submitted.
 * @property sessionId        Optional: The execution/session context for this command.
 * @property command          The main shell, workflow, or system command to execute.
 * @property parameter        Optional: Command parameter(s), if applicable.
 * @property riskAssessmentLevel        Optional: Populated after risk assessment; can be set upstream.
 * @property requiresApproval Indicates if manual/agent approval is required.
 * @property workingDirectory Optional: Directory context for execution (if relevant).
 * @property reason           Optional: User- or system-supplied reason for running this command (for audit).
 */
// [📌 Point: Base RKCL message: Base RKCL command message that comes from client or agent]
data class CommandMessage(
    val uuid: String = CommandIdUtil.generateId(),
    val origin: String, // e.g. "web-client", "agent:alpha"
    val timestamp: Instant = Instant.now(),
    val sessionId: String?,
    val command: String,
    val parameter: String? = null,
    val riskAssessmentLevel: RiskAssessmentLevel? = null, // Added for risk assessment
    val requiresApproval: Boolean = false, // approval flow
    val workingDirectory: String? = null, // Added for context
    val reason: String? = null // For audit trail
)
