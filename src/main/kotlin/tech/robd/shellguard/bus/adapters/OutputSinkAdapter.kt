package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/OutputSinkAdapter.kt
 * description: Adapter converting shellguard BusEvents into TerminalOutputSink messages, bridging workflow and terminal output.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */

import tech.robd.shellguard.bus.core.TerminalOutput
import tech.robd.shellguard.bus.sinks.TerminalOutputSink
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase

/**
 * Adapter that converts shellguard BusEvents to TerminalOutputSink calls
 */
class OutputSinkAdapter(
    private val sink: TerminalOutputSink
) {
    // [🧩 Section: BusEvent Phase Handling]
    // Handles event-to-output mapping for each workflow phase.
    // Expand here for custom output formatting or new event phases.
    fun handleEvent(event: BusEvent) {
        when (event.phase) {
            CommandEventPhase.OUTPUT -> {
                // Direct terminal output
                event.output?.let { output ->
                    sink.onTerminalOutput(output)
                }
            }

            // Generate status messages for workflow events
            CommandEventPhase.SUBMITTED -> {
                emitMessage(event, "Action '${event.command.command}' submitted for processing")
            }

            CommandEventPhase.PENDING_APPROVAL -> {
                val riskMsg = event.riskAssessmentLevel?.let { " (Risk: ${it.level.name})" } ?: ""
                emitMessage(event, "Action pending approval$riskMsg")
            }

            CommandEventPhase.APPROVED -> {
                val approver = event.approvedBy ?: "unknown"
                emitMessage(event, "Action approved by $approver. Executing...")
            }

            CommandEventPhase.REJECTED -> {
                val rejector = event.rejectedBy ?: "unknown"
                val reason = event.rejectionReason ?: "No reason provided"
                emitMessage(event, "Action rejected by $rejector: $reason")
            }

            CommandEventPhase.COMPLETED -> {
                val result = event.result
                val message = if (result?.success == true) {
                    buildString {
                        appendLine("Action completed successfully (${result.executionTimeMs ?: 0}ms)")
                        if (!result.stdout.isNullOrEmpty()) {
                            appendLine("--- OUTPUT ---")
                            append(result.stdout)
                        }
                    }
                } else {
                    buildString {
                        appendLine("Action failed (exit code: ${result?.exitCode ?: -1})")
                        if (!result?.stderr.isNullOrEmpty()) {
                            appendLine("--- ERROR ---")
                            append(result?.stderr)
                        }
                    }
                }
                emitMessage(event, message)
            }

            CommandEventPhase.FAILED -> {
                val error = event.result?.message ?: "Unknown error"
                emitMessage(event, "Action execution failed: $error")
            }

            else -> {
                // Ignore other phases
            }
        }
    }
    // [/🧩 Section: BusEvent Phase Handling]

    // [🧩 Section: OutputFormatting]
    // This helper centralizes output message emission.
    // Future: Expand for richer formatting, markdown, or localization.
    private fun emitMessage(event: BusEvent, message: String) {
        val output = TerminalOutput(
            sessionId = event.command.sessionId,
            output = message
        )
        sink.onTerminalOutput(output)
    }
    // [/🧩 Section: OutputFormatting]
}
