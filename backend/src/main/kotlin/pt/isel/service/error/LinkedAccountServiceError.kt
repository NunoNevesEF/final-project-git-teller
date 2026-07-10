package pt.isel.service.error

import org.springframework.http.HttpStatus
import pt.isel.domain.account.InvalidOAuthProviderException
import pt.isel.domain.account.OAuthAccountProviderException
import pt.isel.entity.account.BlankProviderIdException
import pt.isel.entity.account.LinkedAccountEntityException

/**
 *  `LinkedAccountServiceError`
 *
 * Represents errors that can occur while executing linked-account-related service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 * */
sealed class LinkedAccountServiceError : ServiceError

/** Number of linked accounts of given type for this user is maxed **/
object LinkedAccountTypeMaxed : LinkedAccountServiceError()

/** No linked account for the request identifier **/
object LinkedAccountNotFound : LinkedAccountServiceError()

/** Provided details point to a linked account of the wrong type **/
object UnexpectedProvider: LinkedAccountServiceError()

/** The supplied providerId does not satisfy the application's validation rules.**/
object InvalidProviderID : LinkedAccountServiceError()

/** The supplied password does not satisfy the application's validation rules.**/
object InvalidPassword : LinkedAccountServiceError()

/** The supplied provider does not satisfy the application's validation rules.**/
object InvalidProvider : LinkedAccountServiceError()

/**
 * Maps a [LinkedAccountServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun LinkedAccountServiceError.toHttpStatus(): HttpStatus = when (this) {
    LinkedAccountNotFound -> HttpStatus.NOT_FOUND
    LinkedAccountTypeMaxed -> HttpStatus.CONFLICT
    InvalidProviderID -> HttpStatus.BAD_REQUEST
    InvalidPassword -> HttpStatus.BAD_REQUEST
    InvalidProvider -> HttpStatus.BAD_REQUEST
    UnexpectedProvider -> HttpStatus.BAD_REQUEST
}

/**
 * Maps a [LinkedAccountEntityException] exception to the corresponding [LinkedAccountServiceError] object.
 *
 * This function is intended for the service layer to translate
 * entity-layer errors into the corresponding error.
 *
 * @return the [LinkedAccountServiceError] corresponding to this [LinkedAccountEntityException].
 */
fun LinkedAccountEntityException.toServiceError(): LinkedAccountServiceError = when (this) {
    is BlankProviderIdException -> InvalidProviderID
}

/**
 * Maps a [OAuthAccountProviderException] exception to the corresponding [LinkedAccountServiceError] object.
 *
 * This function is intended for the service layer to translate
 * entity-layer errors into the corresponding error.
 *
 * @return the [LinkedAccountServiceError] corresponding to this [OAuthAccountProviderException].
 */
fun OAuthAccountProviderException.toServiceError(): LinkedAccountServiceError = when (this) {
    is InvalidOAuthProviderException -> InvalidProvider
}