package pt.isel.domain.account

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User


data class User(
    val id: Int,
    val email: String,
    val userName: String,
    val role: Role = Role.USER
){
    //TODO: CONSIDER MORE THINGS LIKE TRIMMING, MINIMUM SIZES AND VALID EMAIL FORMATS
    init{
        require(id >= 0) { "id must be >= 0" }
        require(!email.isBlank()) { "Email cannot be blank" }
        require(!userName.isBlank()) { "UserName cannot be blank" }
    }
    companion object{
        fun create(id: Int = 0, email: String, userName: String) =
            User(0, email, userName)
    }
}



data class OAuthUserWrapper(
    val user: User,
    val oauthUser: OAuth2User
) : OAuth2User {
    override fun getAttributes(): Map<String?, Any?>? = oauthUser.attributes

    override fun getAuthorities(): Collection<GrantedAuthority?>? = oauthUser.authorities

    override fun getName(): String = user.userName
}