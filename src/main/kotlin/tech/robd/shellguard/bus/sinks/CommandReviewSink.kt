package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/CommandReviewSink.kt
 * description: Interface for handling review, approval, and rejection events in the CommandBus pipeline.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage


/**
 * Sink for review-related events in the [tech.robd.shellguard.bus.core.CommandBus] approval workflow.
 *
 * Allows external systems (UIs, logs, bots, dashboards) to receive notifications when:
 *   - A command is pending review,
 *   - Is approved or rejected by a human or agent,
 *   - Or is auto-approved due to low risk.
 */
interface CommandReviewSink {
    /**
     * Called when a command requires review and is pending approval.
     * @param command The [tech.robd.shellguard.bus.core.CommandMessage] that needs human/agent review.
     */
    fun onCommandForReview(command: CommandMessage)

    /**
     * Called when a command has been approved.
     * @param command    The approved [CommandMessage].
     * @param approvedBy The user/agent who approved it.
     */
    fun onCommandApproved(command: CommandMessage, approvedBy: String)

    /**
     * Called when a command is rejected.
     * @param command     The rejected [CommandMessage].
     * @param rejectedBy  The user/agent who rejected it.
     * @param reason      Human-readable reason for rejection.
     */
    fun onCommandRejected(command: CommandMessage, rejectedBy: String, reason: String)

    /**
     * Called when a command is auto-approved (e.g., low risk, does not require review).
     * @param command The auto-approved [CommandMessage].
     */
    fun onCommandAutoApproved(command: CommandMessage) // For low-risk auto-approvals

}