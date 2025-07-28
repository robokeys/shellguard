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