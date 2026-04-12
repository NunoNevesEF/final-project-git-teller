package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.User
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.service.Either
import pt.isel.service.failure
import pt.isel.service.success
import pt.isel.service.toEither


sealed class UserServiceError : AccountServiceError()
object UserNotFound : UserServiceError()
object EmailAlreadyExists : UserServiceError()
object AuthenticationFailure : UserServiceError()

/**TODO: Add function to support multiple login types:
 * 1. If User tries to signup and e-mail does not exist then send confirmation email and when confirmed then create account.
 * 2. If User tries to signup and email does exist:
 * 2.A  If DB User Authentication method includes Form:
 * 2.A.1    If password is the same then login
 * 2.A.2    If password is not the same then return authentication failure
 * 2.B  If DB User Authentication does not include Form then send confirmation email and when confirmed add Form login to User.
 * **/
@Service
class UserService(private val userRepo: UserRepoMem) {
    fun create(email: String, userName: String): Either<EmailAlreadyExists, User> {
        if (userRepo.read(email) != null) return failure(EmailAlreadyExists)
        val user =  User.create(email = email, userName = userName)
        return success(userRepo.create(user))
    }

    fun read(id: Int): Either<UserNotFound, User> = userRepo.read(id).toEither { UserNotFound }

    fun read(email: String): Either<UserNotFound, User> = findByEmail(email).toEither { UserNotFound }

    fun update(id: Int, userName: String): Either<UserNotFound, User> {
        val oldUser = userRepo.read(id) ?: return failure(UserNotFound)
        val updatedUser = oldUser.copy(userName = userName)
        return success(userRepo.update(updatedUser)!!)
    }

    fun delete(id: Int): Either<UserNotFound, User> = userRepo.delete(id).toEither { UserNotFound }

    fun findByEmail(email: String): User? = userRepo.read(email)
}




