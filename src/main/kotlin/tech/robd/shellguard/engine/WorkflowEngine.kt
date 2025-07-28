package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/WorkflowEngine.kt
 * description: Interface for core workflow engine contract—submit, approve, reject, query, and manage AI actions.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage
import tech.robd.shellguard.bus.core.CommandResult
import tech.robd.shellguard.bus.core.TerminalOutput
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Core workflow engine interface - all engines implement this
 */
interface WorkflowEngine {
    // [📌 Section: Core workflow engine API]
    /**
     * Engine type for identification.
     */
    val engineType: String

    fun submitAction(command: CommandMessage): ActionWorkflow
    fun approveAction(actionId: String, approvedBy: String): Boolean
    fun rejectAction(actionId: String, rejectedBy: String, reason: String): Boolean
    fun getPendingApprovals(): List<ActionWorkflow>
    fun getWorkflow(actionId: String): ActionWorkflow?
    fun getWorkflowStats(): WorkflowStats

    // ✅ Add these methods to the interface
    fun completeAction(actionId: String, result: CommandResult)
    fun failAction(actionId: String, error: String)
    fun emitOutput(actionId: String, output: TerminalOutput)
    fun getAllWorkflows(): List<ActionWorkflow>
    // [/📌 Section: Core workflow engine API]
}
