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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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