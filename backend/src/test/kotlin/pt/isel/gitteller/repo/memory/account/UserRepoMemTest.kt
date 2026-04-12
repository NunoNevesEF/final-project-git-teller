package pt.isel.gitteller.repo.memory.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.domain.account.User
import pt.isel.repository.memory.account.UserRepoMem
import kotlin.test.Test

class UserRepoMemTest{
    private lateinit var repo: UserRepoMem

    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(id, email, userName)


    @BeforeEach
    fun setup(){ repo = UserRepoMem() }

    @Test
    fun `method create returns User with repo assigned id`(){
        val testUser = newUser(id = 333)
        val expectedId = repo.currId()

        val actual = repo.create(testUser)
        val expected = testUser.copy(id = expectedId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method create assigned Id increments after call`(){
        val oldId = repo.currId()
        val created = repo.create(newUser())
        val newId = repo.currId()

        assertEquals(oldId, created.id)
        assertEquals(oldId + 1, newId)
    }

    @Test
    fun `method read returns User by id`(){
        val expected = repo.create(newUser())
        val actual = repo.read(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method read returns Null if id not found`(){
        val actual = repo.read(333)
        assertNull(actual)
    }

    @Test
    fun `method read returns User by email`(){
        val expected = repo.create(newUser())
        val actual = repo.read(expected.email)
        assertEquals(expected, actual)
    }

    @Test
    fun `method read returns Null if email not found`(){
        val actual = repo.read("invalidEmail333")
        assertNull(actual)
    }

    @Test
    fun `method readOrCreateByEmail returns User If email is found`(){
        val expected = repo.create(newUser())
        val actual = repo.readOrCreateByEmail(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readOrCreateByEmail creates User If email is not found`(){
        val testUser = newUser()
        val confirmNotFound = repo.read(testUser.email)

        val expected = repo.readOrCreateByEmail(testUser)
        val confirmFound = repo.read(testUser.email)

        assertNull(confirmNotFound)
        assertEquals(expected, confirmFound)
    }

    @Test
    fun `method update returns updated User`(){
        val testUser = repo.create(newUser())
        val expected = testUser.copy(userName = "UpdatedUsername")
        val actual = repo.update(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun `method update returns null if User not found`(){
        val actual = repo.update(newUser())
        assertNull(actual)
    }

    @Test
    fun `method update updates user in memory`(){
        val testUser = repo.create(newUser())
        val updatedUser = testUser.copy(userName = "UpdatedUsername")
        val expected = repo.update(updatedUser)
        val actual = repo.read(updatedUser.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete returns deleted User`(){
        val expected = repo.create(newUser())
        val actual = repo.delete(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete returns null if User not found`(){
        val actual = repo.delete(newUser().id)
        assertNull(actual)
    }

    @Test
    fun `method delete deletes user in memory`(){
        val testUser = repo.create(newUser())
        repo.delete(testUser.id)
        val actual = repo.read(testUser.id)
        assertNull(actual)
    }

    @Test
    fun `method nextId returns curr id and increments it`(){
        val expectedCurr = repo.currId()
        val actualCurr = repo.nextId()
        val expectedNext = expectedCurr + 1
        val actualNext = repo.currId()

        assertEquals(expectedCurr, actualCurr)
        assertEquals(expectedNext, actualNext)
    }
}