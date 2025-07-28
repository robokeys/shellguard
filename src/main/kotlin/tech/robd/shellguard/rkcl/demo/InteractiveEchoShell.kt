package tech.robd.shellguard.rkcl.demo
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/demo/InteractiveEchoShell.kt
 * description: Demo interactive echo shell for stub/test SSH servers. Echos each line back to the user in real time.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import kotlinx.coroutines.*
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import kotlin.coroutines.cancellation.CancellationException
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Demo [Command] implementation with partial update simulation.
 *
 * Now sends line-based updates that work with the frontend partial handlers.
 */
class InteractiveEchoShell : Command {
    private lateinit var inStream: InputStream
    private lateinit var outStream: OutputStream
    private lateinit var callback: ExitCallback
    private var job: Job? = null

    override fun setInputStream(inputStream: InputStream?) { inStream = inputStream!! }
    override fun setOutputStream(outputStream: OutputStream?) { outStream = outputStream!! }
    override fun setErrorStream(errorStream: OutputStream?) {}
    override fun setExitCallback(callback: ExitCallback?) { this.callback = callback!! }

    override fun start(channel: ChannelSession?, env: Environment?) {
        job = CoroutineScope(Dispatchers.IO).launch {
            // Send welcome message
            writeLine("🔁 Welcome to RKCL stub shell with partial updates!")
            writeLine("Type something to see the enhanced response...")
            writeLine("Commands: help, demo, long, clear")
            writeLine("")
            showPrompt()

            val reader = BufferedReader(InputStreamReader(inStream))

            try {
                while (isActive) {
                    val line = withContext(Dispatchers.IO) {
                        reader.readLine()
                    } ?: break

                    // Handle special commands
                    when {
                        line.trim().equals("help", true) -> {
                            handleHelpCommand()
                        }
                        line.trim().equals("demo", true) -> {
                            handleDemoCommand()
                        }
                        line.trim().equals("long", true) -> {
                            handleLongOutputCommand()
                        }
                        line.trim().equals("clear", true) -> {
                            writeLine("Screen cleared")
                        }
                        line.trim().isNotEmpty() -> {
                            handleEchoCommand(line)
                        }
                    }

                    showPrompt()
                }
            } catch (ex: Exception) {
                if (ex !is CancellationException) {
                    ex.printStackTrace()
                }
            } finally {
                callback.onExit(0)
            }
        }
    }

    /**
     * Writes a complete line
     */
    private suspend fun writeLine(message: String) {
        outStream.write("$message\n".toByteArray())
        outStream.flush()
        delay(50) // Small delay for realism
    }

    /**
     * Simulates progressive output by sending increasingly longer lines
     */
    private suspend fun writeProgressive(steps: List<String>, delayMs: Long = 300) {
        for (step in steps) {
            outStream.write("$step\n".toByteArray())
            outStream.flush()
            delay(delayMs)
        }
    }

    /**
     * Simulates typing by sending partial characters on same line
     */
    private suspend fun writeTyping(message: String, delayMs: Long = 80) {
        var partial = ""
        for (char in message) {
            partial += char
            outStream.write("$partial\n".toByteArray())
            outStream.flush()
            delay(delayMs)
        }
    }

    private suspend fun handleEchoCommand(line: String) {
        // Show progressive processing
        writeProgressive(listOf(
            "Processing command...",
            "Processing command... analyzing input",
            "Processing command... analyzing input: '$line'",
            "Processing command... generating response",
            "✅ You said: $line"
        ), 200)
    }

    private suspend fun handleHelpCommand() {
        writeLine("📋 Available commands:")
        delay(100)
        writeLine("  help  - Show this help")
        delay(50)
        writeLine("  demo  - Show progressive file listing")
        delay(50)
        writeLine("  long  - Show multi-step process")
        delay(50)
        writeLine("  clear - Clear screen")
        delay(50)
        writeLine("  <text> - Echo with processing simulation")
    }

    private suspend fun handleDemoCommand() {
        writeLine("🎬 Simulating file system scan...")
        delay(300)

        writeProgressive(listOf(
            "Scanning /home/demo...",
            "Scanning /home/demo... found: .bashrc",
            "Scanning /home/demo... found: .bashrc, .profile",
            "Scanning /home/demo... found: .bashrc, .profile, documents/",
            "Scanning /home/demo... found: .bashrc, .profile, documents/, projects/",
            "✅ Scan complete - 4 items found"
        ), 250)
    }

    private suspend fun handleLongOutputCommand() {
        writeLine("🔄 Starting multi-step process...")
        delay(200)

        val steps = listOf(
            "Step 1/5: Initializing...",
            "Step 2/5: Loading configuration...",
            "Step 3/5: Connecting to services...",
            "Step 4/5: Processing data...",
            "Step 5/5: Finalizing..."
        )

        for (step in steps) {
            writeTyping(step, 60)
            delay(300)
            writeLine("✅ ${step.split(':')[1].trim()} complete")
            delay(200)
        }

        writeLine("🎉 All operations completed successfully!")
    }

    private suspend fun showPrompt() {
        outStream.write("demo@stub-shell:~$ ".toByteArray())
        outStream.flush()
    }

    override fun destroy(channel: ChannelSession?) {
        job?.cancel()
        outStream.write("\n❌ Session closed\n".toByteArray())
        outStream.flush()
    }
}