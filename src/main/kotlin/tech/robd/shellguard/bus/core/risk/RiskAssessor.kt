package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/CommandRiskAssessment.kt
 * description: Interface for command risk assessors (rule-based, AI, composite, etc). Determines command risk and approval requirements.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import tech.robd.shellguard.bus.core.CommandMessage
/**
 * Interface for risk assessment strategies for shell or workflow commands.
 *
 * Implementations may use hardcoded rules, regular expressions, AI models, or composite patterns to
 * classify command risk and determine if human or agent approval is required.
 */
interface RiskAssessor{
    /**
     * Analyze the risk level of the given command.
     * @param command The command CommandMessage to assess (e.g., 'rm -rf /').
     * @return The RiskAssessmentLevel A NUMBER  with a level (LOW, MEDIUM, HIGH, CRITICAL) as defined by the implementation.
     */
    fun assessRisk(command: CommandMessage): RiskAssessmentLevel

    /**
     * Determine if the given risk level requires explicit approval.
     * @param riskLevel The result from [assessRisk].
     * @return True if the command should be paused for approval, false if auto-approved.
     */
    fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean
}