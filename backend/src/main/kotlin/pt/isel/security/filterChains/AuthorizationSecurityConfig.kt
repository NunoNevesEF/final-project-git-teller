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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
class AuthorizationSecurityConfig(
    private val jwtAuthenticationFilter: JWTAuthenticationFilter,
) {
    @Bean
    @Order(1)
    fun authorizationSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Enable CORS with our custom configuration
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }

            .csrf{ csrf -> csrf.disable() }
            .securityMatcher("/api/**")
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/**").permitAll()   //   TODO: Remove this line when all endpoints are protected
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


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:8081",   // Expo web dev
        )

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
        configuration.exposedHeaders = listOf("X-Total-Count", "X-Page-Number")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source

    }
}