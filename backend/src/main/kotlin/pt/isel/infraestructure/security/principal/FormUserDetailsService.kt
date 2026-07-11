package pt.isel.infraestructure.security.principal

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.utils.Success
import pt.isel.utils.rightOrNull

/**
 *  `FormUserDetailsService`
 *
 * Returns a authenticated user that has a form registration linked to them. Used for User/Password login.
 * */
@Service
class FormUserDetailsService(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) : UserDetailsService {
    /**.
     * @param email The email address that identifies the user.
     *
     * @return the [UserPrincipal] which represents an authenticated user in the application
     * @throws UsernameNotFoundException if no user exists for the given email or if the user has no linked form account.
     */
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.findByEmail(email).rightOrNull() ?: throw UsernameNotFoundException("User not found")
        val account = linkedAccountService.findUserFormAccount(user.id)
        if (account !is Success) throw UsernameNotFoundException("Form account not found")

        return UserPrincipal(user, account.right.passwordHash)
    }
}