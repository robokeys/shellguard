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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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