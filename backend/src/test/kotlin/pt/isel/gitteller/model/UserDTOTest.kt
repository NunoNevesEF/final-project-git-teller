package pt.isel.gitteller.model

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.domain.account.User
import pt.isel.model.UserDTO
import kotlin.test.Test

class UserDTOTest() {
    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)


    @Test
    fun `companion method create successfully creates UserDTO from User`(){
        val testUser = newUser()
        val expected = UserDTO(id = testUser.id, email = testUser.email, userName = testUser.userName)
        val actual = UserDTO.create(testUser)
        assertEquals(expected, actual)
    }
}