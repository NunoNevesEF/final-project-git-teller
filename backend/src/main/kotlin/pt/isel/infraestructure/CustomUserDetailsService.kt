package pt.isel.infraestructure

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.utils.rightOrNull

@Service
class CustomUserDetailsService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.findByEmail(email).rightOrNull() ?: throw UsernameNotFoundException("User not found")
//        val account = linkedAccountService.findUserFormAccount(user.id)
//        if(account !is Success) throw UsernameNotFoundException("User not found")

        return UserPrincipal(user, "t")
    }
}