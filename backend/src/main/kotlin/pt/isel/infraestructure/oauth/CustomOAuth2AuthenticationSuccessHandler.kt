package pt.isel.infraestructure.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.utils.Failure
import pt.isel.utils.Success

//TODO: DOCUMENT

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val jwtService: JwtService,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val linkedAccountService: LinkedAccountService,
    private val userService: UserService,
    @Value("\${app.frontend.redirect-url.web}") private val webRedirectUrl: String,
    @Value("\${app.frontend.redirect-url.mobile}") private val mobileRedirectUrl: String
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth = authentication as OAuth2AuthenticationToken
        val principal = oauth.principal as UserPrincipal

        val registrationId = oauth.authorizedClientRegistrationId
        val principalName = oauth.name

        val client = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
            registrationId, principalName
        )

        linkedAccountService.updateOAuthAccount(
            userId = principal.getUserId(),
            providerName = registrationId, //provider
            providerId = principal.getOAuth2Id(), //providerId
            newAccessToken = client.accessToken.tokenValue,
            newRefreshToken = client.refreshToken?.tokenValue
        )

        val tokenPair = jwtService.generateTokenPair(authentication)

        val user = when (val result = userService.findById(principal.getUserId())) {
            is Success -> result.right
            is Failure -> null
        }
        val needsUsername = user?.username.isNullOrBlank()

        val userAgent = request.getHeader("User-Agent") ?: ""
        val isMobile = userAgent.contains("Android", ignoreCase = true) ||
                userAgent.contains("iPhone", ignoreCase = true) ||
                userAgent.contains("iPad", ignoreCase = true)
        val targetUrl = if (isMobile) mobileRedirectUrl else webRedirectUrl

        val redirectUrl = UriComponentsBuilder
            .fromUriString(targetUrl)
            .queryParam("accessToken", tokenPair.accessToken)
            .queryParam("refreshToken", tokenPair.refreshToken)
            .queryParam("needsUsername", needsUsername)
            .queryParam("username", user?.username ?: "")
            .build()
            .toUriString()

        response.sendRedirect(redirectUrl)
    }
}