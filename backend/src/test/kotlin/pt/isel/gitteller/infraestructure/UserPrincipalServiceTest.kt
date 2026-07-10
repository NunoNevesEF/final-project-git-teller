package pt.isel.gitteller.infraestructure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import pt.isel.entity.account.User
import pt.isel.infraestructure.security.principal.UserPrincipalService
import pt.isel.service.account.UserService
import pt.isel.service.error.UserNotFound
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class UserPrincipalServiceTest {

    @Mock
    lateinit var userService: UserService

    @InjectMocks
    lateinit var service: UserPrincipalService

    private val user = User(
        id = 1,
        email = "test@email.com",
        username = "test"
    )

    @Test
    fun `loadUserByUsername returns UserPrincipal`() {

        whenever(userService.findByEmail(user.email))
            .thenReturn(success(user))

        val result = service.loadUserByUsername(user.email)

        assertEquals(user.email, result.username)
    }

    @Test
    fun `loadUserByUsername throws when user not found`() {

        whenever(userService.findByEmail(user.email))
            .thenReturn(failure(UserNotFound))

        assertFailsWith<UsernameNotFoundException> {
            service.loadUserByUsername(user.email)
        }
    }
}