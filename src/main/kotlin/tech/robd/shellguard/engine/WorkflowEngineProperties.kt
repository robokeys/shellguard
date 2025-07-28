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
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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