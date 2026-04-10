package pt.isel.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import pt.isel.service.account.CustomOAuth2UserService

@Configuration
@EnableWebSecurity
class ClientSecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
) {
    private val signUpPage = "/signup"
    private val loginPage = "/login"
    private val publicPages = arrayOf("/", "/error", signUpPage, loginPage, "/css/**", "/js/**", "/images/**")
    private val publicAPI = "/api/public/**"
    private val privateAPI = "/api/private/**"

    //TODO: Add defaultSuccessUrl/failureUrl redirect to login. Add logoutSuccessUrl to logout.
    @Order(2)
    @Bean
    fun clientSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authRequest -> authRequest
                    //.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                    .requestMatchers(*publicPages, publicAPI).permitAll()
                    .requestMatchers(privateAPI).authenticated()
                    .anyRequest().authenticated()
            }
            .csrf{ it.disable() }
            .formLogin { formLogin -> formLogin.loginPage(loginPage).permitAll() }
            .oauth2Login{ oauthLogin -> oauthLogin
                .loginPage(loginPage).permitAll()
                .userInfoEndpoint { userInfo -> userInfo
                    .userService(customOAuth2UserService)
                }
            }
            .logout{
                logout -> logout
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
            }
        return http.build()
    }
}