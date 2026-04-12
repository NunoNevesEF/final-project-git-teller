package pt.isel.gitteller.service.account

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.memory.account.LinkedAccountRepoMem
import pt.isel.service.account.DuplicateAccountTypeError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.isFailure
import pt.isel.service.isSuccess
import pt.isel.service.leftOrNull
import pt.isel.service.rightOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LinkedAccountServiceTest(){
    @Mock
    private lateinit var linkedAccountRepo : LinkedAccountRepoMem

    @InjectMocks
    lateinit var service: LinkedAccountService

    val validId = 0
    val validUserId = 0
    val validProvider = "testProvider"
    val validPasswordHash = "testPasswordHash"

    private fun newOAuthLinkedAccount(id: Int = validId, userId: Int = validUserId, provider : String = validProvider) =
        OAuthLinkedAccount(id, userId, provider)
    private fun newFormLinkedAccount(id: Int = validId, userId: Int = validUserId, passwordHash: String = validPasswordHash) =
        FormLinkedAccount(id, userId, passwordHash)

    @Test
    fun `method createFormAccount returns the created FormLinkedAccount if account type is not duplicate`(){
        val expected = newFormLinkedAccount()

        whenever(linkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createFormAccount(expected.userId, expected.passwordHash)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method createFormAccount returns DuplicateAccountTypeError if account type is duplicated`(){
        whenever(linkedAccountRepo.create(any()))
            .thenThrow(IllegalArgumentException())

        val actual = service.createFormAccount(validUserId, validPasswordHash)
        assertTrue(actual.isFailure())
        assertEquals(DuplicateAccountTypeError, actual.leftOrNull())
    }

    @Test
    fun `method createOAuthAccount returns the created OAuthLinkedAccount if account type is not duplicate`(){
        val expected = newOAuthLinkedAccount()

        whenever(linkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createOAuthAccount(expected.userId, expected.provider)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method createOAuthAccount returns DuplicateAccountTypeError if account type is duplicated`(){
        whenever(linkedAccountRepo.create(any()))
            .thenThrow(IllegalArgumentException())

        val actual = service.createOAuthAccount(validUserId, validProvider)
        assertTrue(actual.isFailure())
        assertEquals(DuplicateAccountTypeError, actual.leftOrNull())
    }

    //TODO: IMPLEMENT TESTS FOR REST OF METHODS.
}