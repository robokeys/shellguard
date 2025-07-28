package tech.robd.shellguard.rkcl.service
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/CommandHistoryEntry.kt
 * description: Represents a single entry in the command history log, including status, result, duration, and formatting helpers.
 * license: GPL-3.0 * generator: human
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.risk.RiskAssessmentLevel
import java.time.Instant

/**
 * Data structure and helpers for displaying and analyzing workflow command history.
 * Expand for additional analytics fields, richer UI formatting, or audit/event metadata.
 *
 * Includes factory for conversion from BusEvent, plus output and status formatting helpers.
 */
data class CommandHistoryEntry(
    val actionId: String,
    val command: String,
    val parameter: String? = null,
    val sessionId: String,
    val origin: String,
    val status: CommandStatus,
    val timestamp: Instant,
    val approvedBy: String? = null,
    val rejectedBy: String? = null,
    val rejectionReason: String? = null,
    val result: CommandResult? = null,
    val riskAssessmentLevel: RiskAssessmentLevel?  = null,
    val executionTimeMs: Long? = null,
    val errorMessage: String? = null
) {

    // History-specific computed properties
    fun isSuccessful(): Boolean = status == CommandStatus.COMPLETED && result?.success == true

    fun getDisplayCommand(): String = if (parameter != null) "$command $parameter" else command

    fun getExitCode(): Int? = result?.exitCode

    fun getOutputPreview(maxLength: Int = 100): String? {
        return result?.stdout?.let { output ->
            if (output.length <= maxLength) output
            else "${output.take(maxLength)}..."
        }
    }

    fun getStatusIcon(): String = when (status) {
        CommandStatus.COMPLETED -> if (isSuccessful()) "✅" else "⚠️"
        CommandStatus.FAILED -> "❌"
        CommandStatus.REJECTED -> "🚫"
    }

    fun getFormattedDuration(): String {
        return executionTimeMs?.let { ms ->
            when {
                ms < 1000 -> "${ms}ms"
                ms < 60000 -> {
                    val seconds = ms / 1000.0
                    "%.1fs".format(seconds).replace(".0s", "s") // Remove .0 for whole seconds
                }
                else -> {
                    val minutes = ms / 60000
                    val remainingSeconds = (ms % 60000) / 1000
                    "${minutes}m ${remainingSeconds}s"
                }
            }
        } ?: "-"
    }

    fun getDurationMs(): Long? = result?.executionTimeMs

    companion object {
        // Factory method to create from BusEvent
        fun fromBusEvent(event: BusEvent): CommandHistoryEntry {
            val status = when (event.phase) {
                CommandEventPhase.COMPLETED -> CommandStatus.COMPLETED
                CommandEventPhase.FAILED -> CommandStatus.FAILED
                CommandEventPhase.REJECTED -> CommandStatus.REJECTED
                else -> throw IllegalArgumentException("Cannot create history entry from phase: ${event.phase}")
            }

            return CommandHistoryEntry(
                actionId = event.command.uuid,
                command = event.command.command,
                parameter = event.command.parameter,
                sessionId = event.command.sessionId ?: "default",
                origin = event.command.origin,
                status = status,
                timestamp = Instant.ofEpochMilli(event.timestamp),
                approvedBy = event.approvedBy,
                rejectedBy = event.rejectedBy,
                rejectionReason = event.rejectionReason,
                result = event.result,
                riskAssessmentLevel = event.riskAssessmentLevel,
                executionTimeMs = event.result?.executionTimeMs
            )
        }
    }
}

//enum class CommandStatus {
//    COMPLETED,
//    FAILED,
//    REJECTED;
//
//    fun getDisplayName(): String = name.lowercase().replace('_', ' ')
//}