package pt.isel.security.successHandlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import pt.isel.service.account.auth.JwtService
import tools.jackson.databind.ObjectMapper

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val jwtService: JwtService
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val tokenPair = jwtService.generateTokenPair(authentication)
        response.status = HttpStatus.OK.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        ObjectMapper().writeValue(response.writer, tokenPair)
    }
}