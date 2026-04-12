package pt.isel.service.account.auth

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import pt.isel.domain.account.TokenPair
import pt.isel.service.Either
import pt.isel.service.failure
import pt.isel.service.success

sealed class AuthServiceError
object InvalidTokenError : AuthServiceError()

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userPrincipalService: UserPrincipalService,
) {

    fun login(email: String, password: String): TokenPair {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(email, password)
        )

        return jwtService.generateTokenPair(authentication)
    }

    fun refreshToken(refreshToken: String): Either<InvalidTokenError, TokenPair> {
        if(!jwtService.isValidToken(refreshToken)){
            return failure(InvalidTokenError)
        }

        val userName = jwtService.getUsername(refreshToken)

        val userDetails = userPrincipalService.loadUserByUsername(userName)

        val authToken = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        val accessToken = jwtService.generateAccessToken(authToken)
        return success(TokenPair(accessToken, refreshToken))
    }
}