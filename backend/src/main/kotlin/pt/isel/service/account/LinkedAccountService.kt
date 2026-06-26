package pt.isel.service.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.entity.User
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.service.ServiceError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.map
import pt.isel.utils.success
import pt.isel.utils.toEither

sealed class LinkedAccountServiceError : ServiceError
object PasswordEncodingError : LinkedAccountServiceError()
object AccountTypeMaxedError : LinkedAccountServiceError()
object AccountNotFoundError : LinkedAccountServiceError()
object LinkedAccountDomainError : LinkedAccountServiceError()

@Service
class LinkedAccountService(
    private val formLinkedAccountRepo: IFormLinkedAccountRepository,
    private val oauthLinkedAccountRepo: IOAuthLinkedAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createFormAccount(user: User, password: String): Either<LinkedAccountServiceError, FormLinkedAccount> {
        val passwordHash = passwordEncoder.encode(password) ?: return failure(PasswordEncodingError)
        val account = FormLinkedAccount.create(userId = user.id, passwordHash = passwordHash)

        if(isFormAccountMaxed(user.id)) return failure(AccountTypeMaxedError)
        return success(formLinkedAccountRepo.create(account.toEntity(user)).toDomain())
    }

    fun createOAuthAccount(
        user: User, provider: String, providerId : String
    ): Either<LinkedAccountServiceError, OAuthLinkedAccount> {
        try{
            val account = OAuthLinkedAccount.create(userId = user.id, provider = provider, providerId = providerId)
            if(isOAuthAccountTypeMaxed(user.id, OAuthAccountProvider.fromString(provider))) return failure(AccountTypeMaxedError)
            return success(oauthLinkedAccountRepo.create(account.toEntity(user)).toDomain())
        } catch (_: Exception) {
            return failure(LinkedAccountDomainError)
        }
    }

    fun findUserFormAccount(userId: Int) : Either<AccountNotFoundError, FormLinkedAccount> =
        formLinkedAccountRepo.findByUserId(userId)
            .toEither { AccountNotFoundError }
            .map{ it.toDomain() }

    fun findUserOAuthAccount(id: Int, userId: Int) =
        oauthLinkedAccountRepo.findByIdAndUserId(id, userId)
            .toEither { AccountNotFoundError }
            .map{ it.toDomain() }

    fun findUserOAuthAccount(userId: Int, provider: OAuthAccountProvider, providerId: String) =
        oauthLinkedAccountRepo.findByUserAndProviderAndProviderId(userId, provider, providerId)
            .toEither { AccountNotFoundError }
            .map{ it.toDomain() }

    fun findUserGithubAccounts(userId: Int): Either<AccountNotFoundError, List<OAuthLinkedAccount>>{
        val accounts = oauthLinkedAccountRepo.findByUserAndProvider(userId, OAuthAccountProvider.GITHUB)
        return if(accounts.isEmpty()) return failure(AccountNotFoundError)
        else success(accounts.map{ it.toDomain() })
    }

    fun updateOAuthAccount(
        userId: Int,
        provider: OAuthAccountProvider, providerId: String,
        accessToken: String?, refreshToken: String?
    ): Either<LinkedAccountServiceError, OAuthLinkedAccount> {
        val account = oauthLinkedAccountRepo.findByUserAndProviderAndProviderId(userId, provider, providerId)
            ?: return failure(AccountNotFoundError)

        val updated = account.toDomain().copy(
            accessToken = accessToken ?: account.accessToken,
            refreshToken = refreshToken ?: account.refreshToken
        )

        return oauthLinkedAccountRepo.update(updated.toEntity(account.user))
            .toEither{ AccountNotFoundError }
            .map{ it.toDomain() }
    }

    private fun isFormAccountMaxed(userId: Int): Boolean =
        formLinkedAccountRepo.findByUserId(userId) != null

    private fun isOAuthAccountTypeMaxed(userId: Int, type: OAuthAccountProvider): Boolean{
        if(type.max == null) return false
        val accountQuantity = oauthLinkedAccountRepo.findByUserAndProvider(userId, type).size
        return accountQuantity >= type.max
    }
}