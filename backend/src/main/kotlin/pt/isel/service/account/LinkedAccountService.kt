package pt.isel.service.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.stereotype.Service
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.toEither

sealed class LinkedAccountServiceError : AccountServiceError()
object PasswordEncodingError : LinkedAccountServiceError()
object DuplicateAccountTypeError : LinkedAccountServiceError()
object AccountNotFoundError : LinkedAccountServiceError()
object UserAccountsNotFoundError : LinkedAccountServiceError()
object InvalidUpdateError : LinkedAccountServiceError()
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

    fun createOAuthAccount(userId: Int, provider: String): Either<DuplicateAccountTypeError, OAuthLinkedAccount> {
        val account = OAuthLinkedAccount.create(userId = userId, provider = provider)
        return createLinkedAccount(account)
    }

    fun read(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.read(id).toEither { AccountNotFoundError }

    fun readByUser(userId: Int): Either<UserAccountsNotFoundError, List<LinkedAccount>> =
        linkedAccountRepo.readByUser(userId).toEither { UserAccountsNotFoundError }

    fun readByUserAndType(userId: Int, type: String): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserAndType(userId, type)
            ?: return identifyMissingAccountError(userId)

        return success(account)
    }

    fun update(
        userId: Int,
        type: String,
        passwordHash: String? = null,
        accessToken: OAuth2AccessToken? = null,
        refreshToken: OAuth2RefreshToken? = null,
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        return if (type == FormLinkedAccount.getType()) {
            updateFormAccount(userId, passwordHash)
        } else {
            updateOAuthAccount(userId, type, accessToken, refreshToken)
        }
    }

    fun delete(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.delete(id).toEither { AccountNotFoundError }

    fun deleteByUserAndType(userId: Int, type: String): Either<LinkedAccountServiceError, LinkedAccount> {
        val removedAccount = linkedAccountRepo.deleteByUserAndType(userId, type)
            ?: return identifyMissingAccountError(userId)

        return success(removedAccount)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : LinkedAccount> createLinkedAccount(
        linkedAccount: T,
    ): Either<DuplicateAccountTypeError, T> {
        return if (isAccountTypeRepeated(linkedAccount.userId, linkedAccount.getType())) {
            failure(DuplicateAccountTypeError)
        } else success(linkedAccountRepo.create(linkedAccount) as T)
    }

    private fun updateFormAccount(
        userId: Int, passwordHash: String?
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserAndType(
            userId, FormLinkedAccount.getType()
        ) as FormLinkedAccount? ?: return identifyMissingAccountError(userId)

        val updated = try{
            account.copy(passwordHash = passwordHash ?: account.passwordHash)
        } catch(e: IllegalArgumentException){ return failure(LinkedAccountDomainError) }

        return persistUpdate(userId, updated)
    }

    private fun updateOAuthAccount(
        userId: Int,
        provider: String,
        accessToken: OAuth2AccessToken?,
        refreshToken: OAuth2RefreshToken?
    ): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserAndType(
            userId, provider
        ) as OAuthLinkedAccount? ?: return identifyMissingAccountError(userId)

        val updated = try{
            account.copy(
                accessToken = accessToken ?: account.accessToken,
                refreshToken = refreshToken ?: account.refreshToken
            )
        } catch(e: IllegalArgumentException){ return failure(LinkedAccountDomainError) }

        return persistUpdate(userId, updated)
    }

    private fun persistUpdate(userId: Int, updated: LinkedAccount): Either<AccountNotFoundError, LinkedAccount> {
        return linkedAccountRepo.update(updated).toEither{ AccountNotFoundError }
    }

    private fun isAccountTypeRepeated(userId: Int, type: String): Boolean =
        linkedAccountRepo.readByUserAndType(userId, type) != null

    private fun identifyMissingAccountError(userId: Int) =
        if (linkedAccountRepo.readByUser(userId) == null) failure(UserAccountsNotFoundError)
        else failure(AccountNotFoundError)
}