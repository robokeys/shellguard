package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/Stage1EventConfig.kt
 * description: Spring config wiring the workflow event bus and a test-only event listener for stage 1 deployments.
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
 * Provides the in-memory workflow event bus as a Spring bean for this deployment stage.
 * Swap out for a distributed or persistent bus in later stages.
 */
@org.springframework.context.annotation.Configuration
class Stage1EventConfig {

    // [🧩 Point: Stage 1 workflow event bus wiring]

    @org.springframework.context.annotation.Bean
    fun workflowEventBus(): WorkflowEventBus {
        return InMemoryWorkflowEventBus()
    }

    /**
     * [🧩 Poimt: Test-only event listener bean]
     * Registers an EventBusTestListener for all workflow events.
     * Only for test/dev, not for production.
     */
    @org.springframework.context.annotation.Bean
    fun eventBusTestListener(eventBus: WorkflowEventBus): EventBusTestListener {
        val listener = EventBusTestListener()

        // Subscribe to all events for testing
        eventBus.subscribe { event -> listener.onEvent(event) }

        return listener
    }
}