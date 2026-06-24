package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.entity.User
import pt.isel.service.ServiceError
import pt.isel.utils.Either
import pt.isel.utils.Success
import pt.isel.utils.flatMap
import pt.isel.utils.isSuccess
import pt.isel.utils.map
import pt.isel.utils.rightOrNull
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

        val existing = userService.findByEmail(email)

        return if (existing.isSuccess()) {

            val user = existing.rightOrNull()!!

            val account = linkedAccountService.findByUserAndType(user.id, AccountType.FORM.type)

            if (account != null) {
                success(LoggedIntoAccount(user))
            } else {
                linkedAccountService.createFormAccount(user.id, password).map { LinkedNewProvider(user) }
            }
        } else {
            userService.create(email, userName).flatMap { createdUser ->
                    linkedAccountService.createFormAccount(createdUser.id, password)
                        .map { CreatedNewAccount(createdUser) }
                }
        }
    }

    fun oAuthSignUp(
        email: String, provider: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val existing = userService.findByEmail(email)
        return if (existing.isSuccess()) {

            val user = existing.rightOrNull()!!
            val account = linkedAccountService.findByUserTypeAndKey(user.id, provider, providerId)

            if (account != null) {
                success(LoggedIntoAccount(user))
            } else {
                linkedAccountService.createOAuthAccount(user.id, provider, providerId).map { LinkedNewProvider(user) }
            }

        } else {
            userService.create(email).flatMap { createdUser ->
                    linkedAccountService.createOAuthAccount(createdUser.id, provider, providerId)
                        .map { CreatedNewAccount(createdUser) }
                }
        }
    }

    fun oAuthAccountLink(
        userId: Int, provider: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val userEither = userService.findById(userId)

        require(userEither is Success)

        val account = linkedAccountService.findByUserTypeAndKey(userId, provider, providerId)

        if(account != null) { return success(LoggedIntoAccount(userEither.right)) }

        return userEither.flatMap { user ->
            linkedAccountService.createOAuthAccount(user.id, provider, providerId).map { LinkedNewProvider(user) }
        }
    }
}
