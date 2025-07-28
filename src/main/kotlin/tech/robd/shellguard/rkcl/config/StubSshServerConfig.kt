package tech.robd.shellguard.rkcl.config

/**
 * [File Info]
 * path: tech/robd/ai/act/rkcl/config/StubSshServerConfig.kt
 * description: Spring configuration for launching an in-memory SSH server for testing/demo (NOT production). Allows interactive echo sessions and password auth.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import jakarta.annotation.PreDestroy
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
import org.apache.sshd.common.session.SessionContext
import org.apache.sshd.common.util.security.SecurityUtils
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory
import org.apache.sshd.server.command.CommandFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.robd.shellguard.rkcl.demo.EchoCommand
import tech.robd.shellguard.rkcl.demo.InteractiveEchoShellFactory

/**
 * Spring config for an in-memory stub SSH server (for testing and demo purposes only).
 *
 * - Launches a local SSH server with an ephemeral port and in-memory host key.
 * - Accepts only a single username/password pair (from config or defaults).
 * - Supports simple echo command and interactive shell for demonstration.
 * - Logs all authentication and command activity.
 * - Cleanly stops the server on shutdown.
 *
 * **WARNING:** Never use this for real user access or in production! Intended for agent/UX development, CI, and manual local testing only.
 */
@Configuration
class StubSshServerConfig(
    @param:Value("\${rkcl.stub-ssh-server.enabled:false}") val enabled: Boolean,
    @param:Value("\${rkcl.stub-ssh-server.username:demo}") val username: String,
    @param:Value("\${rkcl.stub-ssh-server.password:demo}") val password: String
) {
    private var sshd: SshServer? = null

    val logger = LoggerFactory.getLogger(StubSshServerConfig::class.java)

    /**
     * Starts the stub SSH server if enabled via config property.
     *
     * @return The running [SshServer] instance, or null if disabled.
     */
    @Bean
    fun maybeStartStubSshServer(): SshServer? {
        if (!enabled) return null

        val keyPair = SecurityUtils.getKeyPairGenerator("RSA").generateKeyPair()

        val server = SshServer.setUpDefaultServer()
        server.host = "127.0.0.1"
        server.port = 0 // OS assigns free port

        // ✅ Host key provider (in-memory)
        server.keyPairProvider = object : AbstractKeyPairProvider() {
            override fun loadKeys(context: SessionContext?) = listOf(keyPair)
        }

        // ✅ Password authentication logic
        server.passwordAuthenticator = PasswordAuthenticator { u, p, _ ->
            logger.info("🔐 Auth attempt: user=$u, password=$p")
            u == username && p == password
        }

        // ✅ Allow password authentication method
        server.userAuthFactories = listOf(UserAuthPasswordFactory())

        // ✅ Simple echo command
        server.commandFactory = CommandFactory { _, cmd ->
            logger.info("💬 EchoCommand received: $cmd")
            EchoCommand(cmd)
        }
        server.shellFactory = InteractiveEchoShellFactory()

        // ✅ Start the server
        server.start()
        val actualPort = server.port

        logger.info("✅ Stub SSH server running on port $actualPort")
        logger.info("➡️ Connect with: ssh $username@localhost -p $actualPort (pw: $password)")

        this.sshd = server
        return server
    }

    /**
     * Cleanly stops the SSH server on application shutdown.
     */
    @PreDestroy
    fun stop() {
        sshd?.stop()
    }
}
