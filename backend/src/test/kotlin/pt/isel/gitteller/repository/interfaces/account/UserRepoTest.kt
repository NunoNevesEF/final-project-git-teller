package pt.isel.gitteller.repository.interfaces.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import pt.isel.entity.account.model.Role
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.RepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import kotlin.test.Test

abstract class UserRepoTest: RepoTest<User> {
    val validEmail = "test@email.com"
    val validUsername = "test"
    val validRole = Role.USER

    abstract override fun repo(): IUserRepository

    fun createUser(
        email: String = validEmail,
        username: String = validUsername,
        role: Role = validRole
    ) = User(0, email, username, role)

    override fun createEntity(): User = createUser()

    override fun updateEntity(entity: User): User =
        entity.copy(username = "some other username")

    override fun assertEquality(expected: User, actual: User?) {
        assertEquals(expected, actual)
    }

    @Test
    fun `method findByEmail returns correct user`() {
        val testEmail = "some email"

        val repo = repo()
        val expected = createUser(email = testEmail)
        val anotherUser = createUser(email = "some other email", username = validUsername + 1)

        repo.create(expected)
        repo.create(anotherUser)

        val actual = repo.findByEmail(testEmail)

        assertEquals(expected, actual)
    }

    @Test
    fun `method findByEmail returns null if email not found`() {
        val repo = repo()

        val actual = repo.findByEmail("some email")

        assertNull(actual)
    }

    @Test
    fun `method findByUsername returns correct user`() {
        val testUsername = "some username"

        val repo = repo()
        val expected = createUser(username = testUsername)
        val anotherUser = createUser(username = "some other username", email = validEmail + 1)

        repo.create(expected)
        repo.create(anotherUser)

        val actual = repo.findByUsername(testUsername)
        assertEquals(expected, actual)
    }

    @Test
    fun `method findByUsername returns null if username not found`() {
        val repo = repo()

        val actual = repo.findByUsername("some username")

        assertNull(actual)
    }
}