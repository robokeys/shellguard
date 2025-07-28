package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/LoggingReviewSink.kt
 * description: Simple CommandReviewSink that logs review, approval, and rejection events to the console.
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
 * Review sink that logs all command review/approval/rejection events to the system console.
 *
 * Useful for debugging, CI, and development—shows full approval lifecycle for submitted commands.
 */
class LoggingReviewSink : CommandReviewSink {
    /**
     * Called when a command is pending review.
     * Logs the command and its risk level.
     */
    override fun onCommandForReview(command: CommandMessage) {
        val risk = command.riskAssessmentLevel
        println("🔍 REVIEW REQUIRED: ${command.command} (Risk: ${risk?.score}/${risk?.level})")
    }

    /**
     * Called when a command has been approved.
     * Logs the approval and approver.
     */
    override fun onCommandApproved(command: CommandMessage, approvedBy: String) {
        println("✅ APPROVED by $approvedBy: ${command.command}")
    }

    /**
     * Called when a command has been rejected.
     * Logs the rejection, who rejected it, and the reason.
     */
    override fun onCommandRejected(command: CommandMessage, rejectedBy: String, reason: String) {
        println("❌ REJECTED by $rejectedBy: ${command.command} - $reason")
    }

    /**
     * Called when a command is auto-approved (typically low-risk).
     * Logs the auto-approval event.
     */
    override fun onCommandAutoApproved(command: CommandMessage) {
        println("🚀 AUTO-APPROVED: ${command.command}")
    }
}