package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/ReadyToRunListener.kt
 * description: listener to actually run (send to terminal) a command.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.engine.SingleActionWorkflowEngine
import tech.robd.shellguard.engine.WorkflowEngine
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

@Component
class ReadyToRunListener(
    eventBus: WorkflowEventBus,
    private val executionSink: CommandExecutionSink,
    private val workflowEngine: WorkflowEngine
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        // Register listener for READY_TO_RUN events
        eventBus.subscribe(CommandEventPhase.READY_TO_RUN) { event ->
            logger.info("READY_TO_RUN: Executing command via sink: {}", event.command)
            // Tell workflow engine execution is starting
            if (workflowEngine is SingleActionWorkflowEngine) {
                workflowEngine.markExecutionStarted(event.command.uuid)
            }

            executionSink.onCommandExecute(event.command)
        }
    }
}
