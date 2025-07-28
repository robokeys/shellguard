// Configuration for SSH connections
package tech.robd.shellguard.rkcl.config
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/config/SshConfig.kt
 * description: Spring Boot config properties for managed SSH connection settings (host, timeouts, credentials, limits).
 * license: GPL-3.0
 * editable: yes
 * structured: no
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
 * Holds SSH connection configuration properties for RKCL.
 * Configured via `application.yml` or environment variables with the prefix `rkcl.ssh`.
 *
 * Example (YAML):
 * ```
 * rkcl:
 *   ssh:
 *     host: example.com
 *     port: 22
 *     username: myuser
 *     password: secret
 *     connect-timeout-ms: 30000
 *     command-timeout-ms: 60000
 *     max-concurrent-sessions: 5
 *     keep-alive-interval-ms: 15000
 * ```
 *
 * @property host                 SSH server hostname or IP address (default: "localhost").
 * @property port                 SSH server port (default: 34763, override in production).
 * @property username             Username for SSH authentication.
 * @property password             Password for SSH authentication.
 * @property connectTimeoutMs     Timeout for initial SSH connection, in milliseconds.
 * @property commandTimeoutMs     Timeout for individual command execution, in milliseconds.
 * @property maxConcurrentSessions Maximum allowed concurrent SSH sessions.
 * @property keepAliveIntervalMs  How often to send keepalive signals to the server, in ms.
 */
@Configuration
@ConfigurationProperties(prefix = "rkcl.ssh")
data class SshConfig(
    var host: String = "localhost",
    var port: Int = 34763,  // Will be overridden in production
    var username: String = "demo",
    var password: String = "demo",
    var connectTimeoutMs: Long = 30000,
    var commandTimeoutMs: Long = 60000,
    var maxConcurrentSessions: Int = 10,
    var keepAliveIntervalMs: Long = 30000
)