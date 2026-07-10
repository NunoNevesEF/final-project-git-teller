package pt.isel.gitteller.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import pt.isel.controller.AuthController
import pt.isel.service.auth.AuthService
import pt.isel.service.error.InvalidCredentialsError
import pt.isel.service.error.InvalidTokenError
import pt.isel.service.auth.model.TokenPair
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

    @Mock
    lateinit var authService: AuthService

    @InjectMocks
    lateinit var controller: AuthController

    private val validTokenPair = TokenPair(
        "access",
        "refresh"
    )

    @Test
    fun `method login returns token pair`() {

        whenever(authService.login("email", "password"))
            .thenReturn(success(validTokenPair))

        val response = controller.login(
            "email",
            "password"
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(validTokenPair, response.body)
    }

    @Test
    fun `method login returns correct error status`() {

        whenever(authService.login("email", "password"))
            .thenReturn(failure(InvalidCredentialsError))

        val response = controller.login(
            "email",
            "password"
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `method refresh returns token pair`() {

        whenever(authService.refreshToken("refresh"))
            .thenReturn(success(validTokenPair))

        val response = controller.refresh("refresh")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(validTokenPair, response.body)
    }

    @Test
    fun `method refresh returns correct error status`() {

        whenever(authService.refreshToken("refresh"))
            .thenReturn(failure(InvalidTokenError))

        val response = controller.refresh("refresh")

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNull(response.body)
    }
}