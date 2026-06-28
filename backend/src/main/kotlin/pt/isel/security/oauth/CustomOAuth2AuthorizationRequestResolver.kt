package pt.isel.security.oauth

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.auth.JwtService

@Component
class CustomOAuth2AuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    private val jwtService: JwtService
) : OAuth2AuthorizationRequestResolver {
    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization"
        )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return customize(defaultResolver.resolve(request), request)
    }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String?
    ): OAuth2AuthorizationRequest? {
        return customize(
            defaultResolver.resolve(request, clientRegistrationId),
            request
        )
    }

    private fun customize(
        authRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest
    ) : OAuth2AuthorizationRequest? {
        authRequest ?: return null

        val state = request.getParameter("state") ?: return authRequest

        return OAuth2AuthorizationRequest.from(authRequest)
            .state(state)
            .build()
    }
}

