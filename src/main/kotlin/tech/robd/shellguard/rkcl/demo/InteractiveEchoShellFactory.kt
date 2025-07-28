package tech.robd.shellguard.rkcl.demo
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/demo/InteractiveEchoShellFactory.kt
 * description: ShellFactory for stub SSH servers. Creates a new InteractiveEchoShell for each session.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.shell.ShellFactory
import org.apache.sshd.server.command.Command
// Copyright (C) 2025 Rob Deas and Robokeys Ltd.
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
/**
 * [ShellFactory] implementation that produces an [InteractiveEchoShell] for every SSH session.
 *
 * Intended for agent, UX, and integration testing of SSH-enabled frontends.
 */
class InteractiveEchoShellFactory : ShellFactory {
    /**
     * Creates and returns a new [InteractiveEchoShell] for the session.
     *
     * @param channel The current [ChannelSession], if any.
     * @return A fresh [InteractiveEchoShell] instance.
     */
    override fun createShell(channel: ChannelSession?): Command? {
        return InteractiveEchoShell()
    }
}
