package pt.isel.infraestructure.security.principal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import pt.isel.entity.account.User

//TODO: DOCUMENT

class UserPrincipal(
    private val user: User,
    private val passwordHash: String? = null,
    private val oAuth2Id: String = "",
    private val attributes: Map<String, Any> = emptyMap()
) : UserDetails, OAuth2User {
    override fun getAttributes(): Map<String, Any> = attributes
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_" + user.role.name))

    override fun getPassword(): String? = passwordHash
    override fun getUsername(): String = user.email
    override fun getName(): String = user.email

    fun getUserId() = user.id
    fun getOAuth2Id() = oAuth2Id
}