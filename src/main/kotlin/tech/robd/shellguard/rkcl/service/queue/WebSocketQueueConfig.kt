package tech.robd.shellguard.rkcl.service.queue
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/queue/WebSocketQueueConfig.kt
 * description: Configuration class for per-session WebSocket message queue—controls queue size, cleanup, and shutdown.
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
 * Configuration parameters for the WebSocket message queue.
 *
 * @property queueCapacity      Maximum number of outbound messages buffered in memory.
 * @property cleanupInterval    How often (in messages) to check for and remove closed WebSocket listeners.
 * @property shutdownTimeoutMs  Max time to wait for the worker thread to shut down gracefully (ms).
 */
data class WebSocketQueueConfig(
    val queueCapacity: Int = 10000,
    val cleanupInterval: Long = 50L, // Clean up closed listeners every N messages
    val shutdownTimeoutMs: Long = 2000L
)