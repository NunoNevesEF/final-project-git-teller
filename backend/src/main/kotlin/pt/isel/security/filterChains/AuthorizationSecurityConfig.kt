package pt.isel.security.filterChains

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import pt.isel.security.authenticationFilters.JWTAuthenticationFilter

@Configuration
@EnableWebSecurity
class AuthorizationSecurityConfig(
    private val jwtAuthenticationFilter: JWTAuthenticationFilter,
) {
    @Bean
    @Order(1)
    fun authorizationSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf{ csrf -> csrf.disable() }
            .securityMatcher("/api/**")
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .sessionManagement { sessionManager ->
                sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}