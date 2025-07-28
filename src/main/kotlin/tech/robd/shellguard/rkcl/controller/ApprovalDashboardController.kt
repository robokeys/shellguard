package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/ApprovalDashboardController.kt
 * description: Controller for the human approval dashboard. Provides endpoints returning HTML fragments for HTMX and dashboard queries.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.engine.WorkflowEngine
import tech.robd.shellguard.rkcl.service.CommandHistoryService
import java.time.Instant
import kotlin.time.ExperimentalTime
// [🧩 Section: HTMX approval dashboard endpoints]
/**
 * All endpoints for dashboard/debug views, HTML fragments for HTMX, and JSON views for live stats.
 * Extend here for new dashboard widgets, richer workflow summaries, or command filtering.
 */
@RestController
@RequestMapping("/api/shellguard/engine")
class ApprovalDashboardController(
    private val workflowEngine: WorkflowEngine,
    private val sshSessionManager: tech.robd.shellguard.rkcl.service.SshSessionManager,
    private val commandHistoryService: CommandHistoryService,
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/debug-info")
    fun getDebugInfo(): ResponseEntity<String> {
        val info = """
        Workflow Engine Type: ${workflowEngine::class.simpleName}
        Is ActionWorkflowEngine: ${true}
        Engine Type: ${workflowEngine.engineType}
        Current Time: ${System.currentTimeMillis()}
    """.trimIndent()

        return ResponseEntity.ok(info)
    }

    @GetMapping("/debug-workflows")
    fun getWorkflowsDebug(): Map<String, Any> {
        logger.info { "[Dashboard-Debug] Workflow debug info requested" }

        val allWorkflows = workflowEngine.getAllWorkflows()
        return  mapOf(
                "totalWorkflows" to allWorkflows.size,
                "completed" to allWorkflows.count { it.isCompleted() },
                "pending" to allWorkflows.count { !it.isCompleted() },
                "workflows" to allWorkflows.map { w ->
                    mapOf(
                        "id" to w.actionId.take(8),
                        "command" to w.action.command,
                        "phase" to w.currentPhase.name,
                        "completed" to w.isCompleted(),
                        "eventCount" to w.events.size
                    )
                }
            )

    }

    @OptIn(ExperimentalTime::class)
    @GetMapping("/debug-workflows-detailed")
    fun getWorkflowsDetailedDebug(): Map<String, Any> {
        logger.info { "[Dashboard-Debug] Detailed workflow debug info requested" }
        val allWorkflows = workflowEngine.getAllWorkflows()
        return  mapOf(
                "totalWorkflows" to allWorkflows.size,
                "completedCount" to allWorkflows.count { it.isCompleted() },
                "workflows" to allWorkflows.map { w ->
                    mapOf(
                        "id" to w.actionId.take(8),
                        "command" to w.action.command,
                        "phase" to w.currentPhase.name,
                        "completed" to w.isCompleted(),
                        "completedAt" to w.completedAt?.toString(),
                        "createdAt" to w.createdAt.toString(),
                        "eventCount" to w.events.size,
                        "events" to w.events.map { "${it.phase.name}@${it.timestamp}" }
                    )
                }
            )

    }

    @GetMapping("/session-info", produces = ["text/html"])
    fun getSessionInfo(): String {
        val sessions = sshSessionManager.getAllSessions()

        if (sessions.isEmpty()) {
            return """
            <div class="session-info no-sessions">
                <span class="session-status disconnected">No Active SSH Sessions</span>
                <div class="session-details">Connect via test client to see session info</div>
            </div>
        """.trimIndent()
        }

        return sessions.joinToString("\n") { session ->
            """
        <div class="session-info active">
            <span class="session-status connected">Connected</span>
            <div class="session-details">
                <div class="session-detail">
                    <span class="detail-label">Host:</span>
                    <span class="detail-value">${getSessionHost(session)}:${getSessionPort(session)}</span>
                </div>
                <div class="session-detail">
                    <span class="detail-label">User:</span>
                    <span class="detail-value">${getSessionUsername(session)}</span>
                </div>
                <div class="session-detail">
                    <span class="detail-label">Session:</span>
                    <span class="detail-value">${session.sessionId}</span>
                </div>
            </div>
        </div>
        """.trimIndent()
        }
    }

    // Helper methods to extract session info (you'll need to implement these based on your ManagedSshSession)
// Replace the hardcoded helper methods with these:
    private fun getSessionHost(session: tech.robd.shellguard.rkcl.service.ManagedSshSession): String {
        return session.getHost() // Use the getter method you already have
    }

    private fun getSessionPort(session: tech.robd.shellguard.rkcl.service.ManagedSshSession): Int {
        return session.getPort() // Use the getter method you already have
    }

    private fun getSessionUsername(session: tech.robd.shellguard.rkcl.service.ManagedSshSession): String {
        return session.getUsername() // Use the getter method you already have
    }

    @OptIn(ExperimentalTime::class)
    @GetMapping("/pending-preview", produces = ["text/html"])
    fun getQueuePreview(): String {
        // Get all active workflows in queue order
        val allWorkflows = workflowEngine.getAllWorkflows()
            .filter { !it.isCompleted() }
            .sortedBy { it.createdAt } // Queue order

        if (allWorkflows.isEmpty()) {
            return """<div class="preview-placeholder">No commands in queue</div>"""
        }
        logger.info { "[Queue Preview] Total workflows: ${allWorkflows.size}, completed filtered out" }

        // Analyze queue state
        var isBlocked = false
        val queueItems = allWorkflows.take(5).mapIndexed { index, workflow ->
            val riskLevel = workflow.getRiskAssessmentLevel()?.level?.name?.lowercase() ?: "unknown"
            val riskClass = "risk-$riskLevel"
            val commandText = formatCommandForPreview(workflow.action.command, workflow.action.parameter)
            val timestamp = formatTimeForPreview(workflow.createdAt)

            // Determine queue status
            val (statusClass, statusText, explanation) = when {
                workflow.currentPhase == CommandEventPhase.PENDING_APPROVAL -> {
                    isBlocked = true
                    Triple("queue-blocking", "🚫 BLOCKING", "Waiting for approval")
                }
                isBlocked -> {
                    Triple("queue-blocked", "⏸️ BLOCKED", "Waiting for command ahead")
                }
                index == 0 && workflow.currentPhase == CommandEventPhase.APPROVED -> {
                    Triple("queue-next", "▶️ NEXT", "Ready to execute")
                }
                index == 0 && workflow.currentPhase == CommandEventPhase.EXECUTION_STARTED -> {
                    Triple("queue-executing", "⚡ EXECUTING", "Currently running")
                }
                workflow.currentPhase == CommandEventPhase.APPROVED -> {
                    Triple("queue-ready", "✅ READY", "Auto-approved")
                }
                else -> {
                    Triple("queue-waiting", "⏳ WAITING", "In processing")
                }
            }

            """
    <div class="queue-item $riskClass $statusClass" title="Command ID: ${workflow.actionId}">
        <div class="queue-header">
            <span class="queue-position">${index + 1}</span>
            <span class="queue-time">$timestamp</span>
        </div>
        <div class="queue-status-line">
            <span class="queue-status">$statusText</span>
            <span class="queue-explanation">$explanation</span>
        </div>
        <div class="queue-command">$commandText</div>
    </div>
    """.trimIndent()
        }

        // Add queue summary
        val summary = if (allWorkflows.size > 5) {
            logger.info("Queue summary: ${allWorkflows.size} total, showing 5")
            """<div class="queue-summary">... and ${allWorkflows.size - 5} more commands in queue</div>"""
        } else {
            logger.info("No queue summary: only ${allWorkflows.size} workflows")
            ""
        }

        return queueItems.joinToString("\n") + summary
    }

    @OptIn(ExperimentalTime::class)
    @GetMapping("/pending-approval-view", produces = ["text/html"])
    fun getPendingApprovalsView(): String {
        // Get both pending approval AND auto-approved (but not yet executed) workflows
        val pendingApproval = workflowEngine.getPendingApprovals()
        val autoApproved = workflowEngine.getAllWorkflows()
            .filter { workflow ->
                workflow.currentPhase == CommandEventPhase.APPROVED &&
                        workflow.getApprover() == "SYSTEM_AUTO" &&
                        !workflow.isCompleted()
            }

        val allPendingWorkflows = pendingApproval + autoApproved

        if (allPendingWorkflows.isEmpty()) {
            return """
            <div style="text-align: center; padding: 40px; color: #aaa;">
                <h3>✅ No pending actions</h3>
                <p>All AI commands are processed or completed</p>
            </div>
        """.trimIndent()
        }

        return allPendingWorkflows
            .sortedBy { it.createdAt } // Show in submission order
            .joinToString("\n") { workflow ->
                val riskLevel = workflow.getRiskAssessmentLevel()?.level?.name?.lowercase() ?: "unknown"
                val riskClass = "risk-$riskLevel"
                val commandDisplay = formatCommandForDisplay(workflow.action.command, workflow.action.parameter)
                val assessorTrail = buildAssessorTrail(workflow)

                // Determine status and available actions
                val (statusClass, statusText, availableActions) = when {
                    workflow.currentPhase == CommandEventPhase.PENDING_APPROVAL -> {
                        Triple("status-pending", "PENDING", """
                        <button class="btn btn-approve" onclick="approveCommand('${workflow.actionId}')">
                            ✅ Approve
                        </button>
                        <button class="btn btn-reject" onclick="rejectCommand('${workflow.actionId}')">
                            ❌ Reject
                        </button>
                    """.trimIndent())
                    }
                    workflow.getApprover() == "SYSTEM_AUTO" -> {
                        Triple("status-auto-approved", "AUTO-APPROVED", """
                        <button class="btn btn-reject" onclick="rejectCommand('${workflow.actionId}', 'Revoked auto-approval')">
                            ❌ Revoke & Reject
                        </button>
                        <small style="color: #4ade80; margin-left: 10px;">Will execute automatically</small>
                    """.trimIndent())
                    }
                    else -> {
                        Triple("status-approved", "APPROVED", """
                        <button class="btn btn-reject" onclick="rejectCommand('${workflow.actionId}', 'Revoked approval')">
                            ❌ Revoke & Reject
                        </button>
                        <small style="color: #22c55e; margin-left: 10px;">Will execute automatically</small>
                    """.trimIndent())
                    }
                }

                """
            <div class="command-item $riskClass">
                <div class="command-header">
                    <span class="status-indicator $statusClass">$statusText</span>
                    <small>${workflow.actionId.take(8)}...</small>
                </div>
                
                <div class="command-text">$commandDisplay</div>
                
                <div class="command-meta">
                    <div class="meta-item">
                        <div class="meta-label">Risk Level</div>
                        <div class="meta-value">${riskLevel.uppercase()}</div>
                    </div>
                    <div class="meta-item">
                        <div class="meta-label">Origin</div>
                        <div class="meta-value">${workflow.action.origin}</div>
                    </div>
                    <div class="meta-item">
                        <div class="meta-label">Session</div>
                        <div class="meta-value">${workflow.action.sessionId ?: "default"}</div>
                    </div>
                    <div class="meta-item">
                        <div class="meta-label">Submitted</div>
                        <div class="meta-value">${formatTimestamp(workflow.createdAt)}</div>
                    </div>
                </div>
                
                $assessorTrail
                
                <div class="approval-actions">
                    $availableActions
                </div>
            </div>
            """.trimIndent()
            }
    }

    @GetMapping("/command-history-json")
    fun getCommandHistoryJson(): List<Map<String, Any?>> {
        return commandHistoryService.getRecentHistory(50).map { entry ->
            mapOf(
                "actionId" to entry.actionId,
                "command" to entry.command,
                "parameter" to (entry.parameter ?: ""),
                "sessionId" to entry.sessionId,
                "origin" to entry.origin,
                "status" to entry.status.name,
                "timestamp" to entry.timestamp.toString(),
                "approvedBy" to (entry.approvedBy ?: ""),
                "rejectedBy" to (entry.rejectedBy ?: ""),
                "riskLevel" to (entry.riskAssessmentLevel ?: ""),
                "riskAssessmentLevel" to (entry.riskAssessmentLevel ?: ""),
                "success" to entry.isSuccessful(),
                "exitCode" to entry.getExitCode(),
                "durationMs" to entry.getDurationMs(),
                "formattedDuration" to entry.getFormattedDuration(),
                "displayCommand" to entry.getDisplayCommand(),
                "statusIcon" to entry.getStatusIcon()
            )
        }
    }

    @GetMapping("/command-history", produces = ["text/html"])
    fun getCommandHistoryView(): String {
        logger.info("[Dashboard] getCommandHistoryView called - using event-driven history")

        val recentHistory = commandHistoryService.getRecentHistory(20)

        if (recentHistory.isEmpty()) {
            return """
            <div style="text-align: center; color: #aaa; padding: 20px;">
                <h4>📝 No command history yet</h4>
                <p>Submit some commands to see them appear here!</p>
                <small>History is populated from workflow completion events</small>
            </div>
        """.trimIndent()
        }

        return recentHistory.joinToString("\n") { entry ->
            val statusClass = "status-${entry.status.name.lowercase()}"
            val commandDisplay = formatCommandForDisplay(entry.command, entry.parameter)
            val timeAgo = formatTimeAgo(entry.timestamp)

            """
        <div class="history-item">
            <div class="history-command">
                <strong>$commandDisplay</strong>
                <small style="color: #888; margin-left: 10px;">$timeAgo</small>
            </div>
            <div class="history-status">
                <span class="status-indicator $statusClass">${entry.status.getDisplayName()}</span>
                ${if (entry.getExitCode() != null) "<small>exit: ${entry.getExitCode()}</small>" else ""}
            </div>
        </div>
        """.trimIndent()
        }
    }

    private fun formatTimeAgo(timestamp: Instant): String {
        val now = Instant.now()
        val diff = java.time.Duration.between(timestamp, now)

        return when {
            diff.toMinutes() < 1 -> "just now"
            diff.toMinutes() < 60 -> "${diff.toMinutes()}m ago"
            diff.toHours() < 24 -> "${diff.toHours()}h ago"
            else -> "${diff.toDays()}d ago"
        }
    }
    /**
     * Format RKCL commands for human-readable display
     */
    private fun formatCommandForDisplay(command: String, parameter: String?): String {
        // Check if this is an RKCL-style command
        val rkclPattern = Regex("^(TEXT|LINE|KEY|COMBO|EDIT)[:.](.*)$", RegexOption.IGNORE_CASE)
        val match = rkclPattern.find(command)

        return if (match != null) {
            val cmd = match.groupValues[1].uppercase()
            val param = match.groupValues[2].ifEmpty { parameter }
            formatRkclCommand(cmd, param)
        } else {
            // Regular shell command
            if (parameter != null) "$command $parameter" else command
        }
    }

    /**
     * Format RKCL commands into human-readable form
     */
    private fun formatRkclCommand(command: String, parameter: String?): String {
        return when (command) {
            "TEXT" -> parameter ?: ""
            "LINE" -> "${parameter ?: ""} ⏎"
            "KEY" -> {
                val keyMap = mapOf(
                    "ENTER" to "⏎ Enter",
                    "BACKSPACE" to "⌫ Backspace",
                    "TAB" to "⇥ Tab",
                    "ESC" to "⎋ Escape",
                    "UP" to "↑ Up Arrow",
                    "DOWN" to "↓ Down Arrow",
                    "LEFT" to "← Left Arrow",
                    "RIGHT" to "→ Right Arrow",
                    "HOME" to "⇱ Home",
                    "END" to "⇲ End",
                    "DELETE" to "⌦ Delete",
                    "SPACE" to "␣ Space"
                )
                keyMap[parameter?.uppercase()] ?: "🔑 ${parameter ?: ""}"
            }
            "COMBO" -> "🔗 ${parameter?.replace("-", "+") ?: ""}"
            "EDIT" -> {
                val editMap = mapOf(
                    "cut" to "✂️ Cut",
                    "copy" to "📋 Copy",
                    "paste" to "📌 Paste",
                    "selectall" to "🔘 Select All"
                )
                editMap[parameter?.lowercase()] ?: "✏️ ${parameter ?: ""}"
            }
            else -> "$command ${parameter?.let { ": $it" } ?: ""}"
        }
    }

    /**
     * Build assessor trail showing what happened during workflow
     */
    private fun buildAssessorTrail(workflow: tech.robd.shellguard.engine.ActionWorkflow): String {
        val trail = mutableListOf<String>()

        // Risk assessment
        workflow.getRiskAssessmentLevel()?.let { risk ->
            trail.add("""
                <div class="assessor-step">
                    <span class="assessor-name">Risk Assessment</span>
                    <span class="assessor-result">${risk.level.name} / ${risk.score} %</span>
                </div>
            """.trimIndent())
        }

        // Auto-approval check
        if (workflow.requiresApproval()) {
            trail.add("""
                <div class="assessor-step">
                    <span class="assessor-name">Approval Gate</span>
                    <span class="assessor-result">REQUIRES_APPROVAL</span>
                </div>
            """.trimIndent())
        } else {
            trail.add("""
                <div class="assessor-step">
                    <span class="assessor-name">Approval Gate</span>
                    <span class="assessor-result">AUTO_APPROVED</span>
                </div>
            """.trimIndent())
        }

        return if (trail.isNotEmpty()) {
            """
            <div class="assessor-trail">
                <div class="meta-label">Assessment Trail:</div>
                ${trail.joinToString("\n")}
            </div>
            """.trimIndent()
        } else ""
    }

    /**
     * Format timestamp for display
     */
    @OptIn(ExperimentalTime::class)
    private fun formatTimestamp(instant: kotlin.time.Instant): String {
        val now = kotlin.time.Clock.System.now()
        val diff = now - instant

        return when {
            diff.inWholeMinutes < 1 -> "Just now"
            diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
            diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
            else -> "${diff.inWholeDays}d ago"
        }
    }

    /**
     * Get count of pending approvals for the badge
     */
    @GetMapping("/pending-count")
    fun getPendingCount(): Map<String, Any> {
        val pendingApproval = workflowEngine.getPendingApprovals().size
        val autoApproved = workflowEngine.getAllWorkflows()
            .count { workflow ->
                workflow.currentPhase == CommandEventPhase.APPROVED &&
                        workflow.getApprover() == "SYSTEM_AUTO" &&
                        !workflow.isCompleted()
            }

        val totalCount = pendingApproval + autoApproved

        return mapOf(
            "count" to totalCount,
            "pendingApproval" to pendingApproval,
            "autoApproved" to autoApproved,
            "hasCount" to (totalCount > 0)
        )
    }
    /**
    * Format command for preview display (more compact than full display)
    */
    private fun formatCommandForPreview(command: String, parameter: String?): String {
        // Check if this is an RKCL-style command
        val rkclPattern = Regex("^(TEXT|LINE|KEY|COMBO|EDIT)[:.](.*)$", RegexOption.IGNORE_CASE)
        val match = rkclPattern.find(command)

        return if (match != null) {
            val cmd = match.groupValues[1].uppercase()
            val param = match.groupValues[2].ifEmpty { parameter }
            formatRkclCommandForPreview(cmd, param)
        } else {
            // Regular shell command - truncate if too long
            val fullCommand = if (parameter != null) "$command $parameter" else command
            if (fullCommand.length > 60) {
                "${fullCommand.take(57)}..."
            } else {
                fullCommand
            }
        }
    }

    /**
     * Format RKCL commands for preview (shorter versions)
     */
    private fun formatRkclCommandForPreview(command: String, parameter: String?): String {
        return when (command) {
            "TEXT" -> {
                val text = parameter ?: ""
                if (text.length > 40) "${text.take(37)}..." else text
            }
            "LINE" -> {
                val text = parameter ?: ""
                val displayText = if (text.length > 35) "${text.take(32)}..." else text
                "$displayText ⏎"
            }
            "KEY" -> {
                val keyMap = mapOf(
                    "ENTER" to "⏎",
                    "BACKSPACE" to "⌫",
                    "TAB" to "⇥",
                    "ESC" to "⎋",
                    "UP" to "↑",
                    "DOWN" to "↓",
                    "LEFT" to "←",
                    "RIGHT" to "→",
                    "HOME" to "⇱",
                    "END" to "⇲",
                    "DELETE" to "⌦",
                    "SPACE" to "␣"
                )
                keyMap[parameter?.uppercase()] ?: "🔑${parameter ?: ""}"
            }
            "COMBO" -> "🔗${parameter?.replace("-", "+") ?: ""}"
            "EDIT" -> {
                val editMap = mapOf(
                    "cut" to "✂️Cut",
                    "copy" to "📋Copy",
                    "paste" to "📌Paste",
                    "selectall" to "🔘SelectAll"
                )
                editMap[parameter?.lowercase()] ?: "✏️${parameter ?: ""}"
            }
            else -> "$command${parameter?.let { ":$it" } ?: ""}"
        }
    }

    /**
     * Check if command contains non-printable characters
     */
    private fun isCommandNonPrintable(command: String): Boolean {
        // Check for control characters (except common ones like \n, \t)
        return command.any { char ->
            char.code < 32 && char != '\n' && char != '\t' && char != '\r'
        } || command.contains(Regex("\\\\x[0-9a-fA-F]{2}")) // Hex escape sequences
    }

    /**
     * Build tooltip content for non-printable commands
     */
    private fun buildCommandTooltip(workflow: tech.robd.shellguard.engine.ActionWorkflow): String {
        val command = workflow.action.command
        val parameter = workflow.action.parameter
        val riskLevel = workflow.getRiskAssessmentLevel()?.level?.name ?: "UNKNOWN"

        return """
        Raw command: ${command.replace("\n", "\\n").replace("\t", "\\t")}
        ${if (parameter != null) "Parameter: $parameter" else ""}
        Risk: $riskLevel
        Origin: ${workflow.action.origin}
        Type: ${if (command.startsWith("\u001b")) "Terminal control sequence" else "Non-printable command"}
    """.trimIndent()
    }

    /**
     * Format timestamp for preview (HH:mm:ss format)
     */
    @OptIn(ExperimentalTime::class)
    private fun formatTimeForPreview(instant: kotlin.time.Instant): String {
        // Convert to Java Instant for formatting
        val javaInstant = java.time.Instant.ofEpochSecond(instant.epochSeconds, instant.nanosecondsOfSecond.toLong())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(java.time.ZoneId.systemDefault())
        return formatter.format(javaInstant)
    }

    // [/🧩 Section: HTMX approval dashboard endpoints]


}