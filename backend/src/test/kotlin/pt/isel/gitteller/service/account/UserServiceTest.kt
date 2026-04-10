package pt.isel.gitteller.service.account

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import pt.isel.domain.account.User
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.service.account.EmailAlreadyExists
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.service.isFailure
import pt.isel.service.isSuccess
import pt.isel.service.leftOrNull
import pt.isel.service.rightOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    private lateinit var userRepo: UserRepoMem

    @InjectMocks
    lateinit var userService: UserService

    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)

    @Test
    fun `method create returns the created User if email not found in repo`() {
        val expected = newUser()

        whenever(userRepo.read(expected.email)).thenReturn(null)
        whenever(userRepo.create(any())).thenReturn(expected)

        val actual = userService.create(expected.email, expected.userName)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method create returns EmailAlreadyExists error if email found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.email)).thenReturn(testUser)

        val actual = userService.create(testUser.email, testUser.userName)

        assertTrue(actual.isFailure())
        assertEquals(EmailAlreadyExists, actual.leftOrNull())
    }

    @Test
    fun `method read returns the read User if id found in repo`() {
        val expected = newUser()

        whenever(userRepo.read(expected.id)).thenReturn(expected)

        val actual = userService.read(expected.id)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method read returns UserNotFound error if id not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.id)).thenReturn(null)

        val actual = userService.read(testUser.id)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method read returns the read User if email found in repo`() {
        val expected = newUser()

        whenever(userRepo.read(expected.email)).thenReturn(expected)

        val actual = userService.read(expected.email)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method read returns UserNotFound error if email not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.read(testUser.email)).thenReturn(null)

        val actual = userService.read(testUser.email)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method update returns updated User if id found in repo`() {
        val updateUserName = "Updated_UserName"
        val testUser = newUser()
        val expected = testUser.copy(userName = updateUserName)

        whenever(userRepo.read(testUser.id)).thenReturn(testUser)
        whenever(userRepo.update(expected)).thenReturn(expected)

        val actual = userService.update(testUser.id, updateUserName)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method update returns UserNotFound error if id not found in repo`(){
        val updateUserName = "Updated_UserName"
        val testUser = newUser()

        whenever(userRepo.read(testUser.id)).thenReturn(null)

        val actual = userService.update(testUser.id, updateUserName)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }

    @Test
    fun `method delete returns deleted User if id found in repo`() {
        val expected = newUser()

        whenever(userRepo.delete(expected.id)).thenReturn(expected)

        val actual = userService.delete(expected.id)

        assertTrue(actual.isSuccess())
        assertEquals(expected, actual.rightOrNull())
    }

    @Test
    fun `method delete returns UserNotFound error if id not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.delete(testUser.id)).thenReturn(null)

        val actual = userService.delete(testUser.id)

        assertTrue(actual.isFailure())
        assertEquals(UserNotFound, actual.leftOrNull())
    }
}