/**
 * [File Info]
 * path: tech/robd/shellguard/engine/WorkflowStats.kt
 * description: Data class holding statistics for workflow engine monitoring, dashboards, and reporting.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
package tech.robd.shellguard.engine

/**
 * [📌 Point: Dashboard/monitoring stats]
 * Metrics for workflow engine monitoring and dashboards.
 * Expand here for new counters, histograms, percentiles, error codes, etc.
 *
 * @property total           Total number of workflows seen by the engine
 * @property active          Number of workflows currently active (not completed/rejected/failed)
 * @property completed       Number of completed workflows
 * @property pendingApproval Number of workflows currently pending approval
 * @property approved        Number of workflows approved
 * @property rejected        Number of workflows rejected
 * @property failed          Number of workflows failed (errors/exceptions)
 * @property averageDurationMs  Mean duration (ms) for completed workflows
 */
data class WorkflowStats(
    val total: Int,
    val active: Int,
    val completed: Int,
    val pendingApproval: Int,
    val approved: Int,
    val rejected: Int,
    val failed: Int,
    val averageDurationMs: Double
)
