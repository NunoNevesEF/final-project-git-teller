package pt.isel.gitteller.service.account

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.memory.account.LinkedAccountRepoMem
import pt.isel.service.account.AccountTypeMaxedError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.PasswordEncodingError
import pt.isel.utils.isFailure
import pt.isel.utils.isSuccess
import pt.isel.utils.leftOrNull
import pt.isel.utils.rightOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class LinkedAccountServiceTest(){
    @Mock
    private lateinit var linkedAccountRepo : LinkedAccountRepoMem

    @InjectMocks
    lateinit var service: LinkedAccountService

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    val validId = 0
    val validUserId = 0
    val validProvider = AccountType.GOOGLE
    val validProviderId = "1"
    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    private fun newOAuthLinkedAccount(
        id: Int = validId, userId: Int = validUserId,
        provider : AccountType = validProvider, providerId: String = validProviderId
    ) = OAuthLinkedAccount(id, userId, provider = provider, providerId = providerId)
    private fun newFormLinkedAccount(
        id: Int = validId, userId: Int = validUserId, passwordHash: String = validPasswordHash
    ) = FormLinkedAccount(id, userId, passwordHash)

    @Test
    fun `method createFormAccount returns the created FormLinkedAccount if account type is not maxed`() {
        val expected = newFormLinkedAccount()
        val accountType = expected.getType()

        whenever(passwordEncoder.encode(any())).thenReturn(expected.passwordHash)
        whenever(linkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createFormAccount(expected.userId, validPassword)
        val accounts = service.readByUserAndType(expected.userId, accountType.type).rightOrNull()

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
        assertNotNull(accounts)
        assertTrue(accounts.size <= accountType.max!!)
    }

    @Test
    fun `method createFormAccount returns PasswordEncodingError if password encoding returns null`(){
        whenever(passwordEncoder.encode(any())).thenReturn(null)

        val actual = service.createFormAccount(validUserId, validPassword)

        assertTrue(actual.isFailure())
        assertEquals(PasswordEncodingError, actual.leftOrNull())
    }

    @Test
    fun `method createFormAccount returns DuplicateAccountTypeError if a form account already exists`(){
        whenever(passwordEncoder.encode(any())).thenReturn(validPasswordHash)
        whenever(linkedAccountRepo.readByUserAndType(validUserId, AccountType.FORM.type))
            .thenReturn(listOf(newFormLinkedAccount()))

        val actual = service.createFormAccount(validUserId, validPasswordHash)
        assertTrue(actual.isFailure())
        assertEquals(AccountTypeMaxedError, actual.leftOrNull())
    }

    @Test
    fun `method createOAuthAccount returns the created OAuthLinkedAccount if account type is not max`(){
        val expected = newOAuthLinkedAccount(provider = AccountType.GOOGLE)
        val accountType = expected.provider

        whenever(linkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createOAuthAccount(expected.userId, accountType.type, expected.providerId)
        val accounts = service.readByUserAndType(expected.userId, accountType.type).rightOrNull()

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
        assertNotNull(accounts)
        assertTrue(accounts.size <= accountType.max!!)
    }

    @Test
    fun `method createOAuthAccount returns DuplicateAccountTypeError if account type is maxed`(){
        val provider = AccountType.GOOGLE.type

        whenever(linkedAccountRepo.readByUserAndType(validUserId, provider))
            .thenReturn(listOf(newOAuthLinkedAccount()))

        val actual = service.createOAuthAccount(validUserId, provider, validProviderId)
        assertTrue(actual.isFailure())
        assertEquals(AccountTypeMaxedError, actual.leftOrNull())
    }

    @Test
    fun `method createOAuthAccount does not return DuplicateAccountError if account type has no max`(){
        val expected = newOAuthLinkedAccount(provider = AccountType.GITHUB)
        val accountType = expected.provider

        whenever(linkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createOAuthAccount(expected.userId, accountType.type, expected.providerId)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
        assertNull(accountType.max)
    }

    //TODO: IMPLEMENT TESTS FOR REST OF METHOD.S
}