package pt.isel.security.filterChain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import pt.isel.security.oauth.CustomOAuth2AuthorizationRequestResolver
import pt.isel.security.oauth.handler.CustomOAuth2AuthenticationSuccessHandler
import pt.isel.security.oauth.CustomOAuth2UserService

@Configuration
@EnableWebSecurity
class ClientSecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val customOAuth2AuthenticationSuccessHandler: CustomOAuth2AuthenticationSuccessHandler,
    private val customOAuth2AuthorizationRequestResolver: CustomOAuth2AuthorizationRequestResolver
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
            .formLogin { formLogin ->
                formLogin
                    .permitAll()
                    .failureUrl("http://localhost:8081/login?formError=true")
                    .defaultSuccessUrl("http://localhost:8081/home", true)
            }
            .oauth2Login { oauthLogin ->
                oauthLogin
                    .authorizationEndpoint{ endpoint ->
                        endpoint.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                    }
                    .userInfoEndpoint { userInfo -> userInfo.userService(customOAuth2UserService) }
                    .successHandler(customOAuth2AuthenticationSuccessHandler)
                    .failureUrl("http://localhost:8081/login?oauthError=true")
            }
            .logout { logout ->
                logout
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessUrl("http://localhost:8081/login")
                    .permitAll()
            }


        return http.build()
    }
}