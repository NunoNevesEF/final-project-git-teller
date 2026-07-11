package pt.isel.service.auth

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import pt.isel.service.auth.model.TokenPair
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.infraestructure.security.principal.UserPrincipalService
import pt.isel.service.error.InvalidCredentialsError
import pt.isel.service.error.InvalidTokenError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * `AuthService`
 *
 * The service layer responsible for authentication operations such as
 * user login and refresh token exchange.
 *
 * @property [authenticationManager] Authenticates user credentials.
 * @property [jwtService] Generates and validates JWT access and refresh tokens.
 * @property [userPrincipalService] Loads authenticated user details.
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userPrincipalService: UserPrincipalService,
) {

    /**
     * Authenticates a user using their email and password.
     *
     * @param [email] The email address that identifies the user.
     * @param [password] The user's plaintext password.
     *
     * @return
     * - A newly generated [TokenPair] containing an access token
     * and a refresh token on success.
     * - [InvalidCredentialsError] if the given credentials are invalid (do not match any registered user).
     */
    fun login(email: String, password: String): Either<InvalidCredentialsError, TokenPair> {
        try{
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
            return success(jwtService.generateTokenPair(authentication))
        } catch(_: AuthenticationException) {
            return failure(InvalidCredentialsError)
        }
    }

    /**
     * Generates a new access token from a valid refresh token.
     *
     * @param [refreshToken] The refresh token previously issued to the user.
     *
     * @return
     * - A new [TokenPair] containing a freshly generated access token and the
     *   original refresh token in case of success.
     * - [InvalidTokenError] if the refresh token is invalid, expired, malformed or the associated user no longer exists.
     * @throws [JwtException] if token fails to decode.
     */
    fun refreshToken(refreshToken: String): Either<InvalidTokenError, TokenPair> {
        try{
            if(!jwtService.isValidToken(refreshToken)){
                return failure(InvalidTokenError)
            }

            val username = jwtService.getUsername(refreshToken)

            val userDetails = userPrincipalService.loadUserByUsername(username)

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )

            val accessToken = jwtService.generateAccessToken(authToken)
            return success(TokenPair(accessToken, refreshToken))
        } catch(_: UsernameNotFoundException) {
            return failure(InvalidTokenError)
        }
    }
}