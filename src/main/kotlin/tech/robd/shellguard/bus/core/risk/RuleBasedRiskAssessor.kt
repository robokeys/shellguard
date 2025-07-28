// Risk Assessment Service
package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/RuleBasedCommandRiskAssessment.kt
 * description: CommandRiskAssessment implementation using regex patterns and allow-lists to classify command risk.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tech.robd.shellguard.bus.core.CommandMessage

/**
 * Risk assessment service that classifies commands based on static rules and patterns.
 *
 * Uses regular expressions and known allow-lists to quickly flag commands as LOW, MEDIUM, HIGH, or CRITICAL risk.
 * Meant to be fast, explainable, and easily auditable—suitable for most development and many production environments.
 *
 * For unknown or unclassified commands, defaults to MEDIUM risk.
 *
 * Example usage:
 * ```
 * val risk = RuleBasedCommandRiskAssessment().assessRisk("rm -rf /")
 * // risk == CRITICAL
 * ```
 */
@Component("ruleBasedAssessor")
class RuleBasedRiskAssessor ( val objectMapper : ObjectMapper): RiskAssessor {
    private val logger = LoggerFactory.getLogger(RuleBasedRiskAssessor::class.java)

    // [🧩 Section: Risk Rule Logic]
    // Critical/High/Medium/Low patterns and allow-list rules for command classification.
    // Expand here to add config-driven rules, AI rules, or enhanced pattern matching.

    /** Patterns that match catastrophic or system-bricking commands */
    private val criticalPatterns = listOf(
        Regex("""rm\s+-rf\s+/"""),
        Regex("""rm\s+-rf\s+\*"""),
        Regex("""format\s+"""),
        Regex("""shutdown\s+"""),
        Regex("""reboot\s+""")
    )

    /** Patterns that match destructive, privilege-escalating, or highly sensitive commands */
    private val highRiskPatterns = listOf(
        Regex("""rm\s+-[rf]+"""),
        Regex("""sudo\s+"""),
        Regex("""chmod\s+777"""),
        Regex("""passwd\s+""")
    )

    /** Patterns for commands that change state, but are not usually catastrophic */
    private val mediumRiskPatterns = listOf(
        Regex("""git\s+push\s+.*--force"""),
        Regex("""npm\s+install\s+"""),
        Regex("""docker\s+run\s+"""),
        Regex("""rm\s+[^-]""")
    )

    /** Known safe, non-destructive, or informational commands */
    private val lowRiskCommands = setOf(
        "ls", "dir", "pwd", "cd", "cat", "type", "echo", "grep", "find",
        "ps", "top", "df", "du", "free", "whoami", "git status", "git log",
        "git diff", "git branch"
    )
    // [/🧩 Section: Risk Rule Logic]

    /**
     * Classifies the given command into a [RiskAssessmentLevel] by matching regex patterns and allow-lists.
     *
     * @param command The command string to classify.
     * @return The assessed [RiskAssessmentLevel]. Defaults to MEDIUM (50%) for unknown commands.
     */
    override fun assessRisk(command: CommandMessage): RiskAssessmentLevel {
        logger.info("=== RKCL JSON RISK ASSESSMENT START ===")
        logger.info("RKCL JSON payload: '{}'", command)

        try {

            val commandType = command.command
            val parameter = command.parameter ?: ""

            logger.info("RKCL Command Type: '{}'", commandType)
            logger.info("RKCL Parameter: '{}'", parameter)

            return when (commandType.uppercase()) {
                "TEXT", "LINE" -> {
                    logger.info("TEXT/LINE command - assessing shell command parameter: '{}'", parameter)
                    assessPayloadRisk(parameter)
                }
                "KEY" -> {
                    logger.info("KEY command - medium risk for now (TODO: implement key assessment)")
                    RiskAssessmentLevel(50) // [🧩 Point: risk-assessment/todo-key-specific: In future implement key-specific risk assessments]
                }
                "COMBO" -> {
                    logger.info("COMBO command - assessing key combination: '{}'", parameter)
                    assessKeyComboRisk(parameter.uppercase())
                }
                else -> {
                    logger.info("Non-TEXT/LINE command type '{}' - medium risk for now", commandType)
                    RiskAssessmentLevel(50) // Default for other command types
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse RKCL JSON: {}", e.message)
            return RiskAssessmentLevel(50) // Default to medium risk on parse failure
        }
    }

    /**
     * Assesses risk for key combinations and key combos.
     *
     * @param keyCombo The key combination string to assess (e.g., "CTRL+ALT+DEL")
     * @return The assessed [RiskAssessmentLevel].
     */
    private fun assessKeyComboRisk(keyCombo: String): RiskAssessmentLevel {
        logger.info("=== KEY COMBO RISK ASSESSMENT START ===")
        logger.info("Key combination: '{}'", keyCombo)

        return when (keyCombo) {
            "CTRL+ALT+DEL", "CTRL+ALT+DELETE" -> {
                logger.info("CRITICAL key combination: {} - system restart/interrupt", keyCombo)
                RiskAssessmentLevel(90)
            }
            "CTRL+C" -> {
                logger.info("HIGH risk key combination: {} - process interrupt", keyCombo)
                RiskAssessmentLevel(70)
            }
            "CTRL+Z" -> {
                logger.info("MEDIUM risk key combination: {} - process suspend", keyCombo)
                RiskAssessmentLevel(40)
            }
            else -> {
                logger.info("Unknown key combination: {} - defaulting to medium risk", keyCombo)
                RiskAssessmentLevel(50)
            }
        }
    }


    /**
     * Classifies the given command into a [RiskAssessmentLevel] by matching regex patterns and allow-lists.
     *
     * @param command The Payload from JSON command string to classify.
     * @return The assessed [RiskAssessmentLevel]. Defaults to MEDIUM (50%) for unknown commands.
     */
    fun assessPayloadRisk(command: String): RiskAssessmentLevel {
        logger.info("=== PAYLOAD RISK ASSESSMENT START ===")
        logger.info("Original command input: '{}'", command)
        logger.info("Command length: {}", command.length)
        val trimmedCommand = command.trim().lowercase()
        logger.info("Trimmed/lowercase command: '{}'", trimmedCommand)

        // Check critical patterns
        logger.debug("Checking critical patterns...")
        criticalPatterns.forEachIndexed { index, pattern ->
            val match = pattern.find(trimmedCommand)
            logger.debug("Critical pattern {}: '{}' -> match: {}", index, pattern.pattern, match?.value)
            if (match != null) {
                logger.info("CRITICAL risk matched! Pattern: '{}', Match: '{}'", pattern.pattern, match.value)
                return RiskAssessmentLevel(90)
            }
        }
            // Check high risk patterns
            logger.debug("Checking high risk patterns...")
            highRiskPatterns.forEachIndexed { index, pattern ->
                val match = pattern.find(trimmedCommand)
                logger.debug("High risk pattern {}: '{}' -> match: {}", index, pattern.pattern, match?.value)
                if (match != null) {
                    logger.info("HIGH risk matched! Pattern: '{}', Match: '{}'", pattern.pattern, match.value)
                    return RiskAssessmentLevel(70)
                }
            }

            // Check medium risk patterns
            logger.debug("Checking medium risk patterns...")
            mediumRiskPatterns.forEachIndexed { index, pattern ->
                val match = pattern.find(trimmedCommand)
                logger.debug("Medium risk pattern {}: '{}' -> match: {}", index, pattern.pattern, match?.value)
                if (match != null) {
                    logger.info("MEDIUM risk matched! Pattern: '{}', Match: '{}'", pattern.pattern, match.value)
                    return RiskAssessmentLevel(40)
                }
            }

            // Check low risk commands
            val baseCommand = trimmedCommand.split("\\s+".toRegex()).firstOrNull() ?: ""
            logger.info("Extracted base command: '{}'", baseCommand)
            logger.debug("Checking against low risk commands: {}", lowRiskCommands)

            if (lowRiskCommands.contains(baseCommand)) {
                logger.info("LOW risk matched! Base command '{}' found in allow-list", baseCommand)
                return RiskAssessmentLevel(10)
            }
            // Default case
            logger.warn("No patterns matched! Defaulting to MEDIUM risk (50%)")
            logger.warn("Command was: '{}'", command)
            logger.warn("Processed as: '{}'", trimmedCommand)
            logger.warn("Base command: '{}'", baseCommand)
            logger.info("=== PAYLOAD RISK ASSESSMENT END (DEFAULT) ===")

            return RiskAssessmentLevel(50) // Default to medium for unknown commands

    }

    /**
     * Determines if the given [RiskAssessmentLevel] requires explicit approval.
     * By default, only LOW is auto-approved; all others require review.
     *
     * @param riskAssessmentLevel The assessed risk.
     * @return True if approval is needed; false for LOW risk.
     */
    override fun requiresApproval(riskAssessmentLevel: RiskAssessmentLevel): Boolean {
        val requiresApproval = when (riskAssessmentLevel.level) {
            RiskLevel.LOW -> false
            RiskLevel.MEDIUM -> true
            RiskLevel.HIGH -> true
            RiskLevel.CRITICAL -> true
        }
        logger.info("Risk level {} requires approval: {}", riskAssessmentLevel.level, requiresApproval)
        return requiresApproval
    }
}

