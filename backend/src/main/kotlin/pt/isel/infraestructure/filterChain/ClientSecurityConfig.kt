package pt.isel.infraestructure.filterChain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import pt.isel.infraestructure.oauth.CustomOAuth2AuthorizationRequestResolver
import pt.isel.infraestructure.oauth.handler.CustomOAuth2AuthenticationSuccessHandler
import pt.isel.infraestructure.oauth.CustomOAuth2UserService

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
                    .requestMatchers(
                        *publicPages,
                        publicAPI,
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                    .requestMatchers(privateAPI).authenticated()
                    .anyRequest().authenticated()
            }
            .csrf{ it.disable() }
            .sessionManagement { sessionManager ->
                sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2Login { oauthLogin ->
                oauthLogin
                    .authorizationEndpoint{ endpoint ->
                        endpoint.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                    }
                    .userInfoEndpoint { userInfo -> userInfo.userService(customOAuth2UserService) }
                    .successHandler(customOAuth2AuthenticationSuccessHandler)
                    .failureUrl("https://frontend-production-fc0c.up.railway.app/login?oauthError=true")
            }
            .logout { logout ->
                logout
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessUrl("https://frontend-production-fc0c.up.railway.app/login")
                    .permitAll()
            }


        return http.build()
    }
}