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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
//

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
