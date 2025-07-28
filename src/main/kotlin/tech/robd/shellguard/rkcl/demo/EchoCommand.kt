package tech.robd.shellguard.rkcl.demo
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/demo/EchoCommand.kt
 * description: Demo command for stub SSH servers. Echos the received command string and exits after a short delay.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.apache.sshd.server.Environment
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import java.io.InputStream
import java.io.OutputStream
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * Demo [Command] implementation for use with stub/test SSH servers.
 *
 * When executed, this command simply writes back "You ran: $command" to the output stream,
 * waits 2 seconds, then exits with code 0. Intended for agent/UX testing and demo setups.
 *
 * @property command The command string to echo.
 */
class EchoCommand(private val command: String) : Command {
    private var out: OutputStream? = null
    private var exitCallback: ExitCallback? = null

    override fun setInputStream(inputStream: InputStream?) {}
    override fun setOutputStream(outputStream: OutputStream?) { this.out = outputStream }
    override fun setErrorStream(errorStream: OutputStream?) {}
    override fun setExitCallback(callback: ExitCallback?) { this.exitCallback = callback }

    /**
     * Handles command execution: echoes the command, pauses, then exits.
     */
    override fun start(p0: ChannelSession?, p1: Environment?) {
        out?.write("You ran: $command\n".toByteArray())
        out?.flush()  // Keep session open for a moment so it's visible
        Thread.sleep(2000)

        exitCallback?.onExit(0)
    }

    override fun destroy(p0: ChannelSession?) {
    }
}