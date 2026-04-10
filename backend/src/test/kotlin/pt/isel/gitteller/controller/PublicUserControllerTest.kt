package pt.isel.gitteller.controller

/*import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.isel.controller.account.UserController
import pt.isel.domain.User
import pt.isel.domain.LinkedAccount
import pt.isel.model.UserDTO
import pt.isel.service.failure
import pt.isel.service.success
import pt.isel.service.account.EmailAlreadyExists
import pt.isel.service.account.UserService
import pt.isel.service.account.UserNotFound
import tools.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals

@WebMvcTest(UserController::class,
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ClientAutoConfiguration::class,
        OAuth2ClientWebSecurityAutoConfiguration::class
    ])
@AutoConfigureMockMvc(addFilters = false)
class PublicUserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(
            User(id, email, userName),
            listOf(LinkedAccount.FormLinkedAccount("test_pass"))
        )

    private fun getFormPasswordHash(user: User): String {
        val authentication = user.authentication.first() as LinkedAccount.FormLinkedAccount
        return authentication.passwordHash
    }

    @Test
    fun `method signup returns Ok (200) and user when success`() {
        val testUser = newUser()
        val passwordHash = getFormPasswordHash(testUser)

        val expected = UserDTO.create(testUser)

        whenever(userService.create(testUser.data.email, testUser.data.userName, passwordHash))
            .thenReturn(success(expected))

        val mvcResult = mockMvc.perform(
            get("/api/public/users/signup")
                .param("email", testUser.data.email)
                .param("userName", testUser.data.userName)
                .param("password", passwordHash)
        ).andExpect(status().isOk).andReturn()

        val jsonResponse = mvcResult.response.contentAsString
        val actual = ObjectMapper().readValue(jsonResponse, UserDTO::class.java)
        assertEquals(expected, actual)
    }

    @Test
    fun `method signup returns NotFound (404) when failure`(){
        val testUser = newUser()
        val passwordHash = getFormPasswordHash(testUser)

        whenever(userService.create(testUser.data.email, testUser.data.userName, passwordHash))
            .thenReturn(failure(EmailAlreadyExists))

        mockMvc.perform(
            get("/api/public/users/signup/form")
                .param("email", testUser.data.email)
                .param("userName", testUser.data.userName)
                .param("password", passwordHash)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `method read (id) returns Ok (200) and user when success`() {
        val testUser = newUser()

        val expected = UserDTO.create(testUser)

        whenever(userService.read(testUser.data.id))
            .thenReturn(success(expected))

        val mvcResult = mockMvc.perform(
            get("/api/public/users/id")
                .param("id", testUser.data.id.toString())
        ).andExpect(status().isOk).andReturn()

        val jsonResponse = mvcResult.response.contentAsString
        val actual = ObjectMapper().readValue(jsonResponse, UserDTO::class.java)
        assertEquals(expected, actual)
    }

    @Test
    fun `method read (id) returns NotFound (404) when failure`(){
        val testUser = newUser()

        whenever(userService.read(testUser.data.id))
            .thenReturn(failure(UserNotFound))

        mockMvc.perform(
            get("/api/public/users/id")
                .param("id", testUser.data.id.toString())
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `method read (email) returns Ok (200) and user when success`() {
        val testUser = newUser()

        val expected = UserDTO.create(testUser)

        whenever(userService.read(testUser.data.email))
            .thenReturn(success(expected))

        val mvcResult = mockMvc.perform(
            get("/api/public/users/email")
                .param("email", testUser.data.email)
        ).andExpect(status().isOk).andReturn()

        val jsonResponse = mvcResult.response.contentAsString
        val actual = ObjectMapper().readValue(jsonResponse, UserDTO::class.java)
        assertEquals(expected, actual)
    }

    @Test
    fun `method read (email) returns NotFound (404) when failure`(){
        val testUser = newUser()

        whenever(userService.read(testUser.data.email))
            .thenReturn(failure(UserNotFound))

        mockMvc.perform(
            get("/api/public/users/email")
                .param("email", testUser.data.email)
        ).andExpect(status().isNotFound)
    }
}*/