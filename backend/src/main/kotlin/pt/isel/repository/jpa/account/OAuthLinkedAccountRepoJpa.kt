package pt.isel.repository.jpa.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.jpa.RepoJpaAdapter

/**
 *  `OAuthLinkedAccountRepoJpa`
 *
 * Interface that extends the [JpaRepository] with [OAuthLinkedAccount] entity specific operations.
 * The implementation of these is automatically resolved by JPA.
 * */
@Repository
interface OAuthLinkedAccountRepoJpa: JpaRepository<OAuthLinkedAccount, Int>{
    /** @param [id] the identifier of this account in the persistence.
     * @param [userId] The identifier of the [User] this account is linked to. Used to guarantee ownership.
     * @return The [OAuthLinkedAccount] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccount?

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @return A list of [OAuthLinkedAccount] which the parameters correspond to or empty list if not found.
     * **/
    fun findByUserIdAndProvider(userId: Int, provider: OAuthProvider): List<OAuthLinkedAccount>

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @param [providerId] The unique id of a provider account.
     * @return The [OAuthLinkedAccount] which the parameters correspond to or null if not found.
     * **/
    fun findByUserIdAndProviderAndProviderId(userId: Int, provider: OAuthProvider, providerId: String): OAuthLinkedAccount?

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [providers] The list of providers used to match to the accounts to return
     * @return The list of [OAuthLinkedAccount] of [providers] which the [userId] corresponds to or empty list if not found.
     * **/
    fun findByUserIdAndProviderIn(userId: Int, providers: Set<OAuthProvider>): List<OAuthLinkedAccount>
}

/**
 *  `OAuthLinkedAccountRepoAdapter`
 *
 * Class that implements the jpa operations of the [OAuthLinkedAccount] entity, also extends CRUD implementation of [RepoJpaAdapter].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class OAuthLinkedAccountRepoJpaAdapter(
    jpa: OAuthLinkedAccountRepoJpa
) : RepoJpaAdapter<OAuthLinkedAccount, OAuthLinkedAccountRepoJpa>(jpa), IOAuthLinkedAccountRepository{
    /** @param [id] the identifier of this account in the persistence.
     * @param [userId] The identifier of the [User] this account is linked to. Used to guarantee ownership.
     * @return The [OAuthLinkedAccount] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    override fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccount? =
        jpa.findByIdAndUserId(id, userId)

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @return A list of [OAuthLinkedAccount] which the parameters correspond to or empty list if not found.
     * **/
    override fun findByUserIdAndProvider(userId: Int, provider: OAuthProvider): List<OAuthLinkedAccount> =
        jpa.findByUserIdAndProvider(userId, provider)

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider which corresponds to the accounts we are looking for.
     * @param [providerId] The unique id of a provider account.
     * @return The [OAuthLinkedAccount] which the parameters correspond to or null if not found.
     * **/
    override fun findByUserIdAndProviderAndProviderId(userId: Int, provider: OAuthProvider, providerId: String): OAuthLinkedAccount? =
        jpa.findByUserIdAndProviderAndProviderId(userId, provider, providerId)

    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The list of [OAuthLinkedAccount] of git service providers as defined in [OAuthProvider.gitAccounts] which the [userId] corresponds to or empty list if not found.
     * **/
    override fun findGitAccounts(userId: Int): List<OAuthLinkedAccount> =
        jpa.findByUserIdAndProviderIn(userId, OAuthProvider.gitAccounts)
}