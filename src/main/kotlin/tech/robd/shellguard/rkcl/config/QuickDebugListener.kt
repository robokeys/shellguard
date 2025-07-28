package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/QuickDebugListener.kt
 * description: A debug listener could be combined with other listeners, useful when creating new listeners etc
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
@Configuration
class QuickDebugListener(val workflowEventBus: WorkflowEventBus) {
    @PostConstruct
    fun debugSubscribe() {
        workflowEventBus.subscribe(CommandEventPhase.READY_TO_RUN) { event ->
            println("========================== READY_TO_RUN event caught: ${event.command} ===")
        }
        workflowEventBus.subscribe(CommandEventPhase.APPROVED) { event ->
            println("========================= APPROVED event caught: ${event.command} ===")
        }
    }
}
