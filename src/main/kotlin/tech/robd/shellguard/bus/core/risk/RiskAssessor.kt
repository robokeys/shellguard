package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/CommandRiskAssessment.kt
 * description: Interface for command risk assessors (rule-based, AI, composite, etc). Determines command risk and approval requirements.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Interface for risk assessment strategies for shell or workflow commands.
 *
 * Implementations may use hardcoded rules, regular expressions, AI models, or composite patterns to
 * classify command risk and determine if human or agent approval is required.
 */
interface RiskAssessor{
    /**
     * Analyze the risk level of the given command.
     * @param command The command CommandMessage to assess (e.g., 'rm -rf /').
     * @return The RiskAssessmentLevel A NUMBER  with a level (LOW, MEDIUM, HIGH, CRITICAL) as defined by the implementation.
     */
    fun assessRisk(command: CommandMessage): RiskAssessmentLevel

    /**
     * Determine if the given risk level requires explicit approval.
     * @param riskLevel The result from [assessRisk].
     * @return True if the command should be paused for approval, false if auto-approved.
     */
    fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean
}