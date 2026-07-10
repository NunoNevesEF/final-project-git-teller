package pt.isel.service.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthAccountProviderException
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.LinkedAccountEntityException
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.LinkedAccountServiceError
import pt.isel.infraestructure.security.PasswordAuthenticationConfig
import pt.isel.service.error.UnexpectedProvider
import pt.isel.service.error.toServiceError
import pt.isel.service.error.InvalidProvider
import pt.isel.service.error.InvalidProviderID
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.flatMap
import pt.isel.utils.onFailure
import pt.isel.utils.rightOrNull
import pt.isel.utils.success
import pt.isel.utils.toEither

/**
 *  `LinkedAccountService`
 *
 * The service layer linked-account-related operations
 *
 * @property [formLinkedAccountRepo] the form account repository used to communicate with the persistence layer.
 * @property [oauthLinkedAccountRepo] the oauth account repository used to communicate with the persistence layer.
 * @property [passwordEncoder] the password encoder used by the application. Configured in [PasswordAuthenticationConfig]
 * */
@Service
class LinkedAccountService(
    private val formLinkedAccountRepo: IFormLinkedAccountRepository,
    private val oauthLinkedAccountRepo: IOAuthLinkedAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * @param [user] The [user] being linked to the created [FormLinkedAccount].
     * @param [password] the [password] used in the registration.
     * @return
     * - The created [FormLinkedAccount] in case of success.
     * - [InvalidPassword] error in case the password does not fulfill application restrictions
     * - [LinkedAccountTypeMaxed] error in case there already is a [FormLinkedAccount] with the given [User]
     * @throws [IllegalStateException] if the password encoder unexpectedly returns null.
     * **/
    fun createFormAccount(user: User, password: String): Either<LinkedAccountServiceError, FormLinkedAccount> {
        val normalizedPassword = password.trim()
        if (normalizedPassword.isBlank()) {
            return failure(InvalidPassword)
        }
        val passwordHash = checkNotNull(passwordEncoder.encode(normalizedPassword))

        val account = FormLinkedAccount(user = user, passwordHash = passwordHash)

        validateFormAccountCanBeCreated(user.id).onFailure { return failure(it) }

        return success(formLinkedAccountRepo.create(account))
    }

    /**
     * @param [user] The [user] being linked to the created [FormLinkedAccount].
     * @param providerName the name of the provider to map to [OAuthProvider].
     * @param providerId the provider unique identifier for the user provider account.
     * @return
     * - The created [OAuthLinkedAccount] in case of success.
     * - [InvalidProvider] if [providerName] does not match any [OAuthProvider]
     * - [InvalidProviderID] error in case the given provider id does not fulfill application restrictions.
     * - [LinkedAccountTypeMaxed] error in case there already is the max number supported [OAuthLinkedAccount] of [providerName] for the given [User]
     * @throws [IllegalStateException] if password encoding fails
     * **/
    fun createOAuthAccount(
        user: User, providerName: String, providerId: String
    ): Either<LinkedAccountServiceError, OAuthLinkedAccount> {
        try {
            val account = OAuthLinkedAccount(user = user, providerName = providerName, providerId = providerId)

            validateOAuthAccountCanBeCreated(user.id, account.provider).onFailure { return failure(it) }

            return success(oauthLinkedAccountRepo.create(account))
        } catch (e: LinkedAccountEntityException) {
            return failure(e.toServiceError())
        } catch (e: OAuthAccountProviderException) {
            return failure(e.toServiceError())
        }
    }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @return
     * - The [FormLinkedAccount] in case of success.
     * - [LinkedAccountNotFound] error if not found.
     * **/
    fun findUserFormAccount(userId: Int): Either<LinkedAccountNotFound, FormLinkedAccount> =
        formLinkedAccountRepo.findByUserId(userId).toEither { LinkedAccountNotFound }

    /** @param [id] the identifier of this account in the persistence.
     * @param [userId] The identifier of the [User] this account is linked to. Used to guarantee ownership.
     * @return
     * - The [OAuthLinkedAccount] in case of success.
     * - [LinkedAccountNotFound] error if not found.
     * **/
    fun findUserOAuthAccount(id: Int, userId: Int) =
        oauthLinkedAccountRepo.findByIdAndUserId(id, userId).toEither { LinkedAccountNotFound }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [providerName] The name of the provider which corresponds to the accounts we are looking for.
     * @param [providerId] The unique id of a provider account.
     * @return
     * - The [OAuthLinkedAccount] in case of success.
     * - [LinkedAccountNotFound] error if not found.
     * - [InvalidProvider] if [providerName] does not match any [OAuthProvider]
     * **/
    fun findUserOAuthAccount(userId: Int, providerName: String, providerId: String): Either<LinkedAccountServiceError, OAuthLinkedAccount> {
        return try {
            oauthLinkedAccountRepo.findByUserIdAndProviderAndProviderId(
                userId, OAuthProvider.fromString(providerName), providerId
            ).toEither { LinkedAccountNotFound }
        } catch (e: OAuthAccountProviderException) {
            failure(e.toServiceError())
        }
    }

    /** @param [id] the identifier of this account in the persistence.
     * @param [userId] The identifier of the [User] this account is linked to. Used to guarantee ownership.
     * @return
     * - The [OAuthLinkedAccount] corresponding to a Git account in case of success.
     * - [LinkedAccountNotFound] error if not found.
     * - [UnexpectedProvider] if account's provider is not a Git service account as defined in [OAuthProvider.gitAccounts]
     * **/
    fun findUserGitAccount(id: Int, userId: Int) =
        findUserOAuthAccount(id, userId)
            .flatMap {
                if(it.provider !in OAuthProvider.gitAccounts) failure(UnexpectedProvider)
                else success(it)
            }


    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The list of [OAuthLinkedAccount] of git service providers as defined in [OAuthProvider.gitAccounts] which the [userId] corresponds
     * (can be empty)
     * **/
    fun findUserGitAccounts(userId: Int): List<OAuthLinkedAccount> = oauthLinkedAccountRepo.findGitAccounts(userId)

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [providerName] The name of the provider which corresponds to the accounts we are looking for.
     * @param [providerId] The unique id of a provider account.
     * @param [newAccessToken] The new accessToken to insert into the [OAuthLinkedAccount]. Null if old value is to be kept.
     * @param [newRefreshToken] The new refreshToken to insert into the [OAuthLinkedAccount]. Null if old value is to be kept.
     * @return
     * - The updated [OAuthLinkedAccount] if success
     * - The old [OAuthLinkedAccount] if both [newAccessToken] and [newRefreshToken] are null.
     * - [InvalidProvider]  if [providerName] does not match any [OAuthProvider]
     * - [LinkedAccountNotFound] if old account not found at read or when updating.
     * **/
    fun updateOAuthAccount(
        userId: Int,
        providerName: String,
        providerId: String,
        newAccessToken: String? = null,
        newRefreshToken: String? = null
    ): Either<LinkedAccountServiceError, OAuthLinkedAccount> {
        val accountResult = findUserOAuthAccount(userId, providerName, providerId).onFailure { return failure(it) }
        val account = accountResult.rightOrNull()!!

        if (newAccessToken == null && newRefreshToken == null) return success(account)

        val updated = account.copy(
            accessToken = newAccessToken ?: account.accessToken, refreshToken = newRefreshToken ?: account.refreshToken
        )

        return oauthLinkedAccountRepo.update(updated).toEither { LinkedAccountNotFound }
    }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @return [LinkedAccountTypeMaxed] if there's already a [FormLinkedAccount] associated with this [User].
     * **/
    private fun validateFormAccountCanBeCreated(userId: Int): Either<LinkedAccountTypeMaxed, Unit> {
        formLinkedAccountRepo.findByUserId(userId)?.let {
            return failure(LinkedAccountTypeMaxed)
        }
        return success(Unit)
    }

    /** @param [userId] The identifier of the [User] this account is linked to
     * @param [provider] The provider whose accounts we are checking
     * @return [LinkedAccountTypeMaxed] if there's already a max number of [OAuthLinkedAccount] of [provider] associated with this [User].
     * **/
    private fun validateOAuthAccountCanBeCreated(
        userId: Int, provider: OAuthProvider
    ): Either<LinkedAccountTypeMaxed, Unit> {
        val max = provider.maxPerUser ?: return success(Unit)
        val count = oauthLinkedAccountRepo.findByUserIdAndProvider(userId, provider).size

        if (count >= max) {
            return failure(LinkedAccountTypeMaxed)
        }
        return success(Unit)
    }
}