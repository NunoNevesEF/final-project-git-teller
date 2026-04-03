package pt.isel.gitteller.model

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.domain.User
import pt.isel.domain.UserAuthentication
import pt.isel.domain.UserData
import pt.isel.model.UserDTO
import kotlin.test.Test

class UserDTOTest() {
    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(
            UserData(id, email, userName),
            listOf(UserAuthentication.FormAuthentication("test_pass"))
        )

    @Test
    fun `companion method create successfully creates UserDTO from User`(){
        val testUser = newUser()
        val expected = UserDTO(id = testUser.data.id, email = testUser.data.email, userName = testUser.data.userName)
        val actual = UserDTO.create(testUser)
        assertEquals(expected, actual)
    }
}