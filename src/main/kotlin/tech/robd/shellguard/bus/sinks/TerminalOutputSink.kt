package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/TerminalOutputSink.kt
 * description: Interface for sinks that receive terminal output events from the command bus (for UI, logs, agents, etc).
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.TerminalOutput
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Sink for receiving [tech.robd.shellguard.bus.core.TerminalOutput] events from the command bus pipeline.
 *
 * Implementations may deliver output to a UI, log, remote agent, or other consumer.
 */
interface TerminalOutputSink {
    /**
     * Called whenever new terminal output is available.
     * @param output The [tech.robd.shellguard.bus.core.TerminalOutput] event.
     */
    fun onTerminalOutput(output: TerminalOutput)
}