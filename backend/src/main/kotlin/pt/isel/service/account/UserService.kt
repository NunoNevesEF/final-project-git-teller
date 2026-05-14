package pt.isel.service.account

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import pt.isel.entity.User
import pt.isel.repository.IUserRepository
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.toEither


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
class UserService(private val userRepo: IUserRepository) {
    fun create(email: String, userName: String? = null): Either<UserServiceError, User> {
        if (userRepo.findByEmail(email) != null) return failure(EmailAlreadyExists)
        return success(userRepo.create(User(email = email, userName = userName)))
    }

    fun findById(id: Int): Either<UserNotFound, User> = userRepo.findById(id).toEither{ UserNotFound }

    fun findByEmail(email: String): Either<UserNotFound, User> = userRepo.findByEmail(email).toEither { UserNotFound }

    fun update(id: Int, newUsername: String): Either<UserNotFound, User> {
        val oldUser = userRepo.findById(id) ?: return failure(UserNotFound)
        val updatedUser = oldUser.copy(userName = newUsername)
        return success(userRepo.update(updatedUser)!!)
    }

    fun delete(id: Int): Either<UserNotFound, User> = userRepo.delete(id).toEither { UserNotFound }
}




