package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/ReviewSinkAdapter.kt
 * description: Adapter converting shellguard BusEvents into CommandReviewSink review, approval, and rejection calls.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.sinks.CommandReviewSink
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Adapter that converts shellguard BusEvents to CommandReviewSink calls
 */
class ReviewSinkAdapter(
    private val sink: CommandReviewSink
) {
    // [🧩 Section: Review Phase Handling]
    // Handles mapping BusEvents to review, approval, and rejection actions.
    // Expand here to support additional review-related phases or richer event data.
    fun handleEvent(event: BusEvent) {
        when (event.phase) {
            CommandEventPhase.PENDING_APPROVAL -> {
                sink.onCommandForReview(event.command)
            }

            CommandEventPhase.APPROVED -> {
                val approvedBy = event.approvedBy ?: "unknown"
                sink.onCommandApproved(event.command, approvedBy)
            }

            CommandEventPhase.REJECTED -> {
                val rejectedBy = event.rejectedBy ?: "unknown"
                val reason = event.rejectionReason ?: "No reason provided"
                sink.onCommandRejected(event.command, rejectedBy, reason)
            }

            CommandEventPhase.RISK_ASSESSED -> {
                // Auto-approved if no approval required
                if (event.requiresApproval == false) {
                    sink.onCommandAutoApproved(event.command)
                }
            }

            else -> {
                // Ignore other phases - no review sink action needed
            }
        }
    }
    // [/🧩 Section: Review Phase Handling]
}
