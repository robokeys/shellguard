package tech.robd.shellguard.rkcl.service
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/CommandHistoryService.kt
 * description: Tracks command execution, rejection, and failure events for the workflow engine—provides query and stats endpoints for history.
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Service
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import java.time.Instant
/**
 * [🧩 Point: Command history/event handling]
 * Subscribes to BusEvent (COMPLETED, FAILED, REJECTED), builds an in-memory history, and provides filtering/statistics.
 * Extend for persistent storage, advanced querying, or export integration.
 *
 * History is capped to 100 most recent entries (FIFO).
 */
@Service
class CommandHistoryService(
    private val eventBus: WorkflowEventBus
) {
    private val logger = KotlinLogging.logger {}

    // Store command history in memory (could be database later)
    private val commandHistory = mutableListOf<CommandHistoryEntry>()

    @PostConstruct
    fun init() {
        logger.info { "[CommandHistory] Initializing event subscriptions" }
        logger.info { "[CommandHistory] EventBus instance: ${eventBus::class.simpleName}@${eventBus.hashCode()}" }

        // Subscribe to completion events
        eventBus.subscribe(CommandEventPhase.COMPLETED) { event ->
            handleCommandCompleted(event)
        }

        eventBus.subscribe(CommandEventPhase.FAILED) { event ->
            handleCommandFailed(event)
        }

        eventBus.subscribe(CommandEventPhase.REJECTED) { event ->
            handleCommandRejected(event)
        }

        logger.info { "[CommandHistory] ✅ Subscribed to workflow completion events" }
        // Test the event bus
        logger.info { "[CommandHistory] 🧪 Testing event bus subscription..." }
        if (eventBus is tech.robd.shellguard.bus.workflow.InMemoryWorkflowEventBus) {
            val busImpl = eventBus
            logger.info { "[CommandHistory] 📊 COMPLETED listeners: ${busImpl.getListenerCount(CommandEventPhase.COMPLETED)}" }
            logger.info { "[CommandHistory] 📊 FAILED listeners: ${busImpl.getListenerCount(CommandEventPhase.FAILED)}" }
            logger.info { "[CommandHistory] 📊 REJECTED listeners: ${busImpl.getListenerCount(CommandEventPhase.REJECTED)}" }
            logger.info { "[CommandHistory] 📊 Total listeners: ${busImpl.getTotalListenerCount()}" }
        }
    }

    private fun handleCommandCompleted(event: BusEvent) {
        logger.info { "[CommandHistory] 🔥🔥🔥 RECEIVED COMPLETED EVENT for: ${event.command.command}" }
        logger.info { "[CommandHistory] 📝 Command completed: ${event.command.command}" }

        val entry = CommandHistoryEntry(
            actionId = event.command.uuid,
            command = event.command.command,
            parameter = event.command.parameter,
            sessionId = event.command.sessionId ?: "default",
            origin = event.command.origin,
            status = CommandStatus.COMPLETED,
            timestamp = Instant.ofEpochMilli(event.timestamp),
            approvedBy = event.approvedBy,
            result = event.result,
            riskAssessmentLevel = event.riskAssessmentLevel
        )

        addToHistory(entry)
    }

    private fun handleCommandFailed(event: BusEvent) {
        logger.info { "[CommandHistory] 🔥🔥🔥 RECEIVED FAILED EVENT for: ${event.command.command}" }
        logger.info { "[CommandHistory] ❌ Command failed: ${event.command.command}" }

        val entry = CommandHistoryEntry(
            actionId = event.command.uuid,
            command = event.command.command,
            parameter = event.command.parameter,
            sessionId = event.command.sessionId ?: "default",
            origin = event.command.origin,
            status = CommandStatus.FAILED,
            timestamp = Instant.ofEpochMilli(event.timestamp),
            result = event.result,
            riskAssessmentLevel = event.riskAssessmentLevel,
            errorMessage = event.result?.message
        )

        addToHistory(entry)
    }

    private fun handleCommandRejected(event: BusEvent) {
        logger.info { "[CommandHistory] 🔥🔥🔥 RECEIVED REJECTED EVENT for: ${event.command.command}" }
        logger.info { "[CommandHistory] 🚫 Command rejected: ${event.command.command}" }

        val entry = CommandHistoryEntry(
            actionId = event.command.uuid,
            command = event.command.command,
            parameter = event.command.parameter,
            sessionId = event.command.sessionId ?: "default",
            origin = event.command.origin,
            status = CommandStatus.REJECTED,
            timestamp = Instant.ofEpochMilli(event.timestamp),
            rejectedBy = event.rejectedBy,
            rejectionReason = event.rejectionReason,
            riskAssessmentLevel = event.riskAssessmentLevel
        )

        addToHistory(entry)
    }

    private fun addToHistory(entry: CommandHistoryEntry) {
        synchronized(commandHistory) {
            commandHistory.add(entry)
            // Keep only last 100 entries
            if (commandHistory.size > 100) {
                commandHistory.removeAt(0)
            }
        }
        logger.info { "[CommandHistory] ✅ Added to history: ${entry.command} (${entry.status}) - Total entries: ${commandHistory.size}" }
    }

    fun getRecentHistory(limit: Int = 20): List<CommandHistoryEntry> {
        return synchronized(commandHistory) {
            commandHistory.takeLast(limit).reversed() // Most recent first
        }
    }

    fun getHistoryByStatus(status: CommandStatus): List<CommandHistoryEntry> {
        return synchronized(commandHistory) {
            commandHistory.filter { it.status == status }
        }
    }

    fun getHistoryBySession(sessionId: String): List<CommandHistoryEntry> {
        return synchronized(commandHistory) {
            commandHistory.filter { it.sessionId == sessionId }
        }
    }

    fun getTotalCount(): Int {
        return synchronized(commandHistory) {
            commandHistory.size
        }
    }

    fun getStats(): Map<String, Int> {
        return synchronized(commandHistory) {
            val byStatus = commandHistory.groupBy { it.status }.mapValues { it.value.size }
            mapOf(
                "total" to commandHistory.size,
                "completed" to (byStatus[CommandStatus.COMPLETED] ?: 0),
                "failed" to (byStatus[CommandStatus.FAILED] ?: 0),
                "rejected" to (byStatus[CommandStatus.REJECTED] ?: 0)
            )
        }
    }

    fun clearHistory() {
        synchronized(commandHistory) {
            val oldSize = commandHistory.size
            commandHistory.clear()
            logger.info { "[CommandHistory] 🗑️ Cleared $oldSize history entries" }
        }
    }
}


enum class CommandStatus {
    COMPLETED,
    FAILED,
    REJECTED;

    fun getDisplayName(): String = name.lowercase().replace('_', ' ')
}