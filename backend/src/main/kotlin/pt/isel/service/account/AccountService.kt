package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.entity.User
import pt.isel.service.ServiceError
import pt.isel.utils.Either
import pt.isel.utils.Success
import pt.isel.utils.flatMap
import pt.isel.utils.map
import pt.isel.utils.success

sealed interface SignUpResult {
    val user: User
}

data class CreatedNewAccount(override val user: User) : SignUpResult
data class LinkedNewProvider(override val user: User) : SignUpResult
data class LoggedIntoAccount(override val user: User) : SignUpResult

@Service
class AccountService(
    private val userService: UserService, private val linkedAccountService: LinkedAccountService
) {
    fun formSignUp(
        email: String, userName: String?, password: String
    ): Either<ServiceError, SignUpResult> {

        val userResult = userService.findByEmail(email)

        return if (userResult is Success) {
            val user = userResult.right
            val account = linkedAccountService.findUserFormAccount(user.id)

            if (account is Success) { success(LoggedIntoAccount(user)) }
            else { linkedAccountService.createFormAccount(user, password).map { LinkedNewProvider(user) } }
        } else {
            userService.create(email, userName).flatMap { createdUser ->
                    linkedAccountService.createFormAccount(createdUser, password)
                        .map { CreatedNewAccount(createdUser) }
                }
        }
    }

    fun oAuthSignUp(
        email: String, provider: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val userResult = userService.findByEmail(email)
        return if (userResult is Success) {

            val user = userResult.right

            val accountResult = linkedAccountService.findUserOAuthAccount(
                user.id, OAuthAccountProvider.fromString(provider), providerId
            )

            if (accountResult is Success) {
                success(LoggedIntoAccount(user))
            } else {
                linkedAccountService.createOAuthAccount(user, provider, providerId).map { LinkedNewProvider(user) }
            }

        } else {
            userService.create(email).flatMap { createdUser ->
                    linkedAccountService.createOAuthAccount(createdUser, provider, providerId)
                        .map { CreatedNewAccount(createdUser) }
                }
        }
    }

    fun oAuthAccountLink(
        userId: Int, provider: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val userEither = userService.findById(userId)

        require(userEither is Success)

        val accountResult = linkedAccountService.findUserOAuthAccount(
            userId, OAuthAccountProvider.fromString(provider), providerId
        )

        if(accountResult is Success) { return success(LoggedIntoAccount(userEither.right)) }

        return userEither.flatMap { user ->
            linkedAccountService.createOAuthAccount(user, provider, providerId).map { LinkedNewProvider(user) }
        }
    }
}
