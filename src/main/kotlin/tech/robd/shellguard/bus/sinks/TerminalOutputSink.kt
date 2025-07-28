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