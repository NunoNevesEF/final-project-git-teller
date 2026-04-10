package pt.isel.service.account

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthUserWrapper
import pt.isel.service.getOrThrow
import kotlin.random.Random

@Service
class CustomOAuth2UserService(private val accountService: AccountService) : DefaultOAuth2UserService() {
    var temp = 0

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val email = getEmail(oAuth2User, registrationId)
        val userName = oAuth2User.name

        val user = accountService.oAuthSignUp(email, userName, registrationId)
            .getOrThrow(::mapToOAuthException)

        return UserPrincipal(user, attributes = oAuth2User.attributes)

        /*val accessTokenTest = userRequest.accessToken
        println(accessTokenTest.toString())*/

        /*oAuth2User.attributes.entries.forEach { entry ->
           println("${entry.key} : ${entry.value}")
       }*/
    }

    private fun getEmail(oauth2User: OAuth2User, registrationId: String): String {
        //FIXME: IMPROPER GITHUB IMPLEMENTATION
        //1 - In github case it should call getGithubEmail due to the odd cases of this function.
        return when (registrationId) {
            "google" -> oauth2User.getAttribute("email")!!
            "github" -> oauth2User.getAttribute("email") ?: "Fallback${temp++}"
            else -> throw OAuth2AuthenticationException("Unknown Provider")
        }
    }

    private fun getGithubEmail(){
        //TODO: IMPLEMENT
        //1 - User checks if oauth2User has email attribute. If yes, returns.
        //2 - If oauth2User has no email attribute then make call to github services to return email.
    }


    private fun mapToOAuthException(error: AccountServiceError): OAuth2AuthenticationException =
        when (error) {
            is DuplicateAccountTypeError ->
                OAuth2AuthenticationException("Account already linked for this provider")

            is EmailAlreadyExists ->
                OAuth2AuthenticationException("Tried to create but email already in use")

            else ->
                OAuth2AuthenticationException("Authentication failed - Unknown Error")
        }
    //TODO: ADD LOGIC FOR GITHUB, GOOGLE AND GITLAB
    //TODO: TESTING
}