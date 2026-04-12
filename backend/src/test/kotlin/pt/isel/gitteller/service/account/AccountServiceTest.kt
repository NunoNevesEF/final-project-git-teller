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
import pt.isel.service.failure
import pt.isel.service.isFailure
import pt.isel.service.isSuccess
import pt.isel.service.leftOrNull
import pt.isel.service.rightOrNull
import pt.isel.service.success
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
    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    private fun newUser(id: Int = validUserId, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)
    private fun newOAuthLinkedAccount(id: Int = validLinkedAccountId, userId: Int = validUserId, provider : String = validProvider) =
        OAuthLinkedAccount(id, userId, provider)
    private fun newFormLinkedAccount(id: Int = validLinkedAccountId, userId: Int = validUserId, passwordHash: String = validPasswordHash) =
        FormLinkedAccount(id, userId, passwordHash)

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
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns created User if it doesn't already exists`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(null)
        whenever(userService.create(expected.email, expected.userName))
            .thenReturn(success(expected))
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns DuplicateAccountTypeError if account type is duplicate`(){
        val expected = newUser()

        whenever(userService.findByEmail(expected.email)).thenReturn(expected)
        whenever(linkedAccountService.createOAuthAccount(expected.id, validProvider))
            .thenReturn(failure(DuplicateAccountTypeError))

        val actual = service.oAuthSignUp(expected.email, expected.userName, validProvider)

        assertTrue(actual.isFailure())
        assertEquals(DuplicateAccountTypeError, actual.leftOrNull())
    }
}