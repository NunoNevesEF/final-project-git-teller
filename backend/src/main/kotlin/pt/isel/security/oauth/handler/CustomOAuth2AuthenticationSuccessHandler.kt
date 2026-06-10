package pt.isel.security.oauth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.domain.account.AccountType
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.auth.JwtService
import tools.jackson.databind.ObjectMapper

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val jwtService: JwtService,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val linkedAccountService: LinkedAccountService,
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

        linkedAccountService.update(
            userId = principal.getUserId(),
            type = registrationId, //provider
            key = principal.getOAuth2Id(), //providerId
            accessToken = client.accessToken,
            refreshToken = client.refreshToken
        )

        val tokenPair = jwtService.generateTokenPair(authentication)

        val userAgent = request.getHeader("User-Agent") ?: ""
        val isMobile = userAgent.contains("Android", ignoreCase = true) ||
                userAgent.contains("iPhone", ignoreCase = true) ||
                userAgent.contains("iPad", ignoreCase = true)
        val targetUrl = if (isMobile) mobileRedirectUrl else webRedirectUrl

        val redirectUrl = UriComponentsBuilder
            .fromUriString(targetUrl)
            .queryParam("accessToken", tokenPair.accessToken)
            .queryParam("refreshToken", tokenPair.refreshToken)
            .build()
            .toUriString()

        response.sendRedirect(redirectUrl)
    }
}