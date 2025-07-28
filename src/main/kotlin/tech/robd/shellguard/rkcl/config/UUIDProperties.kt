package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/UuidProperties.kt
 * description: Spring Boot config properties for UUID generation—mode, prefix, and namespace selection.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * [🧩 Point: UUID config properties]
 * Configuration for UUID generator—mode (v1, v4, sequential, etc.), prefix, and namespace.
 * Expand for new UUID options, base name defaults, or distributed settings.
 *
 * Example (application.yml):
 *   uuid:
 *     mode: v4
 *     prefix: cmd
 *     baseName: my-app
 *     namespace: 12345678-90ab-cdef-1234-567890abcdef
 */
@ConfigurationProperties(prefix = "uuid")
@Component
data class UuidProperties(
    var mode: String = "v4",           // "v4" by default; supports: "sequential", "v1", ..., "v8", etc.
    var prefix: String = "cmd",        // Still handy for traceable/sequential use cases
    var baseName: String? = null,      // For v3/v5 name-based UUIDs
    var namespace: String? = null      // Ditto
)