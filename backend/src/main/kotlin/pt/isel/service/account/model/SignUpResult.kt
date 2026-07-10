package pt.isel.service.account.model

import org.springframework.http.HttpStatus
import pt.isel.entity.account.User

/**
 *  `SignUpResult`
 *
 * Represents a possible result on a successful sign-up.
 *
 * @property [user] The user who was acted upon.
 * */
sealed interface SignUpResult { val user: User }

/**Sign up resulted in the creation of a new user and link of a new account**/
data class CreatedNewUserAccount(override val user: User) : SignUpResult

/**Sign up resulted in the link of a new account to an existing user**/
data class LinkedNewAccount(override val user: User) : SignUpResult

/**Sign up resulted in no account data creation**/
data class LoggedIntoUserAccount(override val user: User) : SignUpResult

/**
 * Maps a [SignUpResult] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer success result into HTTP responses.
 *
 * @return the HTTP status code corresponding to the successful outcome.
 */
fun SignUpResult.toHttpStatus() = when (this) {
    is CreatedNewUserAccount -> HttpStatus.CREATED
    is LinkedNewAccount -> HttpStatus.CREATED
    is LoggedIntoUserAccount -> HttpStatus.OK
}