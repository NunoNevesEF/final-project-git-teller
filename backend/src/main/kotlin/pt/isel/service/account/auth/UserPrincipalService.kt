package pt.isel.service.account.auth

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.domain.account.UserPrincipal
import pt.isel.service.account.UserService

@Service
class UserPrincipalService(
    private val userService: UserService
) {
    fun loadUserByUsername(email: String): UserPrincipal {
        val user = userService.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found")

        return UserPrincipal(user)
    }
}