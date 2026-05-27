package pt.isel.service.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.interfaces.account.ILinkedAccountRepository
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.toEither

sealed class LinkedAccountServiceError : AccountServiceError()
object PasswordEncodingError : LinkedAccountServiceError()
object AccountTypeMaxedError : LinkedAccountServiceError()
object AccountNotFoundError : LinkedAccountServiceError()
object UserAccountsNotFoundError : LinkedAccountServiceError()
object LinkedAccountDomainError : LinkedAccountServiceError()

@Service
class LinkedAccountService(
    private val linkedAccountRepo: ILinkedAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createFormAccount(userId: Int, password: String): Either<LinkedAccountServiceError, FormLinkedAccount> {
        val passwordHash = passwordEncoder.encode(password) ?: return failure(PasswordEncodingError)
        val account = FormLinkedAccount.create(userId = userId, passwordHash = passwordHash)
        return createLinkedAccount(account)
    }

    fun createOAuthAccount(
        userId: Int, provider: String, providerId : String
    ): Either<AccountTypeMaxedError, OAuthLinkedAccount> {
        val account = OAuthLinkedAccount.create(userId = userId, provider = provider, providerId = providerId)
        return createLinkedAccount(account)
    }

    fun read(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.findById(id).toEither { AccountNotFoundError }

    fun readByUser(userId: Int): Either<UserAccountsNotFoundError, List<LinkedAccount>> =
        linkedAccountRepo.readByUser(userId).toEither { UserAccountsNotFoundError }

    fun readByUserAndType(userId: Int, type: String): Either<LinkedAccountServiceError, List<LinkedAccount>> {
        val account = findByUserAndType(userId, type) ?: return identifyMissingAccountError(userId)
        return success(account)
    }

    fun readByUserTypeAndKey(userId: Int, type: String, key: String): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = findByUserTypeAndKey(userId, type, key)
            ?: return identifyMissingAccountError(userId)

        return success(account)
    }

    fun update(
        userId: Int,
        type: String,
        key: String = "",
        passwordHash: String? = null,
        accessToken: OAuth2AccessToken? = null,
        refreshToken: OAuth2RefreshToken? = null,
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        return when(type){
            AccountType.FORM.type -> updateFormAccount(userId, passwordHash)
            else -> updateOAuthAccount(userId, type, key, accessToken, refreshToken)
        }
    }

    fun delete(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.delete(id).toEither { AccountNotFoundError }

    fun deleteByUserTypeAndKey(
        userId: Int, type: String, key: String
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        val removedAccount = linkedAccountRepo.deleteByUserTypeAndKey(userId, type, key)
            ?: return identifyMissingAccountError(userId)

        return success(removedAccount)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : LinkedAccount> createLinkedAccount(
        linkedAccount: T,
    ): Either<AccountTypeMaxedError, T> {
        return if (isAccountTypeMaxed(linkedAccount.userId, linkedAccount.getType())) {
            failure(AccountTypeMaxedError)
        } else success(linkedAccountRepo.create(linkedAccount) as T)
    }

    fun findByUserAndType(userId: Int, type: String) =
        linkedAccountRepo.readByUserAndType(userId, type)

    fun findByUserTypeAndKey(userId: Int, type: String, key: String) =
        linkedAccountRepo.readByUserTypeAndKey(userId, type, key)


    private fun updateFormAccount(
        userId: Int, passwordHash: String?,
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserTypeAndKey(userId, AccountType.FORM.type, null)
                as? FormLinkedAccount? ?: return identifyMissingAccountError(userId)

        val updated = try{
            account.copy(passwordHash = passwordHash ?: account.passwordHash)
        } catch(e: IllegalArgumentException){ return failure(LinkedAccountDomainError) }

        return persistUpdate(updated)
    }

    private fun updateOAuthAccount(
        userId: Int,
        provider: String,
        key: String,
        accessToken: OAuth2AccessToken?,
        refreshToken: OAuth2RefreshToken?
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserTypeAndKey(userId, provider, key)
                as? OAuthLinkedAccount? ?: return identifyMissingAccountError(userId)

        val updated = try{
            account.copy(
                accessToken = accessToken ?: account.accessToken,
                refreshToken = refreshToken ?: account.refreshToken
            )
        } catch(e: IllegalArgumentException){ return failure(LinkedAccountDomainError) }

        return persistUpdate(updated)
    }

    private fun persistUpdate(updated: LinkedAccount): Either<AccountNotFoundError, LinkedAccount> {
        return linkedAccountRepo.update(updated).toEither{ AccountNotFoundError }
    }

    private fun isAccountTypeMaxed(userId: Int, type: AccountType): Boolean{
        if(type.max == null) return false
        val accountQuantity = linkedAccountRepo.readByUserAndType(userId, type.type)?.size ?: return false
        return accountQuantity >= type.max
    }

    private fun identifyMissingAccountError(userId: Int) =
        if (linkedAccountRepo.readByUser(userId) == null) failure(UserAccountsNotFoundError)
        else failure(AccountNotFoundError)
}