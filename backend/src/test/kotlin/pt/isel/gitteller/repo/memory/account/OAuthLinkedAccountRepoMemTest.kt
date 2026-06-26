package pt.isel.gitteller.repo.memory.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.entity.User
import pt.isel.gitteller.repo.memory.RepoMemTest
import pt.isel.repository.memory.RepoMem
import pt.isel.repository.memory.account.linkedAccount.OAuthLinkedAccountRepoMem
import kotlin.test.Test

class OAuthLinkedAccountRepoMemTest: RepoMemTest<OAuthLinkedAccountEntity> {
    private lateinit var repo: OAuthLinkedAccountRepoMem

    val validUserId = 0
    val validProvider = OAuthAccountProvider.GOOGLE
    val validProviderId = "testProviderId"

    val validAccessToken = "testAccessToken"
    val validRefreshToken = "testRefreshToken"

    val validUser = User(validUserId, "test@email.com", "test")

    private fun newOAuthLinkedAccountEntity(
        id: Int = 0, user: User = validUser,
        accessToken: String = validAccessToken, refreshToken: String = validRefreshToken,
        provider : OAuthAccountProvider = validProvider, providerId : String = validProviderId
    ) = OAuthLinkedAccountEntity(id, user, accessToken, refreshToken, provider, providerId)

    @BeforeEach
    fun setup(){ repo = OAuthLinkedAccountRepoMem() }

    @Test
    fun `method findByIdAndUserId returns OAuthLinkedAccount of given id associated with user`(){
        val gitAccount1 = repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GITHUB))
        repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GITHUB, providerId = "2"))

        val actual = repo.findByIdAndUserId(gitAccount1.id, validUser.id)

        assertEquals(gitAccount1, actual)
    }

    @Test
    fun `method findByIdAndUserId returns null if user does not own account`(){
        val gitAccount1 = repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GITHUB))

        val actual = repo.findByIdAndUserId(gitAccount1.id, validUser.id + 1)

        assertNull(actual)
    }

    @Test
    fun `method findByUserAndProvider returns all OAuthLinkedAccount associated with user of given provider`(){
        val gitAccount1 = repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GITHUB))
        val gitAccount2 = repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GITHUB, providerId = "2"))

        val expected = listOf(gitAccount1, gitAccount2)

        repo.create(newOAuthLinkedAccountEntity(user = validUser, provider = OAuthAccountProvider.GOOGLE))
        repo.create(newOAuthLinkedAccountEntity(user = validUser.copy(id = validUser.id + 1), provider = OAuthAccountProvider.GITHUB))

        val actual = repo.findByUserAndProvider(validUserId, expected.first().provider)
        assertEquals(expected, actual)
    }

    @Test
    fun `method findByUserAndProviderAndProviderId returns specific OAuthLinkedAccount`(){
        val expected = repo.create(newOAuthLinkedAccountEntity(user = validUser))
        val actual = repo.findByUserAndProviderAndProviderId(
            expected.user.id, provider = expected.provider, expected.providerId
        )
        assertEquals(expected, actual)
    }

    override fun createRepo(): RepoMem<OAuthLinkedAccountEntity> =
        OAuthLinkedAccountRepoMem()

    override fun createEntity(): OAuthLinkedAccountEntity =
        newOAuthLinkedAccountEntity()

    override fun updateEntity(entity: OAuthLinkedAccountEntity): OAuthLinkedAccountEntity {
        entity.accessToken = "anotherAccessToken"
        return entity
    }
}