package tech.robd.shellguard.rkcl.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import tech.robd.shellguard.bus.workflow.CommandEventPhase
import tech.robd.shellguard.bus.workflow.WorkflowEventBus

@Configuration
class QuickDebugListener(val workflowEventBus: WorkflowEventBus) {
    @PostConstruct
    fun debugSubscribe() {
        workflowEventBus.subscribe(CommandEventPhase.READY_TO_RUN) { event ->
            println("========================== READY_TO_RUN event caught: ${event.command} ===")
        }
        workflowEventBus.subscribe(CommandEventPhase.APPROVED) { event ->
            println("========================= APPROVED event caught: ${event.command} ===")
        }
    }
}
