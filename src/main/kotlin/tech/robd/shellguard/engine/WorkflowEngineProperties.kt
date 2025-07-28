package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/WorkflowEngineProperties.kt
 * description: Spring Boot configuration properties for workflow engine selection ("single" or others).
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


/**
 * [🧩 Point: Workflow engine config properties]
 * Holds configuration for selecting workflow engine type at runtime.
 * Extend for additional engine modes or configuration keys.
 *
 * Example (application.yml):
 *   workflow:
 *     engine: single
 */
@Configuration
@ConfigurationProperties(prefix = "workflow")
class WorkflowEngineProperties {
    var engine: String = "single"
}