package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.service.auth.AuthService
import pt.isel.service.error.toHttpStatus
import pt.isel.service.auth.model.TokenPair
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.utils.rightOrNull

/**
 *  `AuthController`
 *
 * Exposes authentication related presentation-layer endpoints of the application.
 * */
@CrossOrigin(origins = ["https://frontend-production-fc0c.up.railway.app"])
@RestController
@RequestMapping("/api/public/auth")
class AuthController(private val authService: AuthService) {
    /**
     * Logs into a [pt.isel.entity.account.User] through email and password registration. Requires [pt.isel.entity.account.User] to have a associated [pt.isel.entity.account.FormLinkedAccount].
     * For OAuth provider login, it is done through the configured flow in framework/oauth
     *
     * Returns:
     * - `200 ok` with the corresponding [pt.isel.service.auth.model.TokenPair] if login is successful.
     * - `401 Unauthorized` if the given credentials are invalid (do not match any registered user).
     *
     * @param email The email address used to identify the [pt.isel.entity.account.User].
     * @param password The password provided during registration.
     *
     * @return A [org.springframework.http.ResponseEntity] containing the created [pt.isel.service.auth.model.TokenPair] on success, or an empty response with the appropriate HTTP status on failure.
     */
    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<TokenPair> {
        return when(val result = authService.login(email, password)){
            is Success -> ResponseEntity.ok(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
    /**
     * Refreshes a [pt.isel.entity.account.User]'s access token.
     *
     * Returns:
     * - `200 ok` with the corresponding [TokenPair] (new access + old refresh) if login is successful.
     * - `401 Unauthorized` if the refresh token is invalid, expired, malformed or the associated user no longer exists.
     *
     * @param refreshToken The [pt.isel.entity.account.User]'s refresh token.
     *
     * @return A [ResponseEntity] containing the created [TokenPair] on success, or an empty response with the appropriate HTTP status on failure.
     *
     * @throws [org.springframework.security.oauth2.jwt.JwtException] if token fails to decode.
     */
    @PostMapping("/refresh-token")
    fun refresh(
        @RequestParam refreshToken: String
    ): ResponseEntity<TokenPair> {
        return when(val token = authService.refreshToken(refreshToken)) {
            is Success -> ResponseEntity.ok(token.rightOrNull())
            is Failure -> ResponseEntity.status(token.left.toHttpStatus()).build()
        }
    }
}