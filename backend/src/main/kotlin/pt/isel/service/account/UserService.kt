package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.entity.account.User
import pt.isel.entity.account.UserEntityException
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.service.error.EmailAlreadyExists
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.UserServiceError
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.service.error.toServiceError
import pt.isel.service.error.InvalidEmail
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.onFailure
import pt.isel.utils.success
import pt.isel.utils.toEither

/**
 *  `UserService`
 *
 * The service layer user-related operations
 *
 * @property [userRepo] the user repository used to communicate with the persistence layer.
 * */
@Service
class UserService(private val userRepo: IUserRepository) {
    /** @param [email] The [email] that is used to identify the [User].
     * @param [username] the [username] used by the application to refer to the [User]. Nullable to allow 2-step creation process in the oauth flow. (Create User -> Choose username)
     * @return
     * - The created [User] in case of success.
     * - [EmailAlreadyExists] error in case there already exists a user with that email.
     * - [UsernameAlreadyExists] error in case there already exists a user with that username. Only applies to not null usernames.
     * - [InvalidEmail] error if provided email does not satisfy data object restrictions
     * - [InvalidUsername] error if provided username does not satisfy data object restrictions
     * **/
    fun create(email: String, username: String? = null): Either<UserServiceError, User> {
        validateEmailIsUnique(email).onFailure { return failure(it) }
        if(username != null) validateUsernameIsUnique(username).onFailure { return failure(it) }

        try{
            val user = User(email = email, username = username)
            return success(userRepo.create(user))
        } catch(e: UserEntityException) {
            return failure(e.toServiceError())
        }
    }
    /** @param [id] The [id] that is used to identify the [User] in the persistence.
     * @return
     * - The corresponding [User] in case of success.
     * - [UserNotFound] error in case that [User] does not exist.
     * **/
    fun findById(id: Int): Either<UserNotFound, User> = userRepo.findById(id).toEither{ UserNotFound }
    /** @param [email] The [email] that corresponds to the [User].
     * @return
     * - The corresponding [User] in case of success.
     * - [UserNotFound] error in case that [User] does not exist.
     * **/
    fun findByEmail(email: String): Either<UserNotFound, User> = userRepo.findByEmail(email).toEither { UserNotFound }
    /** @param [id] The [id] that is used to identify the [User] in the persistence.
     * @return
     * - The [User] with the updated [newUsername] in case of success.
     * - [InvalidUsername] in case [newUsername] has an invalid username format or text.
     * - [UsernameAlreadyExists] in case [newUsername] already belongs to another [User].
     * - [UserNotFound] error in case the [User] attempting to be updated does not exist.
     * **/
    fun updateUsername(id: Int, newUsername: String): Either<UserServiceError, User> {
        validateUsernameIsUnique(newUsername, id).onFailure { return failure(it) }
        val oldUser = userRepo.findById(id) ?: return failure(UserNotFound)
        try{
            val updatedUser = oldUser.copy(username = newUsername)
            return success(userRepo.update(updatedUser)!!)
        } catch(e: UserEntityException) {
            return failure(e.toServiceError())
        }
    }
    /** @param [id] The [id] that is used to identify the [User] in the persistence.
     * @return
     * - The deleted [User] in case of success.
     * - [UserNotFound] error in case the [User] attempting to be deleted does not exist.
     * **/
    fun delete(id: Int): Either<UserNotFound, User> = userRepo.delete(id).toEither { UserNotFound }
    /** @param [email] The [email] attempting to be persisted.
     * @param [id] The [id] that is used to identify the [User] in the persistence.
     * @return [EmailAlreadyExists] error in case the [email] already exists in database and does not belong to [User]
     * **/
    private fun validateEmailIsUnique(email: String, id: Int? = null): Either<EmailAlreadyExists, Unit>{
        userRepo.findByEmail(email)?.let {
            if (it.id != id) return failure(EmailAlreadyExists)
        }
        return success(Unit)
    }
    /** @param [username] The [username] attempting to be persisted.
     * @param [id] The [id] that is used to identify the [User] in the persistence.
     * @return [EmailAlreadyExists] error in case the [username] already exists in database and does not belong to [User]
     * **/
    private fun validateUsernameIsUnique(username: String, id: Int? = null): Either<UsernameAlreadyExists, Unit>{
        userRepo.findByUsername(username)?.let {
            if (it.id != id) return failure(UsernameAlreadyExists)
        }
        return success(Unit)
    }
}




