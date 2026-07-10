package pt.isel.gitteller.service.account

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import pt.isel.entity.account.User
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.service.account.UserService
import pt.isel.service.error.EmailAlreadyExists
import pt.isel.service.error.InvalidEmail
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.utils.isFailure
import pt.isel.utils.isSuccess
import pt.isel.utils.leftOrNull
import pt.isel.utils.rightOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    private lateinit var userRepo: UserRepoMem

    @InjectMocks
    lateinit var userService: UserService

    val validEmail = "test@email.com"
    val validUsername = "test"

    private fun newUser(id: Int = 0, email: String = validEmail, username: String? = validUsername) =
        User(id, email, username)

    @Test
    fun `method create returns the created User if success`() {
        val expected = newUser()

        whenever(userRepo.create(any())).thenReturn(expected)

        val actual = userService.create(expected.email, expected.username)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method create returns EmailAlreadyExists error if email found in repo`() {
        val testEmail = "some test email"
        val testUser = newUser(email = testEmail)

        whenever(userRepo.findByEmail(testUser.email)).thenReturn(testUser)

        val actual = userService.create(testUser.email, "some other username")

        assertTrue(actual is Failure)
        assertEquals(EmailAlreadyExists, actual.left)
    }

    @Test
    fun `method create returns UsernameAlreadyExists error if username found in repo`(){
        val testUsername = "some test username"
        val testUser = newUser(username = testUsername)

        whenever(userRepo.findByUsername(testUsername)).thenReturn(testUser)

        val actual = userService.create("some other email", testUser.username)

        assertTrue(actual is Failure)
        assertEquals(UsernameAlreadyExists, actual.leftOrNull())
    }

    @Test
    fun `method create returns InvalidEmail error if email is blank`(){
        val actual = userService.create(email = " ")

        assertTrue(actual is Failure)
        assertEquals(InvalidEmail, actual.left)
    }

    @Test
    fun `method create returns InvalidUsername error if username is blank`(){
        val actual = userService.create(email = validEmail, username = " ")

        assertTrue(actual is Failure)
        assertEquals(InvalidUsername, actual.left)
    }

    @Test
    fun `method create succeeds on null username`(){
        val testUser = newUser(username = null)

        whenever(userRepo.create(any())).thenReturn(testUser)

        val actual = userService.create(testUser.email, null)

        assertTrue(actual is Success)
        assertEquals(testUser, actual.right)
    }

    @Test
    fun `method findById returns the found User if id found in repo`() {
        val expected = newUser()

        whenever(userRepo.findById(expected.id)).thenReturn(expected)

        val actual = userService.findById(expected.id)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findById returns UserNotFound error if id not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.findById(testUser.id)).thenReturn(null)

        val actual = userService.findById(testUser.id)

        assertTrue(actual is Failure)
        assertEquals(UserNotFound, actual.left)
    }

    @Test
    fun `method findByEmail returns the found User if email found in repo`() {
        val expected = newUser()

        whenever(userRepo.findByEmail(expected.email)).thenReturn(expected)

        val actual = userService.findByEmail(expected.email)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method findByEmail returns UserNotFound error if email not found in repo`() {
        val testUser = newUser()

        whenever(userRepo.findByEmail(testUser.email)).thenReturn(null)

        val actual = userService.findByEmail(testUser.email)

        assertTrue(actual is Failure)
        assertEquals(UserNotFound, actual.left)
    }

    @Test
    fun `method update returns updated User if success`() {
        val newUsername = "new username"
        val testUser = newUser()
        val expected = testUser.copy(username = newUsername)

        whenever(userRepo.findById(testUser.id)).thenReturn(testUser)
        whenever(userRepo.update(expected)).thenReturn(expected)

        val actual = userService.updateUsername(testUser.id, newUsername)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method update returns UsernameAlreadyExistsError if another user already has that username`(){
        val newUsername = "new username"
        val testUser = newUser()
        val anotherUser = newUser(id = testUser.id + 1, username = newUsername)

        whenever(userRepo.findByUsername(newUsername)).thenReturn(anotherUser)

        val actual = userService.updateUsername(testUser.id, newUsername)

        assertTrue(actual is Failure)
        assertEquals(UsernameAlreadyExists, actual.left)
    }

    @Test
    fun `method update succeeds on repeated username if it belongs to user`(){
        val newUsername = "new username"
        val expected = newUser(username = newUsername)

        whenever(userRepo.findByUsername(newUsername)).thenReturn(expected)
        whenever(userRepo.findById(expected.id)).thenReturn(expected)
        whenever(userRepo.update(expected)).thenReturn(expected)

        val actual = userService.updateUsername(expected.id, newUsername)

        assertTrue(actual is Success)
        assertEquals(expected, actual.right)
    }

    @Test
    fun `method update returns UserNotFound error if id not found in repo`(){
        val newUsername = "new username"
        val testUser = newUser()

        whenever(userRepo.findById(testUser.id)).thenReturn(null)

        val actual = userService.updateUsername(testUser.id, newUsername)

        assertTrue(actual is Failure)
        assertEquals(UserNotFound, actual.left)
    }

    @Test
    fun `method update returns InvalidUsername error if new username is blank`(){
        val testUser = newUser()

        whenever(userRepo.findById(testUser.id)).thenReturn(testUser)

        val actual = userService.updateUsername(testUser.id, " ")

        assertTrue(actual.isFailure())
        assertEquals(InvalidUsername, actual.leftOrNull())
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