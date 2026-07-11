package pt.isel.infraestructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.infraestructure.security.principal.FormUserDetailsService

/**
 *  `PasswordAuthenticationConfig`
 *
 * Configures password encoding and authentication components for Spring Security.
 * */
@Configuration
class PasswordAuthenticationConfig{
    /**
     * Defines the PasswordEncoder used to hash and verify user passwords.
     * */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    /**
     * Configures the AuthenticationProvider used to authenticate username/password requests.
     * Note: In this application email is used in place of username.
     *
     * It loads the authenticated user [UserPrincipal], through the provided [UserDetailsService] and verifies passwords
     * using the configured PasswordEncoder. In this application's case, the injected [FormUserDetailsService].
     *
     * @param userDetailsService the service used to load UserDetails during authentication. In this application [UserPrincipal]
     * is used as an aggregate of UserDetails and OAuth2User as to work with both registration methods.
     */
    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService): DaoAuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder())
        return authenticationProvider
    }
    /**
     * Exposes the application's [AuthenticationManager].
     *
     * The [AuthenticationManager] delegates authentication to the configured
     * AuthenticationProvider, [authenticationProvider].
     */
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }
}