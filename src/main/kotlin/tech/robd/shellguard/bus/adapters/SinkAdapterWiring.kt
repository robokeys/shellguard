package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/SinkAdapterWiring.kt
 * description: Component for wiring CommandReviewSink, CommandExecutionSink, and TerminalOutputSink instances into the SinkAdapterRegistry.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.bus.sinks.CommandReviewSink
import tech.robd.shellguard.bus.sinks.TerminalOutputSink
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Configuration that wires your existing sinks to ShellGuard
 */
@Component
class SinkAdapterWiring(
    private val registry: SinkAdapterRegistry
) {
    // [🧩 Section: Sink Wiring Logic]
    // Wires provided sinks into the registry. Expand for custom wiring, conditional logic, or future sink types.

    /**
     * Wire existing sinks to ShellGuard - call this from your config
     */
    fun wireExistingSinks(
        reviewSinks: List<CommandReviewSink> = emptyList(),
        executionSinks: List<CommandExecutionSink> = emptyList(),
        outputSinks: List<TerminalOutputSink> = emptyList()
    ) {
        reviewSinks.forEach { sink ->
            registry.addReviewSink(sink)
        }

        executionSinks.forEach { sink ->
            registry.addExecutionSink(sink)
        }

        outputSinks.forEach { sink ->
            registry.addOutputSink(sink)
        }

        val counts = registry.getAdapterCounts()
        println("[ShellGuard] Wired existing sinks: $counts")
    }
    // [/🧩 Section: Sink Wiring Logic]
}