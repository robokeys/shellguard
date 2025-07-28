package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/Stage1EventConfig.kt
 * description: Spring config wiring the workflow event bus and a test-only event listener for stage 1 deployments.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */

/**
 * Provides the in-memory workflow event bus as a Spring bean for this deployment stage.
 * Swap out for a distributed or persistent bus in later stages.
 */
@org.springframework.context.annotation.Configuration
class Stage1EventConfig {

    // [🧩 Point: Stage 1 workflow event bus wiring]

    @org.springframework.context.annotation.Bean
    fun workflowEventBus(): WorkflowEventBus {
        return InMemoryWorkflowEventBus()
    }

    /**
     * [🧩 Poimt: Test-only event listener bean]
     * Registers an EventBusTestListener for all workflow events.
     * Only for test/dev, not for production.
     */
    @org.springframework.context.annotation.Bean
    fun eventBusTestListener(eventBus: WorkflowEventBus): EventBusTestListener {
        val listener = EventBusTestListener()

        // Subscribe to all events for testing
        eventBus.subscribe { event -> listener.onEvent(event) }

        return listener
    }
}