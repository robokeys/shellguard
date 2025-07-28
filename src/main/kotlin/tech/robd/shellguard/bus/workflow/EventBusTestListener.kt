package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/EventBusTestListener.kt
 * description: Test-only component for verifying event flow on the workflow event bus.
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
//

// [🧩 Region: Testing Only: DO NOT USE IN PRODUCTION]
/**
 * Simple listener for development/test environments to verify that workflow events are emitted and received.
 *
 * Not for use in production. Intended for local development, debugging, and test harnesses.
 */
class EventBusTestListener {
    private val receivedEvents = mutableListOf<BusEvent>()

    fun onEvent(event: BusEvent) {
        receivedEvents.add(event)
        println("[TestListener] Received: $event")
    }

    fun getReceivedEvents(): List<BusEvent> = receivedEvents.toList()
    fun clearEvents() = receivedEvents.clear()
    fun getEventCount(): Int = receivedEvents.size
}
// [/🧩 Region: Testing Only: DO NOT USE IN PRODUCTION]