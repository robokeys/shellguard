package tech.robd.shellguard.bus.sinks
/**
 * [File Info]
 * path: tech/robd/bus/core/ConsoleOutputSink.kt
 * description: Simple TerminalOutputSink implementation that prints terminal output to the local console.
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
 * Output sink that prints all terminal output events to the local standard output.
 *
 * Useful for debugging, testing, or as a fallback output in CLI tools and test harnesses.
 */
class ConsoleOutputSink : TerminalOutputSink {
    /**
     * Called whenever new terminal output is available.
     * Prints output to the system console, prepending the session ID.
     *
     * @param output The terminal output event to print.
     */
    override fun onTerminalOutput(output: TerminalOutput) {
        println("[${output.sessionId}] ${output.output}")
    }
}