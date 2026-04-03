package pt.isel.gitteller.service

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import pt.isel.domain.User
import pt.isel.domain.UserAuthentication
import pt.isel.domain.UserData
import pt.isel.model.UserDTO
import pt.isel.repository.memory.UserRepoMem
import pt.isel.service.isFailure
import pt.isel.service.isSuccess
import pt.isel.service.leftOrNull
import pt.isel.service.rightOrNull
import pt.isel.service.userServices.EmailAlreadyExists
import pt.isel.service.userServices.FormUserService
import pt.isel.service.userServices.UserNotFound
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class FormUserServiceTest {
    @Mock
    private lateinit var userRepo: UserRepoMem

    @InjectMocks
    lateinit var userService: FormUserService

    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(
            UserData(id, email, userName),
            listOf(UserAuthentication.FormAuthentication("test_pass"))
        )

    private fun getFormPasswordHash(user: User): String {
        val authentication = user.authentication.first() as UserAuthentication.FormAuthentication
        return authentication.passwordHash
    }

    @Test
    fun `method create returns the created User if email not found in repo`() {
        val testUser = newUser()
        val expected = UserDTO.create(testUser)

        whenever(userRepo.read(testUser.data.email)).thenReturn(null)
        whenever(userRepo.create(any())).thenReturn(testUser)

        val actual = userService.create(testUser.data.email, testUser.data.userName, getFormPasswordHash(testUser))

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method create returns EmailAlreadyExists error if email found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.data.email)).thenReturn(testUser)

        val actual = userService.create(testUser.data.email, testUser.data.userName, getFormPasswordHash(testUser))

        assertTrue(actual.isFailure())
        assertEquals(EmailAlreadyExists, actual.leftOrNull())
    }

    @Test
    fun `method read returns the read User if id found in repo`() {
        val testUser = newUser()
        val expected = UserDTO.create(testUser)

        whenever(userRepo.read(testUser.data.id)).thenReturn(testUser)

        val actual = userService.read(testUser.data.id)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method read returns UserNotFound error if id not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.data.id)).thenReturn(null)

        val actual = userService.read(testUser.data.id)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method read returns the read User if email found in repo`() {
        val testUser = newUser()
        val expected = UserDTO.create(testUser)

        whenever(userRepo.read(testUser.data.email)).thenReturn(testUser)

        val actual = userService.read(testUser.data.email)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method read returns UserNotFound error if email not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.data.email)).thenReturn(null)

        val actual = userService.read(testUser.data.email)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method update returns updated User if id found in repo`() {
        val updateUserName = "Updated_UserName"
        val testUser = newUser()
        val updatedTestUser = testUser.copy(data = testUser.data.copy(userName = updateUserName))
        val expected = UserDTO.create(updatedTestUser)

        whenever(userRepo.read(testUser.data.id)).thenReturn(testUser)
        whenever(userRepo.update(updatedTestUser)).thenReturn(updatedTestUser)

        val actual = userService.update(testUser.data.id, updateUserName)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method update returns UserNotFound error if id not found in repo`(){
        val updateUserName = "Updated_UserName"
        val testUser = newUser()

        whenever(userRepo.read(testUser.data.id)).thenReturn(null)

        val actual = userService.update(testUser.data.id, updateUserName)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method delete returns deleted User if id found in repo`() {
        val testUser = newUser()
        val expected = UserDTO.create(testUser)

        whenever(userRepo.delete(testUser.data.id)).thenReturn(testUser)

        val actual = userService.delete(testUser.data.id)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method delete returns UserNotFound error if id not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.delete(testUser.data.id)).thenReturn(null)

        val actual = userService.delete(testUser.data.id)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }
}