package pt.isel.gitteller.service.auth

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import pt.isel.entity.account.User
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.infraestructure.security.principal.UserPrincipalService
import pt.isel.service.auth.AuthService
import pt.isel.service.error.InvalidCredentialsError
import pt.isel.service.error.InvalidTokenError
import pt.isel.service.auth.model.TokenPair
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class AuthServiceTest{
    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var userPrincipalService: UserPrincipalService

    @InjectMocks
    private lateinit var authService: AuthService

    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    @Test
    fun `method login returns token pair`() {

        val testAuth = Mockito.mock<Authentication>()
        val testPair = TokenPair("access", "refresh")

        whenever(authenticationManager.authenticate(any()))
            .thenReturn(testAuth)

        whenever(jwtService.generateTokenPair(testAuth))
            .thenReturn(testPair)

        val result = authService.login(
            "test@email.com",
            "password"
        )

        assertEquals(success(testPair), result)
    }

    @Test
    fun `method login returns InvalidCredentialsError`() {

        whenever(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException(""))

        val result = authService.login(
            "email",
            "password"
        )

        assertEquals(
            failure(InvalidCredentialsError),
            result
        )
    }

    @Test
    fun `method refreshToken returns new token pair`() {

        val principal = UserPrincipal(validUser)

        whenever(jwtService.isValidToken("refresh"))
            .thenReturn(true)

        whenever(jwtService.getUsername("refresh"))
            .thenReturn(validUser.email)

        whenever(userPrincipalService.loadUserByUsername(validUser.email))
            .thenReturn(principal)

        whenever(jwtService.generateAccessToken(any()))
            .thenReturn("access")

        val result = authService.refreshToken("refresh")

        assertEquals(
            success(TokenPair("access", "refresh")),
            result
        )
    }

    @Test
    fun `method refreshToken returns InvalidTokenError when token invalid`() {

        whenever(jwtService.isValidToken("refresh"))
            .thenReturn(false)

        val result = authService.refreshToken("refresh")

        assertEquals(
            failure(InvalidTokenError),
            result
        )
    }

    @Test
    fun `method refreshToken returns InvalidTokenError when user no longer exists`() {

        whenever(jwtService.isValidToken("refresh"))
            .thenReturn(true)

        whenever(jwtService.getUsername("refresh"))
            .thenReturn(validUser.email)

        whenever(userPrincipalService.loadUserByUsername(validUser.email))
            .thenThrow(UsernameNotFoundException(""))

        val result = authService.refreshToken("refresh")

        assertEquals(
            failure(InvalidTokenError),
            result
        )
    }
}