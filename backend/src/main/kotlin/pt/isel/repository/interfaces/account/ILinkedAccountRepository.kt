package pt.isel.repository.interfaces.account

import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.IRepository

/**
 *  `IFormLinkedAccountRepository`
 *
 * Interface that establishes the actions upon the [FormLinkedAccount] entity, extends the CRUD operations of [IRepository]
 * */
interface IFormLinkedAccountRepository : IRepository<FormLinkedAccount> {
    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The [FormLinkedAccount] which the [userId] corresponds to or null if not found.
     * **/
    fun findByUserId(userId: Int): FormLinkedAccount?
}

/**
 *  `IOAuthLinkedAccountRepository`
 *
 * Interface that establishes the actions upon the [OAuthLinkedAccount] entity, extends the CRUD operations of [IRepository]
 * */
interface IOAuthLinkedAccountRepository : IRepository<OAuthLinkedAccount> {
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
    fun findByUserIdAndProviderAndProviderId(
        userId: Int, provider: OAuthProvider, providerId: String
    ): OAuthLinkedAccount?

    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The list of [OAuthLinkedAccount] of git service providers as defined in [OAuthProvider.gitAccounts] which the [userId] corresponds to or empty list if not found.
     * **/
    fun findGitAccounts(userId: Int): List<OAuthLinkedAccount>
}