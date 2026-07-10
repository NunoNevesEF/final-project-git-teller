package pt.isel.infraestructure.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pt.isel.infraestructure.security.principal.UserPrincipalService

//TODO: DOCUMENT

@Component
class JWTAuthenticationFilter(
    private val jwtService: JwtService,
    private val userPrincipalService: UserPrincipalService
) : OncePerRequestFilter() {
    private val tokenPrefix = "Bearer "

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(tokenPrefix.length)

        try {
            if (SecurityContextHolder.getContext().authentication == null &&
                jwtService.isValidToken(token)
            ) {
                val username = jwtService.getUsername(token)
                val userDetails = userPrincipalService.loadUserByUsername(username)

                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )

                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }

        } catch (_: Exception) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}

