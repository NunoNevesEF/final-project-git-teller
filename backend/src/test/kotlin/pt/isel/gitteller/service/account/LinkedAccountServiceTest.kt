package pt.isel.gitteller.service.account

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.InvalidProvider
import pt.isel.service.error.InvalidProviderID
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.UnexpectedProvider
import pt.isel.utils.Failure
import pt.isel.utils.Success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class LinkedAccountServiceTest() {
    @Mock
    private lateinit var formLinkedAccountRepo: IFormLinkedAccountRepository

    @Mock
    private lateinit var oauthLinkedAccountRepo: IOAuthLinkedAccountRepository

    @InjectMocks
    lateinit var service: LinkedAccountService

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    val validId = 0

    val validAccessToken = "testAccessToken"
    val validRefreshToken = "testRefreshToken"
    val validProvider = OAuthProvider.GOOGLE
    val validProviderId = "1"

    val validPassword = "testPassword"
    val validPasswordHash = "testPasswordHash"

    val validUser = User(0, "test@email.com", "test")

    private fun newFormLinkedAccount(
        id: Int = validId, user: User = validUser, passwordHash: String = validPasswordHash
    ) = FormLinkedAccount(id, user, passwordHash)

    private fun newOAuthLinkedAccount(
        id: Int = validId,
        user: User = validUser,
        accessToken: String = validAccessToken,
        refreshToken: String = validRefreshToken,
        provider: OAuthProvider = validProvider,
        providerId: String = validProviderId
    ) = OAuthLinkedAccount(id, user, accessToken, refreshToken, provider, providerId)


    @Test
    fun `method createFormAccount returns created FormLinkedAccount on success`() {
        val expected = newFormLinkedAccount()

        whenever(passwordEncoder.encode(validPassword)).thenReturn(expected.passwordHash)

        whenever(formLinkedAccountRepo.findByUserId(validUser.id)).thenReturn(null)

        whenever(formLinkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createFormAccount(validUser, validPassword)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(passwordEncoder).encode(validPassword)
        verify(formLinkedAccountRepo).findByUserId(validUser.id)
        verify(formLinkedAccountRepo).create(any())
    }

    @Test
    fun `method createFormAccount hashes trimmed password`() {
        whenever(passwordEncoder.encode("password")).thenReturn(validPasswordHash)

        whenever(formLinkedAccountRepo.findByUserId(validUser.id)).thenReturn(null)

        whenever(formLinkedAccountRepo.create(any())).thenReturn(newFormLinkedAccount())

        val captor = argumentCaptor<FormLinkedAccount>()

        service.createFormAccount(validUser, "  password  ")

        verify(formLinkedAccountRepo).create(captor.capture())

        assertEquals(validPasswordHash, captor.firstValue.passwordHash)

        verify(passwordEncoder).encode("password")
    }

    @Test
    fun `method createFormAccount returns InvalidPassword when password is blank`() {
        val actual = service.createFormAccount(validUser, "   ")

        assertTrue(actual is Failure)
        assertEquals(InvalidPassword, actual.left)

        verifyNoInteractions(passwordEncoder)
        verifyNoInteractions(formLinkedAccountRepo)
    }

    @Test
    fun `method createFormAccount returns LinkedAccountTypeMaxed when form account already exists`() {
        whenever(passwordEncoder.encode(validPassword)).thenReturn(validPasswordHash)

        whenever(formLinkedAccountRepo.findByUserId(validUser.id)).thenReturn(newFormLinkedAccount())

        val actual = service.createFormAccount(validUser, validPassword)

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountTypeMaxed, actual.left)

        val inOrder = inOrder(formLinkedAccountRepo)
        inOrder.verify(formLinkedAccountRepo).findByUserId(validUser.id)
        inOrder.verify(formLinkedAccountRepo, never()).create(any())
    }

    @Test
    fun `method createFormAccount throws IllegalStateException when password encoding fails`() {
        whenever(passwordEncoder.encode(validPassword)).thenReturn(null)

        assertFailsWith<IllegalStateException> {
            service.createFormAccount(validUser, validPassword)
        }

        verify(passwordEncoder).encode(validPassword)
        verifyNoInteractions(formLinkedAccountRepo)
    }

    @Test
    fun `method createOAuthAccount returns OAuthLinkedAccount on success`() {
        val expected = newOAuthLinkedAccount()

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProvider(validUser.id, validProvider)
        ).thenReturn(emptyList())

        whenever(oauthLinkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createOAuthAccount(
            validUser, validProvider.providerName, validProviderId
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        val inOrder = inOrder(oauthLinkedAccountRepo)

        inOrder.verify(oauthLinkedAccountRepo).findByUserIdAndProvider(validUser.id, validProvider)
        inOrder.verify(oauthLinkedAccountRepo).create(any())
    }

    @Test
    fun `method createOAuthAccount returns LinkedAccountTypeMaxed when provider is maxed`() {
        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProvider(
                validUser.id, OAuthProvider.GOOGLE
            )
        ).thenReturn(listOf(newOAuthLinkedAccount()))

        val actual = service.createOAuthAccount(
            validUser, OAuthProvider.GOOGLE.providerName, validProviderId
        )

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountTypeMaxed, actual.left)

        verify(oauthLinkedAccountRepo, never()).create(any())
    }

    @Test
    fun `method createOAuthAccount succeeds when provider has no maximum`() {
        val expected = newOAuthLinkedAccount(provider = OAuthProvider.GITHUB)

        whenever(oauthLinkedAccountRepo.create(any())).thenReturn(expected)

        val actual = service.createOAuthAccount(
            validUser, OAuthProvider.GITHUB.providerName, validProviderId
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(oauthLinkedAccountRepo, never()) //verifies it doesn't check DB for count
            .findByUserIdAndProvider(any(), any())
    }

    @Test
    fun `method createOAuthAccount returns InvalidProvider for unknown provider`() {
        val actual = service.createOAuthAccount(
            validUser, "invalid", validProviderId
        )

        assertTrue(actual is Failure)
        assertEquals(InvalidProvider, actual.left)

        verifyNoInteractions(oauthLinkedAccountRepo)
    }

    @Test
    fun `method createOAuthAccount returns InvalidProviderId on blank provider id`() {
        val actual = service.createOAuthAccount(
            validUser, validProvider.providerName, "  "
        )

        assertTrue(actual is Failure)
        assertEquals(InvalidProviderID, actual.left)

        verifyNoInteractions(oauthLinkedAccountRepo)
    }

    @Test
    fun `method findUserFormAccount returns FormLinkedAccount on success`() {
        val expected = newFormLinkedAccount()

        whenever(formLinkedAccountRepo.findByUserId(validUser.id)).thenReturn(expected)

        val actual = service.findUserFormAccount(validUser.id)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findUserFormAccount returns LinkedAccountNotFound if not found`() {
        whenever(formLinkedAccountRepo.findByUserId(validUser.id)).thenReturn(null)

        val actual = service.findUserFormAccount(validUser.id)

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)
    }

    @Test
    fun `method findUserOAuthAccount by id and userId returns OAuthLinkedAccount on success`() {
        val expected = newOAuthLinkedAccount()

        whenever(oauthLinkedAccountRepo.findByIdAndUserId(validId, validUser.id)).thenReturn(expected)

        val actual = service.findUserOAuthAccount(validId, validUser.id)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findUserOAuthAccount by id and userId returns LinkedAccountNotFound if not found`() {
        whenever(oauthLinkedAccountRepo.findByIdAndUserId(validId, validUser.id)).thenReturn(null)

        val actual = service.findUserOAuthAccount(validId, validUser.id)

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)
    }

    @Test
    fun `method findUserOAuthAccount by provider and providerId returns OAuthLinkedAccount on success`() {
        val expected = newOAuthLinkedAccount()

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(expected)

        val actual = service.findUserOAuthAccount(validUser.id, validProvider.providerName, validProviderId)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findUserOAuthAccount by provider and providerId returns LinkedAccountNotFound if not found`() {
        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(null)

        val actual = service.findUserOAuthAccount(validUser.id, validProvider.providerName, validProviderId)

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)
    }

    @Test
    fun `method findUserOAuthAccount by provider and providerId returns InvalidProvider if invalid provider name`() {
        val actual = service.findUserOAuthAccount(validUser.id, "invalid", validProviderId)

        assertTrue(actual is Failure)
        assertEquals(InvalidProvider, actual.left)

        verifyNoInteractions(oauthLinkedAccountRepo)
    }

    @Test
    fun `method findUserGitAccount returns git OAuthLinkedAccount on success`() {
        val expected = newOAuthLinkedAccount(provider = OAuthProvider.GITHUB)

        whenever(oauthLinkedAccountRepo.findByIdAndUserId(validId, validUser.id))
            .thenReturn(expected)

        val actual = service.findUserGitAccount(validId, validUser.id)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findUserGitAccount returns LinkedAccountNotFound if no account matches`() {
        whenever(oauthLinkedAccountRepo.findByIdAndUserId(validId, validUser.id))
            .thenReturn(null)

        val actual = service.findUserGitAccount(validId, validUser.id)

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)
    }

    @Test
    fun `method findUserGitAccount returns UnexpectedProvider if account provider not git provider`() {
        whenever(oauthLinkedAccountRepo.findByIdAndUserId(validId, validUser.id))
            .thenReturn(newOAuthLinkedAccount(provider = OAuthProvider.GOOGLE))

        val actual = service.findUserGitAccount(validId, validUser.id)

        assertTrue(actual is Failure)
        assertEquals(UnexpectedProvider, actual.left)
    }

    @Test
    fun `method findUserGitAccounts returns list of git OAuthLinkedAccount on success`() {
        val expected1 = newOAuthLinkedAccount(provider = OAuthProvider.GITHUB, providerId = validProviderId)
        val expected2 = newOAuthLinkedAccount(provider = OAuthProvider.GITHUB, providerId = validProviderId + '1')

        val expected = listOf(expected1, expected2)

        whenever(oauthLinkedAccountRepo.findGitAccounts(validUser.id)).thenReturn(listOf(expected1, expected2))

        val actual = service.findUserGitAccounts(validUser.id)

        assertEquals(expected, actual)
    }

    @Test
    fun `method findUserGitAccounts returns empty list if no git OAuthLinkedAccounts`() {
        whenever(oauthLinkedAccountRepo.findGitAccounts(validUser.id)).thenReturn(emptyList())

        val actual = service.findUserGitAccounts(validUser.id)

        assertEquals(emptyList(), actual)
    }

    @Test
    fun `method updateOAuthAccount updates both tokens`() {
        val original = newOAuthLinkedAccount()

        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        val expected = original.copy(accessToken = newAccessToken, refreshToken = newRefreshToken)

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(original)

        whenever(oauthLinkedAccountRepo.update(any())).thenReturn(expected)

        val actual = service.updateOAuthAccount(
            validUser.id, validProvider.providerName, validProviderId,
            newAccessToken, newRefreshToken
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(oauthLinkedAccountRepo).findByUserIdAndProviderAndProviderId(
            validUser.id, validProvider, validProviderId
        )
        verify(oauthLinkedAccountRepo).update(any())
    }

    @Test
    fun `method updateOAuthAccount updates access tokens`() {
        val original = newOAuthLinkedAccount()

        val newAccessToken = "newAccessToken"

        val expected = original.copy(accessToken = newAccessToken)

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(original)

        whenever(oauthLinkedAccountRepo.update(any())).thenReturn(expected)

        val actual = service.updateOAuthAccount(
            validUser.id, validProvider.providerName, validProviderId,
            newAccessToken = newAccessToken,
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(oauthLinkedAccountRepo).findByUserIdAndProviderAndProviderId(
            validUser.id, validProvider, validProviderId
        )
        verify(oauthLinkedAccountRepo).update(any())
    }

    @Test
    fun `method updateOAuthAccount updates refresh tokens`() {
        val original = newOAuthLinkedAccount()

        val newRefreshToken = "newRefreshToken"

        val expected = original.copy(refreshToken = newRefreshToken)

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(original)

        whenever(oauthLinkedAccountRepo.update(any())).thenReturn(expected)

        val actual = service.updateOAuthAccount(
            validUser.id, validProvider.providerName, validProviderId,
            newRefreshToken = newRefreshToken
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(oauthLinkedAccountRepo).findByUserIdAndProviderAndProviderId(
            validUser.id, validProvider, validProviderId
        )
        verify(oauthLinkedAccountRepo).update(any())
    }

    @Test
    fun `method updateOAuthAccount returns original account when no tokens are provided`() {
        val expected = newOAuthLinkedAccount()

        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id, validProvider, validProviderId
            )
        ).thenReturn(expected)

        val actual = service.updateOAuthAccount(
            validUser.id,
            validProvider.providerName,
            validProviderId
        )

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)

        verify(oauthLinkedAccountRepo)
            .findByUserIdAndProviderAndProviderId(
                validUser.id,
                validProvider,
                validProviderId
            )

        verify(oauthLinkedAccountRepo, never()).update(any())
    }

    @Test
    fun `method updateOAuthAccount returns LinkedAccountNotFound if not found`() {
        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id,
                validProvider,
                validProviderId
            )
        ).thenReturn(null)

        val actual = service.updateOAuthAccount(
            validUser.id,
            validProvider.providerName,
            validProviderId,
            newAccessToken = "newAccessToken"
        )

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)

        verify(oauthLinkedAccountRepo, never()).update(any())
    }

    @Test
    fun `method updateOAuthAccount returns LinkedAccountNotFound if entity deleted after read`() {
        whenever(
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                validUser.id,
                validProvider,
                validProviderId
            )
        ).thenReturn(newOAuthLinkedAccount())

        whenever(oauthLinkedAccountRepo.update(any())).thenReturn(null)

        val actual = service.updateOAuthAccount(
            validUser.id,
            validProvider.providerName,
            validProviderId,
            newAccessToken = "newAccessToken"
        )

        assertTrue(actual is Failure)
        assertEquals(LinkedAccountNotFound, actual.left)
    }
    @Test
    fun `method updateOAuthAccount returns InvalidProvider if invalid provider name`() {
        val actual = service.updateOAuthAccount(
            validUser.id,
            "invalid",
            validProviderId,
            newAccessToken = "newAccessToken"
        )

        assertTrue(actual is Failure)
        assertEquals(InvalidProvider, actual.left)

        verifyNoInteractions(oauthLinkedAccountRepo)
    }
}