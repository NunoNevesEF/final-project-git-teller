package pt.isel.service.userServices

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import pt.isel.domain.OAuthUserWrapper
import pt.isel.domain.User
import pt.isel.domain.UserAuthentication
import pt.isel.domain.UserData
import pt.isel.repository.memory.UserRepoMem
import kotlin.random.Random

@Service
class CustomOAuth2UserService(private val userRepo: UserRepoMem) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        oAuth2User.attributes.entries.forEach { entry ->
            println("${entry.key} : ${entry.value}")
        }

        val registrationId = userRequest.clientRegistration.registrationId
        val email = getEmail(oAuth2User, registrationId)
        val userName = oAuth2User.name

        val user = readOrCreateUser(email, userName, registrationId)

        return OAuthUserWrapper(user, oAuth2User)
    }

    private fun getEmail(oauth2User: OAuth2User, registrationId: String): String {
        //FIXME: IMPROPER GITHUB IMPLEMENTATION
        //1 - In github case it should call getGithubEmail due to the odd cases of this function.
        return when (registrationId) {
            "google" -> oauth2User.getAttribute("email")!!
            "github" -> oauth2User.getAttribute("email") ?: "Fallback To Be Changed + ${Random.nextInt()}"
            else -> throw OAuth2AuthenticationException("Unknown Provider")
        }
    }

    private fun getGithubEmail(){
        //TODO: IMPLEMENT
        //1 - User checks if oauth2User has email attribute. If yes, returns.
        //2 - If oauth2User has no email attribute then make call to github services to return email.
    }

    private fun readOrCreateUser(email: String, userName: String, provider: String): User =
        userRepo.read(email) ?: userRepo.create(newUser(email, userName, provider))

    private fun newUser(email: String, userName: String, provider: String): User = User(
        UserData.create(email = email, userName = userName),
         listOf(UserAuthentication.OAuthAuthentication(provider))
    )

    //TODO: ADD LOGIC FOR GITHUB, GOOGLE AND GITLAB
    //TODO: TESTING
}