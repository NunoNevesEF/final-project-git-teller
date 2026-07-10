package pt.isel.gitteller.repository.interfaces.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.RepoTest
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.interfaces.account.IUserRepository
import kotlin.test.Test

abstract class OAuthLinkedAccountRepoTest: RepoTest<OAuthLinkedAccount> {
    val validId = 0
    val validProvider = OAuthProvider.GOOGLE
    val validProviderId = "someId"


    val validEmail = "test@email.com"
    val validUsername = "test"
    val validUser = User(validId, validEmail, validUsername)


    abstract override fun repo(): IOAuthLinkedAccountRepository
    abstract fun userRepo(): IUserRepository

    fun createOAuthLinkedAccount(
        user: User = validUser,
        accessToken: String = "",
        refreshToken: String = "",
        provider: OAuthProvider = validProvider,
        providerId: String = validProviderId
    ): OAuthLinkedAccount = OAuthLinkedAccount(0, user, accessToken, refreshToken, provider, providerId)

    override fun createEntity(): OAuthLinkedAccount{
        val user = userRepo().create(validUser)

        return createOAuthLinkedAccount(user = user)
    }

    override fun updateEntity(entity: OAuthLinkedAccount): OAuthLinkedAccount =
        entity.copy(accessToken = "some new accessToken")

    override fun assertEquality(expected: OAuthLinkedAccount, actual: OAuthLinkedAccount?) {
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.user.id, actual?.user?.id)
        assertEquals(expected.provider, actual?.provider)
        assertEquals(expected.providerId, actual?.providerId)
        assertEquals(expected.accessToken, actual?.accessToken)
        assertEquals(expected.refreshToken, actual?.refreshToken)
    }

    @Test
    fun `method findByIdAndUserId returns correct OAuthLinkedAccount`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected = repo.create(createOAuthLinkedAccount(user = user))

        val actual = repo.findByIdAndUserId(expected.id, expected.user.id)

        assertEquality(expected, actual)
    }

    @Test
    fun `method findByIdAndUserId returns null if userId not found`() {
        val repo = repo()

        val actual = repo.findByIdAndUserId(1, 1)

        assertNull(actual)
    }

    @Test
    fun `method findByUserAndProvider returns list of provider OAuthLinkedAccount`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected1 = repo.create(createOAuthLinkedAccount(user = user, provider = validProvider))
        val expected2 = repo.create(createOAuthLinkedAccount(user = user, provider = validProvider))

        val expected = listOf(expected1, expected2)

        val actual = repo.findByUserIdAndProvider(user.id, validProvider)

        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { index, expectedAccount -> assertEquality(expectedAccount, actual[index]) }
    }

    @Test
    fun `method findByUserAndProvider returns emptyList if wrong provider`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        repo.create(createOAuthLinkedAccount(user = user, provider = OAuthProvider.GOOGLE))

        val actual = repo.findByUserIdAndProvider(user.id, OAuthProvider.GITHUB)

        assertEquals(emptyList<OAuthLinkedAccount>(), actual)
    }

    @Test
    fun `method findByUserAndProviderAndProviderId returns correct OAuthLinkedAccount`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected = createOAuthLinkedAccount(user = user, provider = OAuthProvider.GOOGLE, providerId = validProviderId)
        val anotherProvider = createOAuthLinkedAccount(user = user, provider = OAuthProvider.GITHUB, providerId = validProviderId)
        val anotherProviderId = createOAuthLinkedAccount(user = user, provider = OAuthProvider.GOOGLE, providerId = expected.providerId + "1")

        repo.create(expected)
        repo.create(anotherProvider)
        repo.create(anotherProviderId)

        val actual = repo.findByUserIdAndProviderAndProviderId(user.id, expected.provider, expected.providerId)

        assertEquality(expected, actual)
    }

    @Test
    fun `method findByUserAndProviderAndProviderId returns null if userId not found`() {
        val repo = repo()

        val actual = repo.findByUserIdAndProviderAndProviderId(1, OAuthProvider.GITHUB, "1")

        assertNull(actual)
    }

    @Test
    fun `method findGitAccounts returns user gitAccounts`(){
        val repo = repo()
        val userRepo = userRepo()

        val user1 = userRepo.create(validUser)
        val user2 = userRepo.create(User(email = "some other email", username = "some other username"))

        val githubAccount1 = createOAuthLinkedAccount(user = user1, provider = OAuthProvider.GITHUB, providerId = validProviderId)
        val githubAccount2 = createOAuthLinkedAccount(user = user1, provider = OAuthProvider.GITHUB, providerId = validProviderId + "1")
        val gitlabAccount = createOAuthLinkedAccount(user = user1, provider = OAuthProvider.GITLAB, providerId = validProviderId)

        val anotherUser = createOAuthLinkedAccount(
            user = user2, provider = OAuthProvider.GITHUB, providerId = validProviderId
        )

        val nonGitProvider = createOAuthLinkedAccount(user = user1, provider = OAuthProvider.GOOGLE, providerId = validProviderId)

        val expected1 = repo.create(githubAccount1)
        val expected2 = repo.create(githubAccount2)
        val expected3 = repo.create(gitlabAccount)
        repo.create(anotherUser)
        repo.create(nonGitProvider)

        val expected = listOf(expected1, expected2, expected3)

        val actual = repo.findGitAccounts(githubAccount1.user.id)

        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { index, expectedAccount -> assertEquality(expectedAccount, actual[index]) }
    }

    @Test
    fun `method findGitAccounts returns emptyList if user has no git accounts`() {
        val repo = repo()

        val testAccount = createOAuthLinkedAccount(user = validUser, provider = OAuthProvider.GOOGLE)

        val actual = repo.findGitAccounts(testAccount.user.id)

        assertEquals(emptyList<OAuthLinkedAccount>(), actual)
    }
}