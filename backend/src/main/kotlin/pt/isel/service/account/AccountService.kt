package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.User
import pt.isel.utils.Either
import pt.isel.utils.flatMap
import pt.isel.utils.map
import pt.isel.utils.success

sealed class AccountServiceError

sealed interface SignUpResult{ val user: User }
data class CreatedNewAccount(override val user: User): SignUpResult
data class LinkedNewProvider(override val user: User): SignUpResult
data class LoggedIntoAccount(override val user: User): SignUpResult

@Service
class AccountService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) {
    fun formSignUp(
        email: String, userName: String? = null, password: String
    ): Either<AccountServiceError, SignUpResult> {
        val existingUser = userService.findByEmail(email)
            ?: return userService.create(email, userName).flatMap { user ->
                linkedAccountService.createFormAccount(user.id, password)
                    .map { CreatedNewAccount(user) }
            }

        return linkedAccountService.findByUserAndType(existingUser.id, AccountType.FORM.type)
            ?.let{ success(LoggedIntoAccount(existingUser)) }
            ?: linkedAccountService.createFormAccount(existingUser.id, password)
                    .map{ LinkedNewProvider(existingUser) }
    }

    fun oAuthSignUp(
        email: String, provider: String, providerId: String
    ): Either<AccountServiceError, SignUpResult> {
        val existingUser = userService.findByEmail(email)
            ?: return userService.create(email).flatMap{ user ->
                linkedAccountService.createOAuthAccount(user.id, provider, providerId)
                    .map{ CreatedNewAccount(user) }
            }

        return linkedAccountService.findByUserTypeAndKey(
            existingUser.id, provider, providerId
        )   ?.let { success(LoggedIntoAccount(existingUser)) }
            ?: linkedAccountService.createOAuthAccount(existingUser.id, provider, providerId)
                .map{ LinkedNewProvider(existingUser) }
    }

    fun oAuthAccountLink(
        userId: Int, provider: String, providerId: String
    ): Either<AccountServiceError, LinkedNewProvider> {
        val userEither = userService.read(userId)
        return userEither.flatMap{ user ->
            linkedAccountService.createOAuthAccount(user.id, provider, providerId)
                .map{ LinkedNewProvider(user) }
        }
    }
}
