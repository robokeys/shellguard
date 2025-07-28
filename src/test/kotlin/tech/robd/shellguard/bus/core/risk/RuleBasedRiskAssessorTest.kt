package tech.robd.shellguard.bus.core.risk

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tech.robd.shellguard.bus.core.CommandMessage
import java.time.Instant
import java.util.*

class RuleBasedRiskAssessorTest {

    private val assessor = RuleBasedRiskAssessor(
        objectMapper = ObjectMapper()
    )

    @Test
    fun `classifies critical commands as critical`() {
        val command = createTestCommand("rm -rf /")
        val risk = assessor.assessRisk(command)
        assertTrue(risk.score in 90..100, "Should be critical risk")
    }

    @Test
    fun `classifies high risk commands as high`() {
        val command = createTestCommand("sudo apt update")
        val risk = assessor.assessRisk(command)
        assertTrue(risk.score in 70..89, "Should be high risk")
    }

    @Test
    fun `classifies medium risk commands as medium`() {
        val command = createTestCommand("git push --force")
        val risk = assessor.assessRisk(command)
        assertTrue(risk.score in 40..69, "Should be medium risk")
    }

    @Test
    fun `classifies low risk commands as low`() {
        val command = createTestCommand("ls")
        val risk = assessor.assessRisk(command)
        assertTrue(risk.score in 0..20, "Should be low risk")
    }

    @Test
    fun `unknown commands are classified as medium`() {
        val command = createTestCommand("foo-bar-baz")
        val risk = assessor.assessRisk(command)
        assertEquals(50, risk.score, "Unknown commands default to medium")
    }

    @Test
    fun `risk score correctly maps to risk level`() {
        assertEquals(RiskLevel.CRITICAL, RiskAssessmentLevel(95).level)
        assertEquals(RiskLevel.HIGH, RiskAssessmentLevel(75).level)
        assertEquals(RiskLevel.MEDIUM, RiskAssessmentLevel(55).level)
        assertEquals(RiskLevel.LOW, RiskAssessmentLevel(10).level)
    }

    private fun createTestCommand(commandText: String): CommandMessage {
        return CommandMessage(
            uuid = UUID.randomUUID().toString(),
            command = "LINE",
            timestamp = Instant.now(),
            sessionId = "test-session",
            parameter = commandText,
            riskAssessmentLevel = null,
            workingDirectory = "/tmp",
            reason = "null",
            origin = "web-client"
        )
    }
}