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