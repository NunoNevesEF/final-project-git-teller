package pt.isel.infraestructure.principal

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.service.account.UserService
import pt.isel.utils.rightOrNull

@Service
class UserPrincipalService(
    private val userService: UserService
) {
    fun loadUserByUsername(email: String): UserPrincipal {
        val user = userService.findByEmail(email).rightOrNull() ?: throw UsernameNotFoundException("User not found")
        return UserPrincipal(user)
    }
}