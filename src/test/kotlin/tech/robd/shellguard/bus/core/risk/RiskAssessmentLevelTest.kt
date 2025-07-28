package tech.robd.shellguard.bus.core.risk
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/core/risk/RiskAssessmentLevelTest.kt
 * description: Unit tests for RiskAssessmentLevel—basic creation, level bounds, equality.
 * license: GPL-3.0
 * generator: human
 * editable: yes
 * structured: yes
 * [/File Info]
 */
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RiskAssessmentLevelTest {

    @Test
    fun `can create risk assessment level`() {
        val risk = RiskAssessmentLevel(42).level
        assertEquals(RiskLevel.MEDIUM, risk)
    }

    @Test
    fun `equality and hash works`() {
        val r1 = RiskAssessmentLevel(5)
        val r2 = RiskAssessmentLevel(5)
        val r3 = RiskAssessmentLevel(10)
        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    @Test
    fun `risk level boundaries are respected`() {
        val min = RiskAssessmentLevel(1).level
        val max = RiskAssessmentLevel(100).level
        assertEquals(RiskLevel.LOW, min)
        assertEquals(RiskLevel.CRITICAL, max)
    }
}
