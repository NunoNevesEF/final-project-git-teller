package pt.isel.service.account

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.User
import pt.isel.service.isFailure
import pt.isel.service.rightOrNull

@Service
class CustomUserDetailsService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.findByEmail(username) ?: throw UsernameNotFoundException("User not found")
        val account = linkedAccountService.readByUserAndType(user.id, FormLinkedAccount.getType())
        if(account.isFailure()) throw UsernameNotFoundException("User not found")

        val passwordHash = (account.rightOrNull() as FormLinkedAccount).passwordHash
        return UserPrincipal(user, passwordHash)
    }
}

class UserPrincipal(
    private val user: User,
    private val passwordHash: String? = null,
    private val attributes: Map<String, Any> = emptyMap()
) : UserDetails, OAuth2User {
    override fun getAttributes(): Map<String, Any> = attributes
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_" + user.role.name))

    override fun getPassword(): String? = passwordHash
    override fun getUsername(): String = user.email
    override fun getName(): String = user.email
}