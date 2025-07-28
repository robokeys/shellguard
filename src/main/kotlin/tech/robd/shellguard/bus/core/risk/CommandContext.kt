package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/CommandContext.kt
 * description: Holds contextual information about the command execution environment for risk assessment or audit.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * Encapsulates metadata about the environment in which a command is being run.
 *
 * This context is intended for AI or rule-based risk assessment, and can be extended with
 * more fields as new sources of context (e.g. user/device details, session duration) are needed.
 *
 * @property workingDirectory  The working directory for the command, or null if unknown.
 * @property previousCommands List of previous commands executed in this session (for behavior analysis).
 * @property userProfile      Information about the user, e.g. role or risk profile (if available).
 * @property environment      Environment type, e.g. "dev", "prod", "test".
 * @property timeOfDay        Optional: current time of day (may affect risk scoring).
 * @property projectType      Optional: project or application type/context (could affect allowed commands).
 */
data class CommandContext(
    val workingDirectory: String?,
    val previousCommands: List<String> = emptyList(),
    val userProfile: String?,
    val environment: String?,
    val timeOfDay: String?,
    val projectType: String?
)