package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/RiskLevel.kt
 * description: Enum defining the risk levels for shell or workflow commands. Used by all risk assessors.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * Risk classification level for commands evaluated by [RiskAssessor], .
 * The model returns a numerical risk but also provides this based on risk
 *
 * Typical examples:
 * - LOW: Safe, non-destructive queries (e.g., `ls`, `pwd`)
 * - MEDIUM: Operations that modify state but are usually safe (e.g., `git push`, `npm install`)
 * - HIGH: Potentially destructive or privilege-escalating (e.g., `rm`, `chmod`, `sudo`)
 * - CRITICAL: Commands that can wipe, brick, or shut down a system (e.g., `rm -rf /`, `shutdown`, `format`)
 */
enum class RiskLevel {
    /** Safe: informational or readonly commands (e.g., ls, pwd, git status) */
    LOW,

    /** Changes state but not usually destructive (e.g., git push, npm install) */
    MEDIUM,

    /** Potentially destructive or needs elevated privileges (e.g., rm, chmod, sudo) */
    HIGH,

    /** Catastrophic: system-wiping, irreversible, or shutdown commands (e.g., rm -rf /, format, shutdown) */
    CRITICAL
}