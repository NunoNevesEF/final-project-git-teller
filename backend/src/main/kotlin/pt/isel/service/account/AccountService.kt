package pt.isel.service.account

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.stereotype.Service
import pt.isel.domain.account.User
import pt.isel.service.Either
import pt.isel.service.flatMap
import pt.isel.service.map
import pt.isel.service.success

sealed class AccountServiceError

@Service
class AccountService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) {
    fun formSignUp(email: String, userName: String, password: String): Either<AccountServiceError, User> {
        val userEither =
            userService.findByEmail(email)?.let { readUser -> success(readUser) } ?: userService.create(email, userName)
        return userEither.flatMap { user ->
            linkedAccountService.createFormAccount(user.id, password).map { user }
        }
    }

    fun oAuthSignUp(email: String, userName: String, provider: String): Either<AccountServiceError, User> {
        val userEither =
            userService.findByEmail(email)?.let { readUser -> success(readUser) } ?: userService.create(email, userName)
        return userEither.flatMap { user ->
            linkedAccountService.createOAuthAccount(user.id, provider).map { user }
        }
    }
}
