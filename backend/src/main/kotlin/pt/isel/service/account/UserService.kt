package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.entity.User
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.service.ServiceError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.toEither


sealed class UserServiceError : ServiceError
object UserNotFound : UserServiceError()
object EmailAlreadyExists : UserServiceError()

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




