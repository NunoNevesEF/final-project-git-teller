package pt.isel.gitteller.repo.memory.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.memory.account.LinkedAccountRepoMem
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LinkedAccountRepoMemTest {
    private lateinit var repo: LinkedAccountRepoMem
    val validUserId = 0
    val validProvider = "testProvider"

    private fun newOAuthLinkedAccount(id: Int = 0, userId: Int = validUserId, provider : String = validProvider) =
        OAuthLinkedAccount(id, userId, provider)
    private fun newFormLinkedAccount(id: Int = 0, userId: Int = validUserId, passwordHash: String = "testPasswordHash") =
        FormLinkedAccount(id, userId, passwordHash)

    @BeforeEach
    fun setup(){ repo = LinkedAccountRepoMem() }

    @Test
    fun `method create returns LinkedAccount with repo assigned id`(){
        val testLinkedAccount = newOAuthLinkedAccount(id = Int.MAX_VALUE)
        val expectedId = repo.currId()

        val actual = repo.create(testLinkedAccount)
        val expected = testLinkedAccount.accountCopy(id = expectedId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method create succeeds if account type different from existing one`(){
        assertDoesNotThrow {
            repo.create(newOAuthLinkedAccount())
            repo.create(newOAuthLinkedAccount(provider = "Different"))
            repo.create(newFormLinkedAccount())
        }
    }

    @Test
    fun `method create fails if User already has account type`(){
        assertFailsWith<IllegalArgumentException>{
            repo.create(newFormLinkedAccount())
            repo.create(newFormLinkedAccount())
        }
    }

    @Test
    fun `method create assigned Id increments after call`(){
        val oldId = repo.currId()
        val created = repo.create(newFormLinkedAccount())
        val newId = repo.currId()

        assertEquals(oldId, created.id)
        assertEquals(oldId+1, newId)
    }

    @Test
    fun `method read returns LinkedAccount by id`(){
        val expected = repo.create(newFormLinkedAccount())
        val actual = repo.read(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUser returns all LinkedAccounts associated with user`(){
        val accountOAuth1 = repo.create(newOAuthLinkedAccount(userId = validUserId))
        val accountOAuth2 = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = "Different"))
        val accountForm = repo.create(newFormLinkedAccount(userId = validUserId))

        val expected = listOf(accountOAuth1, accountOAuth2, accountForm)
        val actual = repo.readByUser(validUserId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUser returns Null if user accounts not found`(){
        val actual = repo.readByUser(Int.MAX_VALUE)
        assertNull(actual)
    }

    @Test
    fun `method readByUserAndType returns specified LinkedAccount associated with user`(){
        val expected = repo.create(newOAuthLinkedAccount(userId = validUserId))
        repo.create(newOAuthLinkedAccount(userId = validUserId, provider = "Different"))

        val actual = repo.readByUserAndType(validUserId, expected.getType())
        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUserAndType returns Null if user accounts not found`(){
        val actual = repo.readByUserAndType(userId = Int.MAX_VALUE, type = validProvider)
        assertNull(actual)
    }

    @Test
    fun `method readByUserAndType returns Null if account type not found in user accounts`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount())
        val actual = repo.readByUserAndType(userId = testLinkedAccount.userId, type = "Different")
        assertNull(actual)
    }

    @Test
    fun `method update returns the updated LinkedAccount`(){
        val testLinkedAccount = repo.create(newFormLinkedAccount()) as FormLinkedAccount
        val expected = testLinkedAccount.copy(passwordHash = "Different PasswordHash")
        val actual = repo.update(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun `method update returns Null if user accounts not found`(){
        val actual = repo.update(newFormLinkedAccount())
        assertNull(actual)
    }

    @Test
    fun `method update fails if new userId does not match existing userId`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount()) as OAuthLinkedAccount
        assertFailsWith<IllegalArgumentException>{
            repo.update(testLinkedAccount.copy(_userId = testLinkedAccount.userId + 1))
        }
    }

    @Test
    fun `method update fails if new type does not match existing type`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount()) as OAuthLinkedAccount
        assertFailsWith<IllegalArgumentException>{
            repo.update(testLinkedAccount.copy(provider = "Different"))
        }
    }

    @Test
    fun `method delete returns deleted LinkedAccount`(){
        val expected = repo.create(newFormLinkedAccount())
        val actual = repo.delete(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete remove userId key if all accounts were deleted`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount(userId = validUserId))
        repo.delete(testLinkedAccount.id)
        val actual = repo.readByUser(validUserId)
        assertNull(actual)
    }

    @Test
    fun `method delete returns null if linked account not found`(){
        val actual = repo.delete(Int.MAX_VALUE)
        assertNull(actual)
    }

    @Test
    fun `method deleteByUserAndType returns deleted LinkedAccount`(){
        val expected = repo.create(newFormLinkedAccount())
        val actual = repo.deleteByUserAndType(userId = validUserId, type = expected.getType())
        assertEquals(expected, actual)
    }

    @Test
    fun `method deleteByUserAndType returns null if user accounts not found`(){
        val actual = repo.deleteByUserAndType(userId = Int.MAX_VALUE, type = "Different")
        assertNull(actual)
    }

    @Test
    fun `method deleteByUserAndType returns null if type not found in user accounts`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount())
        val actual = repo.deleteByUserAndType(userId = testLinkedAccount.userId, type = "Different")
        assertNull(actual)
    }
}