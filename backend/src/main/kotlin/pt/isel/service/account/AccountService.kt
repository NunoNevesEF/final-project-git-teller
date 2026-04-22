package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.User
import pt.isel.utils.Either
import pt.isel.utils.flatMap
import pt.isel.utils.map
import pt.isel.utils.success

sealed class AccountServiceError

@Service
class AccountService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) {
    fun formSignUp(email: String, userName: String, password: String): Either<AccountServiceError, User> {
        val userEither =
            userService.findByEmail(email)?.let { readUser -> success(readUser) }
                ?: userService.create(email, userName)
        return userEither.flatMap { user ->
            linkedAccountService.findByUserAndType(user.id, AccountType.FORM) ?:
                linkedAccountService.createFormAccount(user.id, password).map { user }
            success(user)
        }
    }

    fun oAuthSignUp(email: String, userName: String, provider: String, providerId: String): Either<AccountServiceError, User> {
        val userEither =
            userService.findByEmail(email)?.let { readUser -> success(readUser) }
                ?: userService.create(email, userName)
        return userEither.flatMap { user ->
            linkedAccountService.findByUserTypeAndKey(
                user.id, AccountType.fromString(provider), providerId
            ) ?: linkedAccountService.createOAuthAccount(user.id, provider, providerId).map { user }
            success(user)
        }
    }
}
