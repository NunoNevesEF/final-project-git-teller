package pt.isel.service.account.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import pt.isel.domain.account.TokenPair
import java.time.Instant
import java.util.stream.Collectors

@EnableConfigurationProperties(JwtProperties::class)
@Service
class JwtService(
    private val jwtProperties: JwtProperties,
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {
    private val appName = "gitTeller"

    fun generateTokenPair(authentication: Authentication): TokenPair {
        val accessToken = generateAccessToken(authentication)
        val refreshToken = generateRefreshToken(authentication)
        return TokenPair(accessToken, refreshToken)
    }

    fun generateAccessToken(authentication: Authentication): String{
        val scope = authentication
            .authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "))

        val claims = getClaimsBuilder(authentication, jwtProperties.expiration, "access")
            .claim("scope", scope)
            .build()

        return generateToken(claims)
    }

    fun generateRefreshToken(authentication: Authentication): String{
        val claims = getClaimsBuilder(authentication, jwtProperties.refreshExpiration, "refresh")
            .build()

        return generateToken(claims)
    }

    //Validate Token
    fun isValidToken(token: String): Boolean{
        try{
            jwtDecoder.decode(token)
            return true
        } catch (e: Exception){
            return false
        }
    }

    fun getUsername(token: String): String =
        jwtDecoder.decode(token).subject

    private fun getClaimsBuilder(
        authentication: Authentication, expirationTimeMs: Long, type: String
    ): JwtClaimsSet.Builder{
        val now = Instant.now()
        val userPrincipal = authentication.principal as UserDetails

        return JwtClaimsSet.builder()
            .issuer(appName)
            .issuedAt(now)
            .expiresAt(now.plusMillis(expirationTimeMs))
            .subject(userPrincipal.username)
            .claim("type", type)
    }

    private fun generateToken(claims: JwtClaimsSet): String =
        jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
}

@ConfigurationProperties("app.jwt")
data class JwtProperties(
    val expiration: Long,
    val refreshExpiration: Long,
)

