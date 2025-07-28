// Composite risk assessment (multiple assessors)
package tech.robd.shellguard.bus.core.risk

import tech.robd.shellguard.bus.core.CommandMessage

/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/CompositeCommandRiskAssessor.kt
 * description: Risk assessment strategy that combines multiple assessors (e.g. rule-based + AI). Uses the highest reported risk.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * Combines multiple [RiskAssessor] implementations to assess risk for a given command.
 *
 * The default strategy takes the highest risk level reported by any assessor
 * (i.e., if *any* implementation flags HIGH/CRITICAL, the composite result is HIGH/CRITICAL).
 * This can be extended to use consensus, weighted scoring, or allow per-command configuration.
 *
 * Example use: combine rule-based and AI-based assessments for defense in depth.
 *
 * @property assessors List of child risk assessment strategies to aggregate.
 */
class CompositeRiskAssessor(
    private val assessors: List<RiskAssessor>
) : RiskAssessor {

    /**
     * Evaluate the risk of a command using all assessors, returning the highest reported risk.
     * Other aggregation strategies (consensus, weighted, etc.) could be implemented here.
     *
     * @param command The command to assess.
     * @return The highest [RiskLevel] reported by any assessor, or MEDIUM if no assessors are present.
     */
    override fun assessRisk(command: CommandMessage): RiskAssessmentLevel {
        if (assessors.isEmpty()) {
            return RiskAssessmentLevel(50) // Default to MEDIUM
        }

        // Get the highest risk score from all assessors
        val highestScore = assessors.maxOf { it.assessRisk(command).score }

        return RiskAssessmentLevel(highestScore)
    }



    /**
     * By default, requires approval for anything above LOW risk.
     * Can be customized if composite strategies need more advanced logic.
     *
     * @param riskLevel The combined risk level.
     * @return True if approval is needed, false if auto-approved.
     */
    override fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean {
        return riskAssessmentLevel.level != RiskLevel.LOW
    }
}