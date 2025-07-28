package tech.robd.shellguard
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/RkclTerminalApplication.kt
 * description: Main entrypoint for the RKCL Terminal Spring Boot application.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Spring Boot application for RKCL Terminal.
 *
 * Starts all configured REST endpoints, WebSocket handlers, SSH stubs, and supporting beans.
 */
@SpringBootApplication
class RkclTerminalApplication

/**
 * Application entrypoint. Boots Spring and all configured beans/controllers.
 */
fun main(args: Array<String>) {
    runApplication<RkclTerminalApplication>(*args)
}