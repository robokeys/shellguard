package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/SinkAdapterWiring.kt
 * description: Component for wiring CommandReviewSink, CommandExecutionSink, and TerminalOutputSink instances into the SinkAdapterRegistry.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.sinks.CommandExecutionSink
import tech.robd.shellguard.bus.sinks.CommandReviewSink
import tech.robd.shellguard.bus.sinks.TerminalOutputSink

/**
 * Configuration that wires your existing sinks to ShellGuard
 */
@Component
class SinkAdapterWiring(
    private val registry: SinkAdapterRegistry
) {
    // [🧩 Section: Sink Wiring Logic]
    // Wires provided sinks into the registry. Expand for custom wiring, conditional logic, or future sink types.

    /**
     * Wire existing sinks to ShellGuard - call this from your config
     */
    fun wireExistingSinks(
        reviewSinks: List<CommandReviewSink> = emptyList(),
        executionSinks: List<CommandExecutionSink> = emptyList(),
        outputSinks: List<TerminalOutputSink> = emptyList()
    ) {
        reviewSinks.forEach { sink ->
            registry.addReviewSink(sink)
        }

        executionSinks.forEach { sink ->
            registry.addExecutionSink(sink)
        }

        outputSinks.forEach { sink ->
            registry.addOutputSink(sink)
        }

        val counts = registry.getAdapterCounts()
        println("[ShellGuard] Wired existing sinks: $counts")
    }
    // [/🧩 Section: Sink Wiring Logic]
}