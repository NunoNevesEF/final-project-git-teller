package pt.isel.infraestructure.oauth

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.service.error.ServiceError
import pt.isel.service.account.AccountOrchestrator
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.EmailAlreadyExists
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.utils.getOrThrow
import pt.isel.service.gitProviders.GithubCommunicationService

//TODO: DOCUMENT

@Service
class CustomOAuth2UserService(
    private val accountService: AccountOrchestrator,
    private val githubCommunicationService: GithubCommunicationService,
    private val jwtService: JwtService,
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val email = getEmail(oAuth2User, registrationId, userRequest.accessToken.tokenValue)
        val oAuth2id = getId(oAuth2User, registrationId)

        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val state = request.getParameter("state")

        if(state != null && jwtService.isValidLinkState(state, registrationId)) {
            val payload = jwtService.parseLinkState(state)
            val user = accountService.oAuthLink(payload.userId, registrationId, oAuth2id)
                .getOrThrow(::mapToOAuthException)

            return UserPrincipal(user.user, oAuth2Id = oAuth2id, attributes = oAuth2User.attributes) //temp
        } else{
            val user = accountService.oAuthSignUp(email, registrationId, oAuth2id)
                .getOrThrow(::mapToOAuthException)

            return UserPrincipal(user.user, oAuth2Id = oAuth2id, attributes = oAuth2User.attributes)
        }
    }

    private fun getEmail(oauth2User: OAuth2User, registrationId: String, accessTokenValue: String): String {
        return when (registrationId) {
            "google" -> getGoogleEmail(oauth2User)
            "github" -> getGithubEmail(oauth2User, accessTokenValue)
            "gitlab" -> getGitlabEmail(oauth2User)
            else -> throw OAuth2AuthenticationException("Unknown Provider")
        }
    }

    private fun getId(oauth2User: OAuth2User, registrationId: String): String {
        return when (registrationId) {
            "google" -> oauth2User.name
            "github" -> getGithubId(oauth2User)
            "gitlab" -> getGitlabId(oauth2User)
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

    private fun getGitlabEmail(oauth2User: OAuth2User): String =
        oauth2User.getAttribute("email")
        ?: throw OAuth2AuthenticationException("Gitlab Authentication Failed - Email Not Found")

    private fun getGithubId(oauth2User: OAuth2User): String =
        oauth2User.getAttribute<Int?>("id")?.toString() ?:
            throw OAuth2AuthenticationException("Github Authentication Failed - Id Not Found")

    private fun getGitlabId(oauth2User: OAuth2User): String =
        oauth2User.getAttribute<Int?>("id")?.toString() ?:
        throw OAuth2AuthenticationException("Gitlab Authentication Failed - Id Not Found")

    private fun mapToOAuthException(error: ServiceError): OAuth2AuthenticationException =
        when (error) {
            is LinkedAccountTypeMaxed ->
                OAuth2AuthenticationException("Account already linked for this provider")

            is EmailAlreadyExists ->
                OAuth2AuthenticationException("Tried to create but email already in use")

            else ->
                OAuth2AuthenticationException("Authentication failed - Unknown Error")
        }
}