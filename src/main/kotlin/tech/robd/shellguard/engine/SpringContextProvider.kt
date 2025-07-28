package tech.robd.shellguard.engine
/**
 * [File Info]
 * path: tech/robd/shellguard/engine/SpringContextProvider.kt
 * description: Get the Spring application context from non-Spring-managed components
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: no
 * [/File Info]
 */
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Provides access to the Spring application context from non-Spring-managed components
 * This class allows retrieving Spring-managed beans programmatically using their type or name and type.
 * It serves as a utility to access the application context from non-Spring-managed components.
 * Syntax example: private val executionSink: CommandExecutionSink = SpringContextProvider.getBean(CommandExecutionSink::class.java)
 *
 * Implements the `ApplicationContextAware` interface to receive and store the application context.
 */
@Component
class SpringContextProvider : ApplicationContextAware {
    override fun setApplicationContext(ctx: ApplicationContext) {
        Companion.ctx = ctx
    }
    companion object {
        @Volatile
        private var ctx: ApplicationContext? = null
        fun <T> getBean(clazz: Class<T>): T = ctx!!.getBean(clazz)
        fun <T> getBean(name: String, clazz: Class<T>): T = ctx!!.getBean(name, clazz)
    }
}
