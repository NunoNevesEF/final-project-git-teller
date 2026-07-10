package pt.isel.gitteller.infraestructure

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.infraestructure.security.principal.FormUserDetailsService
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.UserNotFound
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class FormUserDetailsServiceTest {

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var linkedAccountService: LinkedAccountService

    @InjectMocks
    lateinit var service: FormUserDetailsService

    private val validUser = User(
        id = 1,
        email = "test@email.com",
        username = "test"
    )

    private val validFormLinkedAccount = FormLinkedAccount(
        id = 1,
        user = validUser,
        passwordHash = "passwordHash"
    )

    @Test
    fun `loadUserByUsername returns UserPrincipal`() {

        whenever(userService.findByEmail(validUser.email))
            .thenReturn(success(validUser))

        whenever(linkedAccountService.findUserFormAccount(validUser.id))
            .thenReturn(success(validFormLinkedAccount))

        val result = service.loadUserByUsername(validUser.email)

        Assertions.assertEquals(validUser.email, result.username)
        Assertions.assertEquals("passwordHash", result.password)
    }

    @Test
    fun `loadUserByUsername throws when user not found`() {

        whenever(userService.findByEmail(validUser.email))
            .thenReturn(failure(UserNotFound))

        assertFailsWith<UsernameNotFoundException> {
            service.loadUserByUsername(validUser.email)
        }
    }

    @Test
    fun `loadUserByUsername throws when form account not found`() {

        whenever(userService.findByEmail(validUser.email))
            .thenReturn(success(validUser))

        whenever(linkedAccountService.findUserFormAccount(validUser.id))
            .thenReturn(failure(LinkedAccountNotFound))

        assertFailsWith<UsernameNotFoundException> {
            service.loadUserByUsername(validUser.email)
        }
    }
}