package pt.isel.service.error

import org.springframework.http.HttpStatus
import pt.isel.entity.account.BlankEmailException
import pt.isel.entity.account.BlankUsernameException
import pt.isel.entity.account.UserEntityException

/**
 *  `UserServiceError`
 *
 * Represents errors that can occur while executing user-related service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 * */
sealed class UserServiceError : ServiceError

/** No user with the requested identifier exists. */
object UserNotFound : UserServiceError()

/** A user with the specified email address already exists. */
object EmailAlreadyExists : UserServiceError()

/** A user with the specified username already exists. */
object UsernameAlreadyExists : UserServiceError()

/** The supplied email does not satisfy the application's validation rules.**/
object InvalidEmail : UserServiceError()

/** The supplied username does not satisfy the application's validation rules. */
object InvalidUsername : UserServiceError()

/**
 * Maps a [UserServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun UserServiceError.toHttpStatus(): HttpStatus = when (this) {
    UserNotFound -> HttpStatus.NOT_FOUND
    EmailAlreadyExists -> HttpStatus.CONFLICT
    UsernameAlreadyExists -> HttpStatus.CONFLICT
    InvalidEmail -> HttpStatus.BAD_REQUEST
    InvalidUsername -> HttpStatus.BAD_REQUEST
}

/**
 * Maps a [UserEntityException] exception to the corresponding [UserServiceError] object.
 *
 * This function is intended for the service layer to translate
 * entity-layer errors into the corresponding error.
 *
 * @return the [UserServiceError] corresponding to this [UserEntityException].
 */
fun UserEntityException.toServiceError(): UserServiceError = when (this) {
    is BlankEmailException -> InvalidEmail
    is BlankUsernameException -> InvalidUsername
}