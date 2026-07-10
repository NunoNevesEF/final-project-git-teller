package pt.isel.gitteller.controller

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import pt.isel.controller.account.PrivateAccountController
import pt.isel.controller.account.PublicAccountController
import pt.isel.controller.account.dto.OAuthLinkedAccountListItemDTO
import pt.isel.controller.account.dto.UserDTO
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.service.error.ServiceError
import pt.isel.service.account.AccountOrchestrator
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.service.error.InvalidEmail
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.UserServiceError
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.service.account.model.CreatedNewUserAccount
import pt.isel.service.account.model.LinkedNewAccount
import pt.isel.service.account.model.LoggedIntoUserAccount
import pt.isel.service.account.model.SignUpResult
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class AccountControllerTest {
    val validPassword = "password"

    @Mock
    private lateinit var accountService: AccountOrchestrator

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var linkedAccountService: LinkedAccountService

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var publicController: PublicAccountController

    @InjectMocks
    private lateinit var privateController: PrivateAccountController

    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    private val principal = UserPrincipal(validUser)

    private val githubAccount = OAuthLinkedAccount(
        id = 0,
        user = validUser,
        provider = OAuthProvider.GITHUB,
        providerId = "123"
    )

    companion object {
        private val companionValidUser = User(
            id = 0,
            email = "test@email.com",
            username = "test"
        )

        @JvmStatic
        fun signUpSuccessCases() = listOf(
            Arguments.of(CreatedNewUserAccount(companionValidUser), HttpStatus.CREATED),
            Arguments.of(LinkedNewAccount(companionValidUser), HttpStatus.CREATED),
            Arguments.of(LoggedIntoUserAccount(companionValidUser), HttpStatus.OK)
        )

        @JvmStatic
        fun signUpFailureCases() = listOf(
            Arguments.of(InvalidEmail, HttpStatus.BAD_REQUEST),
            Arguments.of(InvalidUsername, HttpStatus.BAD_REQUEST),
            Arguments.of(InvalidPassword, HttpStatus.BAD_REQUEST),
            Arguments.of(UsernameAlreadyExists, HttpStatus.CONFLICT),
            Arguments.of(LinkedAccountTypeMaxed, HttpStatus.CONFLICT)
        )

        @JvmStatic
        fun updateFailureCases() = listOf(
            Arguments.of(InvalidUsername, HttpStatus.BAD_REQUEST),
            Arguments.of(UserNotFound, HttpStatus.NOT_FOUND),
            Arguments.of(UsernameAlreadyExists, HttpStatus.CONFLICT),
        )
    }

    @ParameterizedTest
    @MethodSource("signUpSuccessCases")
    fun `method signup returns created user with correct status`(
        result: SignUpResult,
        expectedStatus: HttpStatus
    ) {
        whenever(
            accountService.formSignUp(
                validUser.email,
                validUser.username!!,
                validPassword
            )
        ).thenReturn(success(result))

        val response = publicController.signup(
            validUser.email,
            validUser.username!!,
            validPassword
        )

        assertEquals(expectedStatus, response.statusCode)
        assertEquals(UserDTO(validUser), response.body)
    }

    @ParameterizedTest
    @MethodSource("signUpFailureCases")
    fun `signup returns correct error status`(
        error: ServiceError,
        expectedStatus: HttpStatus
    ) {
        whenever(
            accountService.formSignUp(
                validUser.email,
                validUser.username!!,
                validPassword
            )
        ).thenReturn(failure(error))

        val response = publicController.signup(
            validUser.email,
            validUser.username!!,
            validPassword
        )

        assertEquals(expectedStatus, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `setUsername returns updated user`() {
        val updatedUsername = "newUsername"
        val updatedUser = validUser.copy(username = "newUsername")

        whenever(userService.updateUsername(validUser.id, updatedUsername))
            .thenReturn(success(updatedUser))

        val response = privateController.setUsername(
            principal,
            updatedUsername,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(UserDTO(updatedUser), response.body)
    }

    @ParameterizedTest
    @MethodSource("updateFailureCases")
    fun `update returns correct error status`(
        error: UserServiceError,
        expectedStatus: HttpStatus
    ) {
        whenever(
            userService.updateUsername(validUser.id, "newUsername",)
        ).thenReturn(failure(error))

        val response = privateController.setUsername(
            principal,
            "newUsername",
        )

        assertEquals(expectedStatus, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `linkNewProviderAccount returns oauth url`() {
        val provider = "github"
        val state = "state123"

        whenever(jwtService.generateLinkState(validUser.id, provider))
            .thenReturn(state)

        val response = privateController.linkNewProviderAccount(
            principal,
            provider
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(
            mapOf("url" to "oauth2/authorization/${provider}?state=${state}"),
            response.body
        )
    }

    @Test
    fun `listGitAccounts returns empty list`() {
        whenever(linkedAccountService.findUserGitAccounts(validUser.id))
            .thenReturn(emptyList())

        val response = privateController.listGitAccounts(principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(emptyList<OAuthLinkedAccountListItemDTO>(), response.body)
    }

    @Test
    fun `listGitAccounts returns linked git accounts`() {
        whenever(linkedAccountService.findUserGitAccounts(validUser.id))
            .thenReturn(listOf(githubAccount))

        val response = privateController.listGitAccounts(principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(
            listOf(OAuthLinkedAccountListItemDTO(githubAccount)),
            response.body
        )
    }
}