package tech.robd.shellguard.rkcl.controller
/**
 * [File Info]
 * path: tech/robd/shellguard/rkcl/controller/RkclControllerTest.kt
 * description: Unit test for RkclController—verifies session deletion endpoint behavior with a mocked SshSessionManager.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import tech.robd.shellguard.rkcl.service.SshSessionManager

class RkclControllerTest {

    private lateinit var controller: RkclController
    private val sshSessionManager: SshSessionManager = mockk()

    @BeforeEach
    fun setUp() {
        controller = RkclController(sshSessionManager)
    }

    @Test
    fun `delete session works`() {
        // Given
        every { sshSessionManager.removeSession("session-123") } returns Unit

        // When
        val response = controller.deleteSession("session-123")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals(true, body["success"])
        assertEquals("Session deleted", body["message"])
        verify { sshSessionManager.removeSession("session-123") }
    }


}