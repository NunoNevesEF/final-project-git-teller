package pt.isel.service.account

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import pt.isel.domain.account.TokenPair

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {

    fun login(email: String, password: String): TokenPair {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(email, password)
        )

        return jwtService.generateTokenPair(authentication)
    }
}