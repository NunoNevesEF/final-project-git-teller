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
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.auth.JwtService
import tools.jackson.databind.ObjectMapper

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val jwtService: JwtService,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val linkedAccountService: LinkedAccountService
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
            accessToken = client.accessToken,
            refreshToken = client.refreshToken
        )

        val tokenPair = jwtService.generateTokenPair(authentication)

        val frontendRedirectBase = "http://localhost:8081/login" // ou /home
        val redirectUrl = UriComponentsBuilder
            .fromUriString(frontendRedirectBase)
            .queryParam("accessToken", tokenPair.accessToken)
            .queryParam("refreshToken", tokenPair.refreshToken)
            .build()
            .toUriString()

        response.sendRedirect(redirectUrl)
    }
}