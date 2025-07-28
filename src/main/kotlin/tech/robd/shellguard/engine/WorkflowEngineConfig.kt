package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/WorkflowEngineConfig.kt
 * description: Spring configuration wiring for WorkflowEngine and dependencies. Selects engine type and risk assessor at runtime.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.robd.shellguard.bus.core.risk.RiskAssessor
import tech.robd.shellguard.bus.workflow.WorkflowEventBus
import tech.robd.shellguard.bus.workflow.stores.WorkflowSessionManager
import tech.robd.shellguard.rkcl.config.SingleWorkflowProperties

/**
 * [🧩 Point: WorkflowEngine Spring configuration]
 * Configures and wires the appropriate WorkflowEngine bean for the application.
 * Selects the engine implementation and risk assessor at runtime based on properties.
 * Extend here to add new engine types (e.g. distributed, AI-driven, multi-tenant).
 */
@Configuration
class WorkflowEngineConfig(
    private val workflowEngineProperties: WorkflowEngineProperties,
    private val applicationContext: ApplicationContext,
    private val singleWorkflowProperties: SingleWorkflowProperties,
    private val eventBus: WorkflowEventBus,
    private val workflowSessionManager: WorkflowSessionManager,
) {

    private val logger = KotlinLogging.logger {}
    @Bean
    fun workflowEngine(): WorkflowEngine {
        return when (workflowEngineProperties.engine) {
            "single" ->{
                logger.info("Using single action workflow engine")
                val assessorBean = applicationContext.getBean(
                    singleWorkflowProperties.assessor,
                    RiskAssessor::class.java
                )
                SingleActionWorkflowEngine(
                    eventBus = eventBus,
                    riskAssessor = assessorBean,
                    workflowSessionManager = workflowSessionManager
                )
            }
            else -> throw IllegalStateException("Unknown workflow engine: ${workflowEngineProperties.engine}")
        }
    }
}