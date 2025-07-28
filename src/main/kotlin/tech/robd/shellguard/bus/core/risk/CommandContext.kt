package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/CommandContext.kt
 * description: Holds contextual information about the command execution environment for risk assessment or audit.
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
 * Encapsulates metadata about the environment in which a command is being run.
 *
 * This context is intended for AI or rule-based risk assessment, and can be extended with
 * more fields as new sources of context (e.g. user/device details, session duration) are needed.
 *
 * @property workingDirectory  The working directory for the command, or null if unknown.
 * @property previousCommands List of previous commands executed in this session (for behavior analysis).
 * @property userProfile      Information about the user, e.g. role or risk profile (if available).
 * @property environment      Environment type, e.g. "dev", "prod", "test".
 * @property timeOfDay        Optional: current time of day (may affect risk scoring).
 * @property projectType      Optional: project or application type/context (could affect allowed commands).
 */
data class CommandContext(
    val workingDirectory: String?,
    val previousCommands: List<String> = emptyList(),
    val userProfile: String?,
    val environment: String?,
    val timeOfDay: String?,
    val projectType: String?
)