package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/RiskLevel.kt
 * description: Enum defining the risk levels for shell or workflow commands. Used by all risk assessors.
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
 * Risk classification level for commands evaluated by [RiskAssessor], .
 * The model returns a numerical risk but also provides this based on risk
 *
 * Typical examples:
 * - LOW: Safe, non-destructive queries (e.g., `ls`, `pwd`)
 * - MEDIUM: Operations that modify state but are usually safe (e.g., `git push`, `npm install`)
 * - HIGH: Potentially destructive or privilege-escalating (e.g., `rm`, `chmod`, `sudo`)
 * - CRITICAL: Commands that can wipe, brick, or shut down a system (e.g., `rm -rf /`, `shutdown`, `format`)
 */
enum class RiskLevel {
    /** Safe: informational or readonly commands (e.g., ls, pwd, git status) */
    LOW,

    /** Changes state but not usually destructive (e.g., git push, npm install) */
    MEDIUM,

    /** Potentially destructive or needs elevated privileges (e.g., rm, chmod, sudo) */
    HIGH,

    /** Catastrophic: system-wiping, irreversible, or shutdown commands (e.g., rm -rf /, format, shutdown) */
    CRITICAL
}