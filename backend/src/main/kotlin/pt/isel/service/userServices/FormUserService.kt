package pt.isel.service.userServices

import org.springframework.stereotype.Service
import pt.isel.domain.User
import pt.isel.domain.UserAuthentication
import pt.isel.domain.UserData
import pt.isel.model.UserDTO
import pt.isel.repository.memory.UserRepoMem
import pt.isel.service.Either
import pt.isel.service.failure
import pt.isel.service.success
import pt.isel.service.toEither


sealed class BaseUserServiceError
object UserNotFound : BaseUserServiceError()
object EmailAlreadyExists : BaseUserServiceError()
object AuthenticationFailure : BaseUserServiceError()

/**TODO: Add function to support multiple login types:
 * 1. If User tries to signup and e-mail does not exist then send confirmation email and when confirmed then create account.
 * 2. If User tries to signup and email does exist:
 * 2.A  If DB User Authentication method includes Form:
 * 2.A.1    If password is the same then login
 * 2.A.2    If password is not the same then return authentication failure
 * 2.B  If DB User Authentication does not include Form then send confirmation email and when confirmed add Form login to User.
 * **/
@Service
class FormUserService(private val userRepo: UserRepoMem) {
    fun create(email: String, userName: String, password: String): Either<EmailAlreadyExists, UserDTO> {
        if (userRepo.read(email) != null) return failure(EmailAlreadyExists)
        val user = User(
            UserData.create(email = email, userName = userName),
            listOf(UserAuthentication.FormAuthentication(password))
        )
        return success(UserDTO.create(userRepo.create(user)))
    }

    fun read(id: Int): Either<UserNotFound, UserDTO> =
        userRepo.read(id)?.let{ user -> UserDTO.create(user) }.toEither { UserNotFound }

    fun read(email: String): Either<UserNotFound, UserDTO> =
        userRepo.read(email)?.let{ user -> UserDTO.create(user) }.toEither { UserNotFound }

    fun update(id: Int, userName: String): Either<UserNotFound, UserDTO> {
        val oldUser = userRepo.read(id) ?: return failure(UserNotFound)
        val updatedUser = oldUser.copy(data = oldUser.data.copy(userName = userName))
        return success(UserDTO.create(userRepo.update(updatedUser)!!))
    }

    fun delete(id: Int): Either<UserNotFound, UserDTO> =
        userRepo.delete(id)?.let{ user -> UserDTO.create(user) }.toEither { UserNotFound }
}




