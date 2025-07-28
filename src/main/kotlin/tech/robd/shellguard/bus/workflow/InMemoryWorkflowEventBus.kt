package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/InMemoryWorkflowEventBus.kt
 * description: Simple in-memory implementation of the workflow event bus. Manages subscriptions and delivers BusEvent to listeners.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

/**
 * In-memory implementation of [WorkflowEventBus].
 * Manages listeners by phase and provides pub/sub for [BusEvent].
 * Use only for development, testing, or small deployments—replace with a distributed event bus for scale or durability.
 */
class InMemoryWorkflowEventBus : WorkflowEventBus {
    // [🧩 Section: EventBus listener management]
    // Manages both phase-specific and "all-event" listeners.
    private val phaseListeners = ConcurrentHashMap<CommandEventPhase, MutableList<(BusEvent) -> Unit>>()
    private val allEventListeners = mutableListOf<(BusEvent) -> Unit>()
    // [/🧩 Section: EventBus listener management]

    private val logger = KotlinLogging.logger {}

    override fun publish(event: BusEvent) {
        logger.info("[EventBus] Publishing: $event")

        // Notify phase-specific listeners
        phaseListeners[event.phase]?.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                logger.info("[EventBus] Error in phase listener for ${event.phase}: ${e.message}")
                e.printStackTrace()
            }
        }

        // Notify all-event listeners
        allEventListeners.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                logger.info("[EventBus] Error in all-event listener: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun subscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit) {
        phaseListeners.computeIfAbsent(phase) { mutableListOf() }.add(listener)
        logger.info("[EventBus] Subscribed to phase: $phase")
    }

    override fun subscribe(listener: (BusEvent) -> Unit) {
        allEventListeners.add(listener)
        logger.info("[EventBus] Subscribed to all events")
    }

    override fun unsubscribe(phase: CommandEventPhase, listener: (BusEvent) -> Unit) {
        phaseListeners[phase]?.remove(listener)
    }

    // [📌 Point: Debug/metrics utilities]
    // Expand here for more observability, metrics, or management endpoints.
    fun getListenerCount(phase: CommandEventPhase): Int = phaseListeners[phase]?.size ?: 0
    fun getTotalListenerCount(): Int = phaseListeners.values.sumOf { it.size } + allEventListeners.size
}

