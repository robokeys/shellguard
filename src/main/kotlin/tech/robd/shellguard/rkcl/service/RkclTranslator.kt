// src/main/kotlin/tech/robd/rkcl/service/RkclTranslator.kt
package tech.robd.shellguard.rkcl.service
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/service/RkclTranslator.kt
 * description: Translates high-level RKCL commands to terminal byte sequences for SSH session control.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.stereotype.Service
import mu.KotlinLogging
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * RKCL command-to-byte-sequence translator.
 *
 * Converts high-level RKCL agent/UI commands into the corresponding byte codes for terminal emulators or SSH sessions.
 * Handles special keys, combos (Ctrl/Alt/etc), and line-based commands.
 */
@Service
class RkclTranslator {
    private val logger = KotlinLogging.logger {}

    /**
     * Converts an RKCL command and optional parameter to terminal byte sequence.
     *
     * @param command   The high-level RKCL command ("KEY", "COMBO", "TEXT", etc).
     * @param parameter An optional argument (e.g. key name, text to send).
     * @return Byte array to write to the terminal, or empty if unknown.
     */
    fun translateToBytes(command: String, parameter: String?): ByteArray {
        return when (command.uppercase()) {
            "TEXT" -> parameter?.toByteArray() ?: byteArrayOf()
            "LINE" -> "${parameter ?: ""}\r\n".toByteArray()
            "KEY" -> translateKey(parameter ?: "")
            "COMBO" -> translateCombo(parameter ?: "")
            "EDIT" -> translateEdit(parameter ?: "")
            else -> {
                logger.warn { "Unknown RKCL command: $command" }
                byteArrayOf()
            }
        }
    }

    /**
     * Translates a single key press into the corresponding terminal byte sequence.
     */
    private fun translateKey(key: String): ByteArray {
        return when (key.uppercase()) {
            "ENTER", "RETURN" -> "\r".toByteArray()
            "BACKSPACE", "BKSP", "BS" -> "\b".toByteArray()
            "TAB" -> "\t".toByteArray()
            "ESC", "ESCAPE" -> "\u001b".toByteArray()
            "SPACE" -> " ".toByteArray()
            "UP" -> "\u001b[A".toByteArray()
            "DOWN" -> "\u001b[B".toByteArray()
            "RIGHT" -> "\u001b[C".toByteArray()
            "LEFT" -> "\u001b[D".toByteArray()
            "HOME" -> "\u001b[H".toByteArray()
            "END" -> "\u001b[F".toByteArray()
            "DELETE", "DEL" -> "\u001b[3~".toByteArray()
            "INSERT", "INS" -> "\u001b[2~".toByteArray()
            "PAGEUP", "PGUP" -> "\u001b[5~".toByteArray()
            "PAGEDOWN", "PGDN" -> "\u001b[6~".toByteArray()
            else -> when {
                key.matches(Regex("F\\d+")) -> translateFunctionKey(key)
                key.length == 1 -> key.toByteArray()
                else -> {
                    logger.warn { "Unknown key: $key" }
                    byteArrayOf()
                }
            }
        }
    }

    /**
     * Translates function key names (F1..F12) to their corresponding escape codes.
     */
    private fun translateFunctionKey(key: String): ByteArray {
        val num = key.substring(1).toIntOrNull() ?: return byteArrayOf()
        return when (num) {
            1 -> "\u001bOP".toByteArray()
            2 -> "\u001bOQ".toByteArray()
            3 -> "\u001bOR".toByteArray()
            4 -> "\u001bOS".toByteArray()
            5 -> "\u001b[15~".toByteArray()
            6 -> "\u001b[17~".toByteArray()
            7 -> "\u001b[18~".toByteArray()
            8 -> "\u001b[19~".toByteArray()
            9 -> "\u001b[20~".toByteArray()
            10 -> "\u001b[21~".toByteArray()
            11 -> "\u001b[23~".toByteArray()
            12 -> "\u001b[24~".toByteArray()
            else -> {
                logger.warn { "Unsupported function key: $key" }
                byteArrayOf()
            }
        }
    }

    /**
     * Translates modifier combos (e.g. CTRL-ALT-DEL, CTRL+C) to terminal byte codes.
     */
    private fun translateCombo(combo: String): ByteArray {
        val parts = combo.split(Regex("[-+\\s]")).map { it.trim().uppercase() }
        var ctrl = false
        var alt = false
        var shift = false
        var key = ""

        parts.forEach { part ->
            when (part) {
                "CTRL" -> ctrl = true
                "ALT" -> alt = true
                "SHIFT"-> shift = true
                "GUI", "META", "CMD" -> {} // Not supported in terminal
                else -> key = part
            }
        }

        if (key.isEmpty()) {
            logger.warn { "No key specified in combo: $combo" }
            return byteArrayOf()
        }

        // Handle special combos
        if (ctrl && alt && key == "DEL") {
            return "\u001b[3;7~".toByteArray() // Ctrl+Alt+Delete
        }

        val baseKey = when (key) {
            "DEL", "DELETE" -> "\u007f"
            else -> translateKey(key).toString(Charsets.UTF_8)
        }

        return when {
            ctrl && baseKey.length == 1 -> {
                val code = baseKey.lowercase()[0].code
                if (code in 97..122) { // a-z
                    byteArrayOf((code - 96).toByte()) // Convert to Ctrl+key
                } else baseKey.toByteArray()
            }
            alt && baseKey.length == 1 -> "\u001b$baseKey".toByteArray()
            else -> baseKey.toByteArray()
        }
    }

    /**
     * Translates standard edit operations ("cut", "copy", "paste", "selectall") to terminal key codes.
     */
    private fun translateEdit(action: String): ByteArray {
        return when (action.lowercase()) {
            "cut" -> "\u0018".toByteArray() // Ctrl+X
            "copy" -> "\u0003".toByteArray() // Ctrl+C
            "paste" -> "\u0016".toByteArray() // Ctrl+V
            "selectall" -> "\u0001".toByteArray() // Ctrl+A
            else -> {
                logger.warn { "Unknown edit action: $action" }
                byteArrayOf()
            }
        }
    }
}
