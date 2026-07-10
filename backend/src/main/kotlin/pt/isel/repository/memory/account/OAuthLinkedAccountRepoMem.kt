package pt.isel.repository.memory.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.domain.account.OAuthProvider
import pt.isel.domain.account.OAuthProvider.Companion.gitAccounts
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.memory.RepoMem

/**
 *  `OAuthLinkedAccountRepoMem`
 *
 * Class that implements the in-memory operations of the [OAuthLinkedAccount] entity, also extends CRUD implementation of [RepoMem].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"], havingValue = "memory", matchIfMissing = true
)
class OAuthLinkedAccountRepoMem : RepoMem<OAuthLinkedAccount>(), IOAuthLinkedAccountRepository {
    /** @param [id] the identifier of this account in the persistence.
     * @param [userId] The identifier of the [User] this account is linked to. Used to guarantee ownership.
     * @return The [OAuthLinkedAccount] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    override fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccount? =
        persistence.values.firstOrNull { it.id == id && it.user.id == userId }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @return A list of [OAuthLinkedAccount] which the parameters correspond to or empty list if not found.
     * **/
    override fun findByUserIdAndProvider(userId: Int, provider: OAuthProvider): List<OAuthLinkedAccount> =
        persistence.values.filter { it.user.id == userId && it.provider == provider }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @param [providerId] The unique id of a provider account.
     * @return The [OAuthLinkedAccount] which the parameters correspond to or null if not found.
     * **/
    override fun findByUserIdAndProviderAndProviderId(
        userId: Int, provider: OAuthProvider, providerId: String
    ): OAuthLinkedAccount? =
        persistence.values.firstOrNull { it.user.id == userId && it.provider == provider && it.providerId == providerId }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The list of [OAuthLinkedAccount] of git service providers as defined in [OAuthProvider.gitAccounts] which the [userId] corresponds to or empty list if not found.
     * **/
    override fun findGitAccounts(userId: Int): List<OAuthLinkedAccount> =
        persistence.values.filter { it.user.id == userId && it.provider in gitAccounts }
}