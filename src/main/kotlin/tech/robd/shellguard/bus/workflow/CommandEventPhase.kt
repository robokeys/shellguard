package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/CommandEventPhase.kt
 * description: Enum representing all workflow phases in the command/event lifecycle for RKCL and approval.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Command workflow phases - tracks the complete lifecycle
 */
enum class CommandEventPhase {
    SUBMITTED,
    RISK_ASSESSED,
    PENDING_APPROVAL,
    APPROVED, // will be able to move to READY_TO_RUN if it is the first in the queue
    REJECTED,
    IMMEDIATE_EXECUTE_APPROVAL, // this one means it will execute even if there is a pending approval status item in the queue
    READY_TO_RUN, // this will be actually actioned
    EXECUTION_STARTED,
    COMPLETED,
    FAILED,
    OUTPUT
}
