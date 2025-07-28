package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/BusEvent.kt
 * description: Represents a single event/step in the command workflow pipeline (submit, approve, execute, etc.).
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.TerminalOutput
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

// [📌 Section: BusEvent payload]
/**
* Core event message for the workflow bus—represents one step (submit, approve, execute, reject, output).
* Expand with additional fields for audit, policy tags, robokeytags v2 event metadata, or system integration.
*
* @property phase               The workflow phase (submit, approve, execute, etc).
* @property command             The command being processed.
* @property result              The result of command execution (if available).
* @property riskAssessmentLevel The assessed risk at this stage.
* @property requiresApproval    Whether approval was needed for this event.
* @property approvedBy          Who approved the command (if any).
* @property rejectedBy          Who rejected the command (if any).
* @property rejectionReason     Reason for rejection (if any).
* @property output              Any terminal output produced by this event.
* @property timestamp           Event time (epoch millis).
*
* Example expansion:
*   - Add `tags: List<EventTag>?` to support robokeytags v2.
*   - Add `originService: String?` for distributed systems.
*/
data class BusEvent(
    val phase: CommandEventPhase,
    val command: CommandMessage,
    val result: CommandResult? = null,
    val riskAssessmentLevel: RiskAssessmentLevel? = null,
    val requiresApproval: Boolean? = null,
    val approvedBy: String? = null,
    val rejectedBy: String? = null,
    val rejectionReason: String? = null,
    val output: TerminalOutput? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "BusEvent(phase=$phase, command='${command.command}', session=${command.sessionId})"
    }
}
// [/📌 Section: BusEvent payload]
