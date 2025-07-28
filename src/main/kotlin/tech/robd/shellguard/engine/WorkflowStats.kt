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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
