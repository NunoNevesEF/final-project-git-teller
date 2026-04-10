package pt.isel.service.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.service.Either
import pt.isel.service.failure
import pt.isel.service.success
import pt.isel.service.toEither

sealed class LinkedAccountServiceError : AccountServiceError()
object PasswordEncodingError : LinkedAccountServiceError()
object DuplicateAccountTypeError : LinkedAccountServiceError()
object AccountNotFoundError : LinkedAccountServiceError()
object UserAccountsNotFoundError : LinkedAccountServiceError()

@Service
class LinkedAccountService(
    private val linkedAccountRepo: ILinkedAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createFormAccount(userId: Int, password: String): Either<LinkedAccountServiceError, FormLinkedAccount> {
        try {
            val passwordHash = passwordEncoder.encode(password) ?: return failure(PasswordEncodingError)
            val account = FormLinkedAccount.create(userId = userId, passwordHash = passwordHash)
            return success(linkedAccountRepo.create(account) as FormLinkedAccount)
        } catch (e: IllegalArgumentException) {
            return failure(DuplicateAccountTypeError)
        }
    }

    fun createOAuthAccount(userId: Int, provider: String): Either<DuplicateAccountTypeError, OAuthLinkedAccount> {
        try {
            val account = OAuthLinkedAccount.create(userId = userId, provider = provider)
            return success(linkedAccountRepo.create(account) as OAuthLinkedAccount)
        } catch (e: IllegalArgumentException) {
            return failure(DuplicateAccountTypeError)
        }
    }

    fun read(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.read(id).toEither { AccountNotFoundError }

    fun readByUser(userId: Int): Either<UserAccountsNotFoundError, List<LinkedAccount>> =
        linkedAccountRepo.readByUser(userId).toEither { UserAccountsNotFoundError }

    fun readByUserAndType(userId: Int, type: String): Either<LinkedAccountServiceError, LinkedAccount> {
        val account = linkedAccountRepo.readByUserAndType(userId, type)

        if (account != null) { return success(account) }

        return if (linkedAccountRepo.readByUser(userId) == null) failure(UserAccountsNotFoundError)
        else failure(AccountNotFoundError)
    }

    fun update() {
        //TODO: IMPLEMENT AFTER THINKING WHAT EXACTLY SHOULD BE UPDATED
    }

    fun delete(id: Int): Either<AccountNotFoundError, LinkedAccount> =
        linkedAccountRepo.delete(id).toEither { AccountNotFoundError }

    fun deleteByUserAndType(userId: Int, type: String): Either<LinkedAccountServiceError, LinkedAccount> {
        val removedAccount = linkedAccountRepo.deleteByUserAndType(userId, type)

        if (removedAccount != null) { return success(removedAccount) }

        return if (linkedAccountRepo.readByUser(userId) == null) failure(UserAccountsNotFoundError)
        else failure(AccountNotFoundError)
    }
}