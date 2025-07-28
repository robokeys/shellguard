package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/SinkAdapterRegistry.kt
 * description: Registry managing all sink adapters for shellguard, routing workflow events to registered sinks.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.bus.sinks.CommandReviewSink
import tech.robd.shellguard.bus.sinks.TerminalOutputSink
import tech.robd.shellguard.bus.workflow.BusEvent
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Registry that manages all sink adapters and routes events to them
 */
@Component
class SinkAdapterRegistry(
    private val eventBus: WorkflowEventBus
) {
    private val reviewAdapters = mutableListOf<ReviewSinkAdapter>()
    private val executionAdapters = mutableListOf<ExecutionCompletionSinkAdapter>()
    private val outputAdapters = mutableListOf<OutputSinkAdapter>()

    // Event Subscription
    // Subscribes to the event bus and sets up event routing.
    // Future: Expand to support filtering, async handling, or metrics.
    init {
        // [🧩 Section: Event Subscription]
        // Subscribe to all events and route to adapters
        eventBus.subscribe { event ->
            routeEventToAdapters(event)
        }
        // [/🧩 Section: Event Subscription]
        println("[shellguard] SinkAdapterRegistry initialized - will route events to existing sinks")
    }

    // [🧩 Section: Event Routing Logic]
    // Handles routing BusEvents to all registered adapters.
    // Expand for filtering, priorities, or per-event customization.
    private fun routeEventToAdapters(event: BusEvent) {
        try {
            // Route to all review sink adapters
            reviewAdapters.forEach { adapter ->
                adapter.handleEvent(event)
            }

            // Route to all execution sink adapters
            executionAdapters.forEach { adapter ->
                adapter.handleEvent(event)
            }

            // Route to all output sink adapters
            outputAdapters.forEach { adapter ->
                adapter.handleEvent(event)
            }
        } catch (e: Exception) {
            println("[shellguard] Error routing event to adapters: ${e.message}")
            e.printStackTrace()
        }
    }
    // [/🧩 Section: Event Routing Logic]


    // [🧩 Section: Adapter Registration API]
    // Add methods for registering new sinks; likely expansion point for hot-reload, removal, or health checking.
    fun addReviewSink(sink: CommandReviewSink) {
        val adapter = ReviewSinkAdapter(sink)
        reviewAdapters.add(adapter)
        println("[shellguard] Registered CommandReviewSink: ${sink::class.simpleName}")
    }

    fun addExecutionSink(sink: CommandExecutionSink) {
        val adapter = ExecutionCompletionSinkAdapter(sink)
        executionAdapters.add(adapter)
        println("[shellguard] Registered ExecutionCompletionSink: ${sink::class.simpleName}")
    }

    fun addOutputSink(sink: TerminalOutputSink) {
        val adapter = OutputSinkAdapter(sink)
        outputAdapters.add(adapter)
        println("[shellguard] Registered TerminalOutputSink: ${sink::class.simpleName}")
    }
    // [/🧩 Section: Adapter Registration API]

    // [🧩 Point: Adapter Introspection]
    /**
     * Returns current adapter counts; expand for more detailed introspection or diagnostics.
     */
    fun getAdapterCounts(): Map<String, Int> {
        return mapOf(
            "reviewAdapters" to reviewAdapters.size,
            "executionAdapters" to executionAdapters.size,
            "outputAdapters" to outputAdapters.size
        )
    }
    // [/🧩 Point: Adapter Introspection]
}
