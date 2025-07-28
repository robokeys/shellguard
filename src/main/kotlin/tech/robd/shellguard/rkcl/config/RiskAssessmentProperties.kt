package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/SingleWorkflowProperties.kt
 * description: Spring Boot config for the single-action workflow engine—selects risk assessor bean and toggles human approval mode.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * [🧩 Point: Single workflow engine config properties]
 * Properties for configuring risk assessor and human/manual approval mode.
 * Expand for more fine-grained workflow engine configuration in the future.
 *
 * Example (application.yml):
 *   workflow:
 *     single:
 *       assessor: failSafeAssessor
 *       human: true
 */
@Configuration
@ConfigurationProperties(prefix = "workflow.single")
class SingleWorkflowProperties {
    var assessor: String = "failSafeAssessor"
    var human: Boolean = false
}