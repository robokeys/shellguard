package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/SinkAdapterConfig.kt
 * description: Spring configuration for auto-discovering and wiring sink adapters in shellguard Bus.
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */

import tech.robd.shellguard.bus.sinks.CommandReviewSink
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.bus.sinks.TerminalOutputSink
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Configuration to auto-discover and wire existing sinks
 */
@org.springframework.context.annotation.Configuration
class SinkAdapterConfig {
    // [🧩 Section: Adapter Registry Bean]
    // Defines the primary registry for sink adapters.
    // Expand for additional configuration or multi-registry support.
    @org.springframework.context.annotation.Bean
    fun sinkAdapterRegistry(eventBus: WorkflowEventBus): SinkAdapterRegistry {
        return SinkAdapterRegistry(eventBus)
    }
    // [/🧩 Section: Adapter Registry Bean]

    // [🧩 Section: Adapter Wiring Bean]
    // Wires up all auto-discovered sinks.
    // Expand for custom wiring logic, conditional registration, or additional sink types.
    @org.springframework.context.annotation.Bean
    fun sinkAdapterWiring(
        registry: SinkAdapterRegistry,
        // Auto-discover existing sinks from Spring context
        reviewSinks: List<CommandReviewSink>,
        executionSinks: List<CommandExecutionSink>,
        outputSinks: List<TerminalOutputSink>
    ): SinkAdapterWiring {

        val wiring = SinkAdapterWiring(registry)

        // Automatically wire all existing sinks
        wiring.wireExistingSinks(reviewSinks, executionSinks, outputSinks)

        return wiring
    }
    // [/🧩 Section: Adapter Wiring Bean]
}
