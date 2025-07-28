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
