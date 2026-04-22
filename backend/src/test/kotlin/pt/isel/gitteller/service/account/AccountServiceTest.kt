package pt.isel.gitteller.service.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.domain.account.User
import pt.isel.service.account.AccountService
import pt.isel.service.account.DuplicateAccountTypeError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.utils.failure
import pt.isel.utils.isFailure
import pt.isel.utils.isSuccess
import pt.isel.utils.leftOrNull
import pt.isel.utils.rightOrNull
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AccountServiceTest() {
    @Mock
    private lateinit var linkedAccountService: LinkedAccountService

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    lateinit var service: AccountService

    val validLinkedAccountId = 0
    val validUserId = 0
    val validProvider = "testProvider"
    val validProviderId = "1"
    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    private fun newUser(id: Int = validUserId, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)

    private fun newOAuthLinkedAccount(
        id: Int = validLinkedAccountId, userId: Int = validUserId,
        provider : String = validProvider, providerId: String = validProviderId
    ) = OAuthLinkedAccount.create(id, userId, provider = provider, providerId = providerId)

    private fun newFormLinkedAccount(
        id: Int = validLinkedAccountId, userId: Int = validUserId, passwordHash: String = validPasswordHash
    ) = FormLinkedAccount.create(id, userId, passwordHash)

    @Test
    fun `method formSignUp returns User if it already exists and linked account type is not duplicate`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(expected)
        whenever(linkedAccountService.createFormAccount(expected.id, validPassword))
            .thenReturn(success(newFormLinkedAccount()))

        val actual = service.formSignUp(expected.email, expected.userName, validPassword)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method formSignUp returns created User if it doesn't already exists`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(null)
        whenever(userService.create(expected.email, expected.userName))
            .thenReturn(success(expected))
        whenever(linkedAccountService.createFormAccount(expected.id, validPassword))
            .thenReturn(success(newFormLinkedAccount()))

        val actual = service.formSignUp(expected.email, expected.userName, validPassword)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method formSignUp returns DuplicateAccountTypeError if account type is duplicate`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(expected)
        whenever(linkedAccountService.createFormAccount(expected.id, validPassword))
            .thenReturn(failure(DuplicateAccountTypeError))

        val actual = service.formSignUp(expected.email, expected.userName, validPassword)

        assertTrue(actual.isFailure())
        assertEquals(DuplicateAccountTypeError, actual.leftOrNull())
    }

    @Test
    fun `method oAuthSignUp returns User if it already exists and linked account type is not duplicate`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(expected)
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider, validProviderId))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider, validProviderId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns created User if it doesn't already exists`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(null)
        whenever(userService.create(expected.email, expected.userName))
            .thenReturn(success(expected))
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider, validProviderId))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider, validProviderId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns DuplicateAccountTypeError if account type is duplicate`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(expected)
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider, validProviderId))
            .thenReturn(failure(DuplicateAccountTypeError))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider, validProviderId)

        assertTrue(actual.isFailure())
        assertEquals(DuplicateAccountTypeError, actual.leftOrNull())
    }
}