package tech.robd.shellguard.bus.core.risk

/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/RiskAssessmentLevel.kt
 * description: RiskAssessmentLevel data class for representing and categorizing risk scores in the workflow system.
 * license: GPL-3.0
 * editable: yes
 * structured: yes
 * [/File Info]
 */
data class RiskAssessmentLevel(
    val score: Int,  // 0-100 primary score
    val level: RiskLevel  // Derived from score
) {
    constructor(score: Int) : this(score, levelFromScore(score))

    companion object {
        // [🧩 Section: Risk Scoring Model]
        // Centralizes score-to-level mapping and threshold logic.
        // Future: Adjust scoring bands, add more granular levels, or support config-driven thresholds.
        private fun levelFromScore(score: Int): RiskLevel = when (score) {
            in 0..39 -> RiskLevel.LOW       // 40 levels (0-39)
            in 40..69 -> RiskLevel.MEDIUM   // 30 levels (40-69)
            in 70..89 -> RiskLevel.HIGH     // 20 levels (70-89)
            in 90..100 -> RiskLevel.CRITICAL // 11 levels (90-100)
            else -> RiskLevel.MEDIUM
        }
        // [/🧩 Section: Risk Scoring Model]
    }

    override fun toString(): String = "$score/$level"

}