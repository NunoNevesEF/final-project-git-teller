package pt.isel.service.account.auth

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
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