package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/FailSafeAssessment.kt
 * description: Risk assessor that marks every command as high risk—nothing is auto-approved. Use as a failsafe or harden mode.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.core.CommandMessage

// [🧩 Point: Failsafe/Paranoid Mode]
// This class enforces maximum safety: all commands require approval, none are auto-approved.
// Use in security-hardened deployments, compliance mode, or for demoing strict policy.

@Component("failSafeAssessor")
class FailSafeAssessor : RiskAssessor {

    override fun assessRisk(command: CommandMessage): RiskAssessmentLevel {
        return  RiskAssessmentLevel(90)
    }

    override fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean {
        return true // everything requires approval
    }
}
