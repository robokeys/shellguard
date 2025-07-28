package tech.robd.shellguard.rkcl.model
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/model/SessionCreateRequest.kt
 * description: Model for REST/API request to create a new SSH session in RKCL.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * API request payload for creating a new SSH session.
 *
 * Used by clients to initiate a session with the specified SSH host and credentials.
 *
 * @property sessionId  Optional: Client-supplied session ID (if null, backend will generate one).
 * @property host       Hostname or IP address of the SSH server to connect to.
 * @property port       Port number for SSH (default: 22).
 * @property username   Username for SSH authentication.
 * @property password   Password for SSH authentication.
 * @property timeout    Connection timeout in milliseconds (default: 30000).
 */
data class SessionCreateRequest(
    val sessionId: String? = null,
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String,
    val timeout: Int = 30000,
    val interactive: Boolean = false
)
