package pt.isel.gitteller.repo.memory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.domain.User
import pt.isel.domain.UserAuthentication
import pt.isel.domain.UserData
import pt.isel.repository.memory.UserRepoMem
import kotlin.test.Test

class UserRepoMemTest{
    private lateinit var repo: UserRepoMem

    private fun newUser(id: Int = 0, email: String = "test@email.com", userName: String = "test") =
        User(
            UserData(id, email, userName),
            listOf(UserAuthentication.FormAuthentication("test_pass"))
        )

    @BeforeEach
    fun setup(){ repo = UserRepoMem() }

    @Test
    fun `method create returns User with repo assigned id`(){
        val testUser = newUser(id = 333)
        val expectedId = repo.currId()

        val actual = repo.create(testUser)
        val expected = testUser.copy(data = testUser.data.copy(id = expectedId))

        assertEquals(expected, actual)
    }

    @Test
    fun `method create assigned Id increments after call`(){
        val oldId = repo.currId()
        val created = repo.create(newUser())
        val newId = repo.currId()

        assertEquals(oldId, created.data.id)
        assertEquals(oldId+1, newId)
    }

    @Test
    fun `method read returns User by id`(){
        val expected = repo.create(newUser())
        val actual = repo.read(expected.data.id)
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
        val actual = repo.read(expected.data.email)
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
        val confirmNotFound = repo.read(testUser.data.email)

        val expected = repo.readOrCreateByEmail(testUser)
        val confirmFound = repo.read(testUser.data.email)

        assertNull(confirmNotFound)
        assertEquals(expected, confirmFound)
    }

    @Test
    fun `method update returns updated User`(){
        val testUser = repo.create(newUser())
        val expected = testUser.copy(data = testUser.data.copy(userName = "UpdatedUsername"))
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
        val updatedUser = testUser.copy(data = testUser.data.copy(userName = "UpdatedUsername"))
        val expected = repo.update(updatedUser)
        val actual = repo.read(updatedUser.data.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete returns deleted User`(){
        val expected = repo.create(newUser())
        val actual = repo.delete(expected.data.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete returns null if User not found`(){
        val actual = repo.delete(newUser().data.id)
        assertNull(actual)
    }

    @Test
    fun `method delete deletes user in memory`(){
        val testUser = repo.create(newUser())
        repo.delete(testUser.data.id)
        val actual = repo.read(testUser.data.id)
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