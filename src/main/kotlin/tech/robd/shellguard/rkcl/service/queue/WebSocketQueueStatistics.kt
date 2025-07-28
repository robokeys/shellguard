package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/WebSocketQueueStatistics.kt
 * description: Runtime statistics and health snapshot for a per-session WebSocket message queue. Used for diagnostics, metrics, and monitoring.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Statistics and health metrics for a WebSocket message queue instance.
 *
 * Provides queue utilization, drop rate, and current delivery state for diagnostics and monitoring.
 *
 * @property queueSize         Number of pending messages in the queue.
 * @property queueCapacity     Maximum buffer size of the queue.
 * @property messagesEnqueued  Total number of messages enqueued since startup.
 * @property messagesProcessed Total messages successfully delivered.
 * @property messagesDropped   Total messages dropped due to queue overflow or socket failure.
 * @property activeWebSockets  Count of currently tracked/connected WebSocket clients.
 * @property workerThreads     Number of active worker threads (always 0 or 1 per session).
 * @property isRunning         Whether the queue is currently running/servicing messages.
 * @property queueUtilization  Ratio (0.0–1.0) of queue fullness.
 * @property dropRate          Ratio of dropped to enqueued messages for alerting.
 */
data class WebSocketQueueStatistics(
    val queueSize: Int,
    val queueCapacity: Int,
    val messagesEnqueued: Long,
    val messagesProcessed: Long,
    val messagesDropped: Long,
    val activeWebSockets: Int,
    val workerThreads: Int, // Will always be 0 or 1
    val isRunning: Boolean
) {
    val queueUtilization: Double = if (queueCapacity > 0) queueSize.toDouble() / queueCapacity else 0.0
    val dropRate: Double = if (messagesEnqueued > 0) messagesDropped.toDouble() / messagesEnqueued else 0.0
}
