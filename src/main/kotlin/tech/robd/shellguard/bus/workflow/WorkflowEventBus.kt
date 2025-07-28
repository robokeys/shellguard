package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/WorkflowEventBus.kt
 * description: Interface for the workflow event bus. Publishes and subscribes to BusEvent instances by phase or globally.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * publish/subscribe interface for workflow events (BusEvent).
 * Expand or extend for distributed bus, policy enforcement, or audit hooks.
 *
 * - `publish(event)`: Emit a workflow event.
 * - `subscribe(phase, listener)`: Listen for events at a specific phase.
 * - `subscribe(listener)`: Listen for all events.
 * - `unsubscribe(phase, listener)`: Remove a phase-based subscription.
 */
interface WorkflowEventBus {
    // [📌 Section: Workflow event bus API]
    fun publish(event: BusEvent)
    fun subscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit)
    fun subscribe(listener: (BusEvent) -> Unit) // Subscribe to all events
    fun unsubscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit)
    // [/📌 Section: Workflow event bus API]
}