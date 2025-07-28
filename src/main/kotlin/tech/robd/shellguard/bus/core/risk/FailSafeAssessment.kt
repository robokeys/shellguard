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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

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
