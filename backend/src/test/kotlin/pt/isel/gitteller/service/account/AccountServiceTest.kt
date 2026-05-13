package pt.isel.gitteller.service.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.entity.User
import pt.isel.service.account.AccountService
import pt.isel.service.account.CreatedNewAccount
import pt.isel.service.account.AccountTypeMaxedError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.LinkedNewProvider
import pt.isel.service.account.LoggedIntoAccount
import pt.isel.service.account.PasswordEncodingError
import pt.isel.service.account.UserNotFound
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
    val validOAuthProvider = AccountType.GOOGLE.type
    val validProviderId = "1"
    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    private fun newUser(id: Int = validUserId, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)

    private fun newOAuthLinkedAccount(
        id: Int = validLinkedAccountId, userId: Int = validUserId,
        provider : String = validOAuthProvider, providerId: String = validProviderId
    ) = OAuthLinkedAccount.create(id, userId, provider = provider, providerId = providerId)

    private fun newFormLinkedAccount(
        id: Int = validLinkedAccountId, userId: Int = validUserId, passwordHash: String = validPasswordHash
    ) = FormLinkedAccount.create(id, userId, passwordHash)

    @Test
    fun `method formSignUp returns CreatedNewAccount if user does not exist`(){
        val testUser = newUser()
        val expected = CreatedNewAccount(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(failure(UserNotFound))
        whenever(userService.create(testUser.email, testUser.userName))
            .thenReturn(success(testUser))
        whenever(linkedAccountService.createFormAccount(testUser.id, validPassword))
            .thenReturn(success(newFormLinkedAccount()))

        val actual = service.formSignUp(testUser.email, testUser.userName, validPassword)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method formSignUp returns LinkedNewProvider if user exists but not the linked account`(){
        val testUser = newUser()
        val expected = LinkedNewProvider(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserAndType(testUser.id, AccountType.FORM.type))
            .thenReturn(null)
        whenever(linkedAccountService.createFormAccount(testUser.id, validPassword))
            .thenReturn(success(newFormLinkedAccount()))

        val actual = service.formSignUp(testUser.email, testUser.userName, validPassword)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method formSignUp returns LoggedIntoAccount if user and the linked account exist`(){
        val testUser = newUser()
        val expected = LoggedIntoAccount(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserAndType(testUser.id, AccountType.FORM.type))
            .thenReturn(listOf(newFormLinkedAccount()))

        val actual = service.formSignUp(testUser.email, testUser.userName, validPassword)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method formSignUp returns PasswordEncodingError from LinkedAccountService layer`(){
        val testUser = newUser()

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserAndType(testUser.id, AccountType.FORM.type))
            .thenReturn(null)
        whenever(linkedAccountService.createFormAccount(testUser.id, validPassword))
            .thenReturn(failure(PasswordEncodingError))

        val actual = service.formSignUp(testUser.email, testUser.userName, validPassword)

        assertTrue(actual.isFailure())
        assertEquals(PasswordEncodingError, actual.leftOrNull())
    }

    @Test
    fun `method formSignUp returns AccountTypeMaxedError from LinkedAccountService layer`(){
        val testUser = newUser()

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserAndType(testUser.id, AccountType.FORM.type))
            .thenReturn(null)
        whenever(linkedAccountService.createFormAccount(testUser.id, validPassword))
            .thenReturn(failure(AccountTypeMaxedError))

        val actual = service.formSignUp(testUser.email, testUser.userName, validPassword)

        assertTrue(actual.isFailure())
        assertEquals(AccountTypeMaxedError, actual.leftOrNull())
    }

    @Test
    fun `method oAuthSignUp returns CreatedNewUser if user does not exist`(){
        val testUser = newUser()
        val expected = CreatedNewAccount(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(failure(UserNotFound))
        whenever(userService.create(testUser.email, null))
            .thenReturn(success(testUser))
        whenever(linkedAccountService.createOAuthAccount(testUser.id, validOAuthProvider, validProviderId))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(testUser.email, validOAuthProvider, validProviderId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns LinkedNewProvider if user exists but not the linked account`(){
        val testUser = newUser()
        val expected = LinkedNewProvider(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserTypeAndKey(testUser.id, validOAuthProvider, validProviderId))
            .thenReturn(null)
        whenever(linkedAccountService.createOAuthAccount(testUser.id, validOAuthProvider, validProviderId))
            .thenReturn(success(newOAuthLinkedAccount()))

        val actual = service.oAuthSignUp(testUser.email, validOAuthProvider, validProviderId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns LoggedIntoAccount if user exists and the linked account exist`(){
        val testUser = newUser()
        val expected = LoggedIntoAccount(testUser)

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.findByUserTypeAndKey(testUser.id, validOAuthProvider, validProviderId))
            .thenReturn(newOAuthLinkedAccount())

        val actual = service.oAuthSignUp(testUser.email, validOAuthProvider, validProviderId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method oAuthSignUp returns DuplicateAccountTypeError if account type is duplicate`(){
        val testUser = newUser()

        whenever(userService.findByEmail(testUser.email)).thenReturn(success(testUser))
        whenever(linkedAccountService.createOAuthAccount(testUser.id, validOAuthProvider, validProviderId))
            .thenReturn(failure(AccountTypeMaxedError))

        val actual = service.oAuthSignUp(testUser.email, validOAuthProvider, validProviderId)

        assertTrue(actual.isFailure())
        assertEquals(AccountTypeMaxedError, actual.leftOrNull())
    }
}