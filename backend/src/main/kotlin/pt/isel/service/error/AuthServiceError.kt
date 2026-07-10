package pt.isel.service.error

import org.springframework.http.HttpStatus

/**
 * `AuthServiceError`
 *
 * Represents errors that can occur while executing authentication-related
 * service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 */
sealed class AuthServiceError: ServiceError

/** The supplied refresh token is invalid, expired, or malformed. */
object InvalidTokenError : AuthServiceError()

/** The supplied email + password is invalid**/
object InvalidCredentialsError : AuthServiceError()

/**
 * Maps an [AuthServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun AuthServiceError.toHttpStatus(): HttpStatus = when (this) {
    InvalidTokenError -> HttpStatus.UNAUTHORIZED
    InvalidCredentialsError -> HttpStatus.UNAUTHORIZED
}