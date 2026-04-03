package pt.isel.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User


data class User(
    val data: UserData,
    val authentication: List<UserAuthentication>,
)

data class UserData(
    val id: Int, val email: String, val userName: String
){
    //TODO: CONSIDER MORE THINGS LIKE TRIMMING, MINIMUM SIZES AND VALID EMAIL FORMATS
    init{
        require(id >= 0) { "id must be >= 0" }
        require(!email.isBlank()) { "Email cannot be blank" }
        require(!userName.isBlank()) { "UserName cannot be blank" }
    }
    companion object{
        fun create(id: Int = 0, email: String, userName: String) =
            UserData(0, email, userName)
    }
}

sealed class UserAuthentication{
    class OAuthAuthentication(val provider: String) : UserAuthentication()
    data class FormAuthentication(val passwordHash: String) : UserAuthentication()
}

data class OAuthUserWrapper(
    val user: User,
    val oauthUser: OAuth2User
) : OAuth2User {
    override fun getAttributes(): Map<String?, Any?>? = oauthUser.attributes

    override fun getAuthorities(): Collection<GrantedAuthority?>? = oauthUser.authorities

    override fun getName(): String = user.data.userName
}