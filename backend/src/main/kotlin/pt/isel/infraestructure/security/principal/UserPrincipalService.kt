package pt.isel.infraestructure.security.principal

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.service.account.UserService
import pt.isel.utils.rightOrNull

/**
 *  `UserPrincipalService`
 *
 * Returns any authenticated regardless of login method
 * */
@Service
class UserPrincipalService(
    private val userService: UserService
) {
    /**.
     * @param email The email address that identifies the user.
     *
     * @return the [UserPrincipal] which represents an authenticated user in the application
     * @throws UsernameNotFoundException if no user exists for the given email.
     */
    fun loadUserByUsername(email: String): UserPrincipal {
        val user = userService.findByEmail(email).rightOrNull() ?: throw UsernameNotFoundException("User not found")
        return UserPrincipal(user)
    }
}