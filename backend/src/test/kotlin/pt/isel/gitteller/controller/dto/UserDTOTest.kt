package pt.isel.gitteller.controller.dto

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.controller.account.dto.UserDTO
import pt.isel.entity.account.User
import kotlin.test.Test

class UserDTOTest {
    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    @Test
    fun `User correctly maps to UserDTO`(){
        val expected = UserDTO(validUser.id, validUser.email, validUser.username!!)
        val actual = UserDTO(validUser)

        assertEquals(expected, actual)
    }
}