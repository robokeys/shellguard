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