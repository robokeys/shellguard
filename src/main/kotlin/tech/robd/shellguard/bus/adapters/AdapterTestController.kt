package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/AdapterTestController.kt
 * description: Minimal REST controller for testing shellguard sink adapter wiring and registry status.
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
 * Test controller to verify adapter wiring
 */
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/api/shellguard/adapters")
class AdapterTestController(
    private val registry: SinkAdapterRegistry
) {
    @org.springframework.web.bind.annotation.GetMapping("/info")
    fun getAdapterInfo(): Map<String, Any> {
        return mapOf(
            "system" to "ShellGuard Sink Adapters",
            "description" to "Bridges ShellGuard events to existing CommandBus sinks",
            "adapterCounts" to registry.getAdapterCounts()
        )
    }

    // [🧩 Section: AddTestAdapterEndpoints]
    // Future: Add endpoints for triggering adapter health checks, sample events, or adapter reloads.
    // [/🧩 Section: AddTestAdapterEndpoints]
}