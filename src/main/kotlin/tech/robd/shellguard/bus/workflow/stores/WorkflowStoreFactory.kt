package tech.robd.shellguard.bus.workflow.stores
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/stores/WorkflowStoreFactory.kt
 * description: Factory for creating WorkflowStore instances with Spring-managed dependencies.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 *
 */
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Factory class for creating new [WorkflowStore] instances, each wired with the shared [WorkflowEventBus].
 *
 * This is used by [WorkflowSessionManager] to ensure each session receives its own independent workflow store
 * with all required dependencies injected by Spring.
 *
 * Usage:
 *   val store = workflowStoreFactory.create()
 *   // Use the store for managing per-session workflows.
 *
 * This pattern ensures that all [WorkflowStore] instances publish events to the global [WorkflowEventBus],
 * allowing for centralized event handling, logging, and dashboard updates.
 */
@Component
class WorkflowStoreFactory @Autowired constructor(
    private val eventBus: WorkflowEventBus
) {
    fun create(): WorkflowStore = WorkflowStore(eventBus)
}
