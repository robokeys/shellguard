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