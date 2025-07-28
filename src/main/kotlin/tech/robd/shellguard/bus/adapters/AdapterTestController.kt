package tech.robd.shellguard.bus.adapters
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/adapters/AdapterTestController.kt
 * description: Minimal REST controller for testing shellguard sink adapter wiring and registry status.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
/**
 * Test controller to verify adapter wiring
 */
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/api/shellguard/adapters")
class AdapterTestController(
    private val registry: SinkAdapterRegistry
) {
    @org.springframework.web.bind.annotation.GetMapping("/info")
    fun getAdapterInfo(): Map<String, Any> {
        return mapOf(
            "system" to "ShellGuard Sink Adapters",
            "description" to "Bridges ShellGuard events to existing CommandBus sinks",
            "adapterCounts" to registry.getAdapterCounts()
        )
    }

    // [🧩 Section: AddTestAdapterEndpoints]
    // Future: Add endpoints for triggering adapter health checks, sample events, or adapter reloads.
    // [/🧩 Section: AddTestAdapterEndpoints]
}