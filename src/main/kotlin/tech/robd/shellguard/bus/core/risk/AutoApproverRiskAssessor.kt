// AUTO-APPROVER - TESTING ONLY - DANGEROUS FOR PRODUCTION
package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/AutoApproverRiskAssessment.kt
 * description: Dummy risk assessment that auto-approves every command. **For testing/development ONLY.**
 * license: GPL-3.0
 * generator: none/human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
//[🧩 Region: Testing Only: DO NOT USE IN PRODUCTION
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.core.CommandMessage
import java.util.concurrent.atomic.AtomicBoolean
/**
 * A *testing-only* implementation of [RiskAssessor] that automatically approves all commands.
 *
 * **WARNING:** This class will approve *every* command, regardless of content or risk.
 * It is useful for integration and end-to-end tests, or for rapid UI/agent development *when no risk controls are required*.
 *
 * NEVER use this class in production, as it bypasses all safety, audit, and approval mechanisms.
 *
 * Example usage (dev only):
 * ```
 * val assessment = AutoApproverRiskAssessment()
 * val risk = assessment.assessRisk("rm -rf /")
 * // risk == LOW, always auto-approved!
 * ```
 */
@Component("autoApproveAssessor")
class AutoApproverRiskAssessor (val ruleBasedRiskAssessor: RuleBasedRiskAssessor): RiskAssessor {

    private val warningShown = AtomicBoolean(false)

    /**
     * Assigns [RiskLevel.LOW] to every command, but logs the actual rule-based risk for audit/debug.
     *
     * @param command The command to "assess" (actual risk is ignored for approval purposes).
     * @return Always returns [RiskLevel.LOW], regardless of command.
     */
    override fun assessRisk(command: CommandMessage): RiskAssessmentLevel {

        // Show warning only when actually used (thread-safe, exactly once)
        if (warningShown.compareAndSet(false, true)) {
            println("⚠️  WARNING: Auto Approver Risk Assessment is NOW ACTIVE - ALL COMMANDS WILL BE AUTO-APPROVED!")
            println("⚠️  THIS IS FOR TESTING / DEVELOPMENT or INTERNAL Systems ONLY - DO NOT USE IN PRODUCTION!")
        }

        // Still assess risk for logging purposes, but everything gets approved
        val actualRisk = ruleBasedRiskAssessor.assessRisk(command)
        println("🤖 AUTO-APPROVING: '$command' (Actual risk: $actualRisk)")
        return RiskAssessmentLevel(10) // Force everything to low risk = auto-approved
    }

    /**
     * Always returns false: no approval is ever required in this implementation.
     *
     * @param riskAssessmentLevel Ignored.
     * @return false (auto-approve all commands)
     */
    override fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean {
        return false // Never require approval - everything auto-approved
    }
}
//[/🧩 Region: Testing Only