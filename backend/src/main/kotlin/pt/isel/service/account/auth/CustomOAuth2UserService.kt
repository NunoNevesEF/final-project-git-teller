package pt.isel.service.account.auth

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import pt.isel.domain.account.UserPrincipal
import pt.isel.service.account.AccountService
import pt.isel.service.account.AccountServiceError
import pt.isel.service.account.DuplicateAccountTypeError
import pt.isel.service.account.EmailAlreadyExists
import pt.isel.service.getOrThrow
import pt.isel.service.git.GithubCommunicationService

@Service
class CustomOAuth2UserService(
    private val accountService: AccountService,
    private val githubCommunicationService: GithubCommunicationService
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val email = getEmail(oAuth2User, registrationId, userRequest.accessToken.tokenValue)
        val userName = oAuth2User.name

        val user = accountService.oAuthSignUp(email, userName, registrationId)
            .getOrThrow(::mapToOAuthException)

        return UserPrincipal(user, attributes = oAuth2User.attributes)
    }

    private fun getEmail(oauth2User: OAuth2User, registrationId: String, accessTokenValue: String): String {
        return when (registrationId) {
            "google" -> getGoogleEmail(oauth2User)
            "github" -> getGithubEmail(oauth2User, accessTokenValue)
            else -> throw OAuth2AuthenticationException("Unknown Provider")
        }
    }

    private fun getGoogleEmail(oauth2User: OAuth2User): String =
        oauth2User.getAttribute("email")
            ?: throw OAuth2AuthenticationException("Google Authentication Failed - Email Not Found")

    private fun getGithubEmail(oauth2User: OAuth2User, accessTokenValue: String): String =
        oauth2User.getAttribute<String>("email")
            ?: (githubCommunicationService.getPrimaryEmailOrNull(accessTokenValue)
                ?: throw OAuth2AuthenticationException("Github Authentication Failed - Email Not Found"))

    private fun mapToOAuthException(error: AccountServiceError): OAuth2AuthenticationException =
        when (error) {
            is DuplicateAccountTypeError ->
                OAuth2AuthenticationException("Account already linked for this provider")

            is EmailAlreadyExists ->
                OAuth2AuthenticationException("Tried to create but email already in use")

            else ->
                OAuth2AuthenticationException("Authentication failed - Unknown Error")
        }
}