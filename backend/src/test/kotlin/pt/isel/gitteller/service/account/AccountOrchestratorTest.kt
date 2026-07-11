package pt.isel.gitteller.service.account

import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.service.account.AccountOrchestrator
import pt.isel.service.account.model.CreatedNewUserAccount
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.model.LinkedNewAccount
import pt.isel.service.account.model.LoggedIntoUserAccount
import pt.isel.service.account.UserService
import pt.isel.service.error.EmailAlreadyExists
import pt.isel.service.error.InvalidEmail
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.InvalidProvider
import pt.isel.service.error.InvalidProviderID
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.LinkedAccountServiceError
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.UserServiceError
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.utils.Success
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AccountOrchestratorTest() {
    @Mock
    private lateinit var linkedAccountService: LinkedAccountService

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    lateinit var service: AccountOrchestrator

    val validLinkedAccountId = 0
    val validUserId = 0
    val validOAuthProvider = OAuthProvider.GOOGLE
    val validProviderId = "1"
    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    val validUser = User(validUserId, "test@email.com", "test")

    private fun newFormLinkedAccount(
        id: Int = validLinkedAccountId, user: User = validUser, passwordHash: String = validPasswordHash
    ) = FormLinkedAccount(id, user, passwordHash)

    private fun newOAuthLinkedAccount(
        id: Int = validLinkedAccountId,
        user: User = validUser,
        provider: OAuthProvider = validOAuthProvider,
        providerId: String = validProviderId
    ) = OAuthLinkedAccount(id, user, provider = provider, providerId = providerId)

    companion object {
        @JvmStatic
        fun userCreateErrors() = listOf(
            InvalidEmail, InvalidUsername, UsernameAlreadyExists, EmailAlreadyExists
        )

        @JvmStatic
        fun formCreateErrors() = listOf(
            InvalidPassword, LinkedAccountTypeMaxed
        )

        @JvmStatic
        fun oAuthCreateErrors() = listOf(
            InvalidProvider, InvalidProviderID, LinkedAccountTypeMaxed
        )
    }


    @Test
    fun `method formSignUp returns CreatedNewUserAccount if user does not exist`() {
        val expected = CreatedNewUserAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email, validUser.username)).thenReturn(success(validUser))

        whenever(linkedAccountService.createFormAccount(validUser, validPassword)).thenReturn(
                success(
                    newFormLinkedAccount()
                )
            )

        val actual = service.formSignUp(validUser.email, validUser.username, validPassword)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method formSignUp returns LinkedNewAccount if user exists but not the linked account`() {
        val expected = LinkedNewAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(linkedAccountService.findUserFormAccount(validUser.id)).thenReturn(failure(LinkedAccountNotFound))

        whenever(linkedAccountService.createFormAccount(validUser, validPassword)).thenReturn(
                success(
                    newFormLinkedAccount()
                )
            )

        val actual = service.formSignUp(validUser.email, validUser.username, validPassword)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method formSignUp returns LoggedIntoAccount if user and the linked account exist`() {
        val expected = LoggedIntoUserAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(linkedAccountService.findUserFormAccount(validUser.id)).thenReturn(success(newFormLinkedAccount()))

        val actual = service.formSignUp(validUser.email, validUser.username, validPassword)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @ParameterizedTest
    @MethodSource("userCreateErrors")
    fun `method formSignUp returns errors from UserService create method`(
        error: UserServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email, validUser.username)).thenReturn(failure(error))

        val result = service.formSignUp(
            validUser.email, validUser.username, validPassword
        )

        assertEquals(failure(error), result)
    }

    @ParameterizedTest
    @MethodSource("formCreateErrors")
    fun `method formSignUp returns errors from LinkedAccountService createFormAccount method on CreatedNewUser`(
        error: LinkedAccountServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email, validUser.username)).thenReturn(success(validUser))

        whenever(linkedAccountService.createFormAccount(validUser, validPassword)).thenReturn(failure(error))

        val result = service.formSignUp(
            validUser.email, validUser.username, validPassword
        )

        assertEquals(failure(error), result)
    }

    @ParameterizedTest
    @MethodSource("formCreateErrors")
    fun `method formSignUp returns errors from LinkedAccountService createFormAccount method on LinkedNewAccount`(
        error: LinkedAccountServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(linkedAccountService.findUserFormAccount(validUser.id)).thenReturn(failure(LinkedAccountNotFound))

        whenever(linkedAccountService.createFormAccount(validUser, validPassword)).thenReturn(failure(error))

        val result = service.formSignUp(
            validUser.email, validUser.username, validPassword
        )

        assertEquals(failure(error), result)
    }

    @Test
    fun `method oAuthSignUp returns CreatedNewUserAccount if user does not exist`() {
        val expected = CreatedNewUserAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email)).thenReturn(success(validUser))

        whenever(
            linkedAccountService.createOAuthAccount(
                validUser,
                validOAuthProvider.providerName,
                validProviderId
            )
        ).thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method oAuthSignUp returns LinkedNewAccount if user exists but not the linked account`() {
        val expected = LinkedNewAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(
            linkedAccountService.findUserOAuthAccount(
                validUser.id, validOAuthProvider.providerName, validProviderId
            )
        ).thenReturn(failure(LinkedAccountNotFound))

        whenever(
            linkedAccountService.createOAuthAccount(
                validUser,
                validOAuthProvider.providerName,
                validProviderId
            )
        ).thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method oAuthSignUp returns LoggedIntoAccount if user and the linked account exist`() {
        val expected = LoggedIntoUserAccount(validUser)

        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(
            linkedAccountService.findUserOAuthAccount(
                validUser.id, validOAuthProvider.providerName, validProviderId
            )
        ).thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @ParameterizedTest
    @MethodSource("userCreateErrors")
    fun `method oAuthSignUp returns errors from UserService create method`(
        error: UserServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email)).thenReturn(failure(error))

        val result = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertEquals(failure(error), result)
    }

    @ParameterizedTest
    @MethodSource("oAuthCreateErrors")
    fun `method oAuthSignUp returns errors from LinkedAccountService createOAuthAccount method on CreatedNewUser`(
        error: LinkedAccountServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(failure(UserNotFound))

        whenever(userService.create(validUser.email)).thenReturn(success(validUser))

        whenever(linkedAccountService.createOAuthAccount(
            validUser,
            validOAuthProvider.providerName,
            validProviderId
        )).thenReturn(failure(error))

        val result = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertEquals(failure(error), result)
    }

    @ParameterizedTest
    @MethodSource("oAuthCreateErrors")
    fun `method oAuthSignUp returns errors from LinkedAccountService createOAuthAccount method on LinkedNewAccount`(
        error: LinkedAccountServiceError
    ) {
        whenever(userService.findByEmail(validUser.email)).thenReturn(success(validUser))

        whenever(linkedAccountService.findUserOAuthAccount(
            validUser.id, validOAuthProvider.providerName, validProviderId
        )).thenReturn(failure(LinkedAccountNotFound))

        whenever(linkedAccountService.createOAuthAccount(
            validUser,
            validOAuthProvider.providerName,
            validProviderId
        )).thenReturn(failure(error))

        val result = service.oAuthSignUp(validUser.email, validOAuthProvider.providerName, validProviderId)

        assertEquals(failure(error), result)
    }

    @Test
    fun `method oAuthLink returns LinkedNewAccount if linked account does not exist`() {
        val expected = LinkedNewAccount(validUser)

        whenever(userService.findById(validUser.id))
            .thenReturn(success(validUser))

        whenever(
            linkedAccountService.findUserOAuthAccount(
                validUser.id, validOAuthProvider.providerName, validProviderId
            )
        ).thenReturn(failure(LinkedAccountNotFound))

        whenever(
            linkedAccountService.createOAuthAccount(
                validUser,
                validOAuthProvider.providerName,
                validProviderId
            )
        ).thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthLink(validUser.id, validOAuthProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method oAuthLink returns LoggedIntoAccount if linked account exist`() {
        val expected = LoggedIntoUserAccount(validUser)

        whenever(userService.findById(validUser.id))
            .thenReturn(success(validUser))

        whenever(
            linkedAccountService.findUserOAuthAccount(
                validUser.id, validOAuthProvider.providerName, validProviderId
            )
        ).thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthLink(validUser.id, validOAuthProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @ParameterizedTest
    @MethodSource("userCreateErrors")
    fun `method oAuthLink returns UserNotFound from UserService findById method`() {
        whenever(userService.findById(validUser.id))
            .thenReturn(failure(UserNotFound))

        val result = service.oAuthLink(validUser.id, validOAuthProvider.providerName, validProviderId)

        assertEquals(failure(UserNotFound), result)
    }

    @ParameterizedTest
    @MethodSource("oAuthCreateErrors")
    fun `method oAuthLink returns errors from LinkedAccountService createOAuthAccount`(
        error: LinkedAccountServiceError
    ) {
        whenever(userService.findById(validUser.id))
            .thenReturn(success(validUser))

        whenever(
            linkedAccountService.findUserOAuthAccount(
                validUser.id, validOAuthProvider.providerName, validProviderId
            )
        ).thenReturn(failure(LinkedAccountNotFound))

        whenever(linkedAccountService.createOAuthAccount(
            validUser,
            validOAuthProvider.providerName,
            validProviderId
        )).thenReturn(failure(error))

        val result = service.oAuthLink(validUser.id, validOAuthProvider.providerName, validProviderId)

        assertEquals(failure(error), result)
    }
}