package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/ShellGuardEngineConfig.kt
 * description: Legacy Spring configuration to provide RiskAssessor bean for compatibility. To be removed after migration.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.risk.RiskAssessor
import tech.robd.shellguard.bus.core.risk.RuleBasedRiskAssessor

// [ 🧩 Region: Legacy risk assessment bean]
/**
 * This configuration provides the RiskAssessor bean under its legacy name, for backward compatibility with older components.
 *
 * TODO: Remove this after all components migrate to new, explicit RiskAssessor configuration.
 *
 */
@org.springframework.context.annotation.Configuration
class ShellGuardEngineConfig(val ruleBasedAssessor : RuleBasedRiskAssessor) {

    /**
     * Provides RiskAssessor under legacy bean name for backward compatibility.
     */
    @org.springframework.context.annotation.Bean
    fun riskAssessment(): RiskAssessor {
        return ruleBasedAssessor
    }
}
//[/ 🧩 Region: Legacy risk assessment bean]
