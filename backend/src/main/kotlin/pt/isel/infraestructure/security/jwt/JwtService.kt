package pt.isel.infraestructure.security.jwt

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
import pt.isel.service.auth.model.TokenPair
import java.time.Instant
import java.util.stream.Collectors
import org.springframework.security.oauth2.jwt.JwtException

/**
 * `JwtService`
 *
 * Responsible for generating, validating and parsing JSON Web Tokens (JWTs)
 * used by the authentication system.
 *
 * This service creates access tokens, refresh tokens and temporary OAuth
 * linking state tokens, and provides methods for validating and extracting
 * information from them.
 *
 * @property [jwtProperties] JWT expiration configuration.
 * @property [jwtEncoder] Encodes signed JWTs.
 * @property [jwtDecoder] Decodes and validates JWTs.
 */
@EnableConfigurationProperties(JwtProperties::class)
@Service
class JwtService(
    private val jwtProperties: JwtProperties,
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {
    private val appName = "gitTeller"

    /**
     * Generates both an access token and a refresh token for an authenticated user.
     *
     * @param [authentication] The authenticated user.
     *
     * @return A [TokenPair] containing newly generated access and refresh tokens.
     */
    fun generateTokenPair(authentication: Authentication): TokenPair {
        val accessToken = generateAccessToken(authentication)
        val refreshToken = generateRefreshToken(authentication)
        return TokenPair(accessToken, refreshToken)
    }

    /**
     * Generates a signed access token for an authenticated user.
     *
     * The generated token contains the user's username as its subject,
     * the granted authorities under the `scope` claim, and a `type`
     * claim with the value `"access"`.
     *
     * @param [authentication] The authenticated user.
     *
     * @return The signed JWT access token.
     */
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

    /**
     * Generates a signed refresh token for an authenticated user.
     *
     * The generated token contains the user's username as its subject
     * and a `type` claim with the value `"refresh"`.
     *
     * @param [authentication] The authenticated user.
     *
     * @return The signed JWT refresh token.
     */
    fun generateRefreshToken(authentication: Authentication): String{
        val claims = getClaimsBuilder(authentication, jwtProperties.refreshExpiration, "refresh")
            .build()

        return generateToken(claims)
    }

    /**
     * Determines whether a JWT is valid.
     *
     * A token is considered valid if it can be successfully decoded,
     * its signature is correct, and it has not expired.
     *
     * @param [token] The JWT to validate.
     *
     * @return `true` if the token is valid; otherwise `false`.
     */
    fun isValidToken(token: String): Boolean{
        try{
            jwtDecoder.decode(token)
            return true
        } catch (_: Exception){
            return false
        }
    }

    /**
     * Extracts the username from a JWT.
     *
     * @param [token] A valid JWT.
     *
     * @return The username stored in the token subject.
     *
     * @throws JwtException if the token cannot be decoded.
     */
    fun getUsername(token: String): String =
        jwtDecoder.decode(token).subject

    /**
     * Builds the common claims for both tokens
     *
     * @param [authentication] The authenticated user.
     * @param [expirationTimeMs] How long the token lasts for in milliseconds
     * @param [type] Name of the type of token (access or refresh).
     *
     * @return The username stored in the token subject.
     *
     * @throws JwtException if the token cannot be decoded.
     */
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

    /**
     * Generates a temporary JWT used as OAuth account-linking state.
     *
     * The generated token is valid for five minutes and contains the
     * target user identifier, OAuth provider and a flag indicating
     * that it represents a linking operation.
     *
     * @param [userId] The user requesting account linking.
     * @param [provider] The OAuth provider being linked.
     *
     * @return The signed state token.
     */
    fun generateLinkState(userId: Int, provider: String): String{
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .issuer(appName)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .subject("oauth-link")
            .claim("userId", userId)
            .claim("provider", provider)
            .claim("link", true)
            .build()

        return generateToken(claims)
    }

    /**
     * Parses an OAuth linking state token.
     *
     * @param [token] A valid OAuth linking state token.
     *
     * @return The decoded [OAuthLinkState].
     *
     * @throws JwtException if the token cannot be decoded.
     */
    fun parseLinkState(token: String): OAuthLinkState {
        val jwt = jwtDecoder.decode(token)

        return OAuthLinkState(
            link = jwt.getClaim("link"),
            userId = (jwt.getClaim("userId")),
            provider = jwt.getClaim("provider"),
        )
    }

    /**
     * Determines whether an OAuth linking state token is valid for
     * the specified provider.
     *
     * A token is considered valid if it can be decoded, has not
     * expired, represents a linking operation and matches the
     * supplied OAuth provider.
     *
     * @param [token] The state token.
     * @param [provider] The expected OAuth provider.
     *
     * @return `true` if the state token is valid for the provider;
     * otherwise `false`.
     */
    fun isValidLinkState(token: String, provider: String): Boolean{
        return try{
            val jwt = jwtDecoder.decode(token)

            jwt.getClaim<Boolean>("link") == true &&
                    jwt.getClaim<String>("provider") == provider
        } catch(_: Exception){
            false
        }
    }

    /**
     * Represents the information encoded in an OAuth linking state token.
     *
     * @property [link] Whether the token represents an OAuth linking operation.
     * @property [userId] The identifier of the user requesting the link.
     * @property [provider] The OAuth provider associated with the linking operation.
     */
    data class OAuthLinkState(
        val link: Boolean = false,
        val userId: Int,
        val provider: String
    )
}

/**
 * Injects access token expiration duration and refresh token expiration duration as configured in application.properties.
 *
 * @property [expiration] Access Token expiration duration.
 * @property [refreshExpiration] Refresh token expiration duration.
 */
@ConfigurationProperties("app.jwt")
data class JwtProperties(
    val expiration: Long,
    val refreshExpiration: Long,
)

