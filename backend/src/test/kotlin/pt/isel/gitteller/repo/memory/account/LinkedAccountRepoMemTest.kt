package pt.isel.gitteller.repo.memory.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.memory.account.LinkedAccountRepoMem
import kotlin.test.Test

class LinkedAccountRepoMemTest {
    private lateinit var repo: LinkedAccountRepoMem
    val validUserId = 0
    val validProvider = AccountType.GOOGLE
    val validProviderId = "testProviderId"

    private fun newOAuthLinkedAccount(
        id: Int = 0, userId: Int = validUserId,
        provider : AccountType = validProvider, providerId : String = validProviderId
    ) = OAuthLinkedAccount.create(id, userId, provider = provider.type, providerId = providerId)

    private fun newFormLinkedAccount(
        id: Int = 0, userId: Int = validUserId,
        passwordHash: String = "testPasswordHash"
    ) =
        FormLinkedAccount.create(id, userId, passwordHash)

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
        val gitAccount1 = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GITHUB))
        val gitAccount2 = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GITHUB, providerId = "2"))
        val googleAccount = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GOOGLE))
        val accountForm = repo.create(newFormLinkedAccount(userId = validUserId))

        val expected = listOf(gitAccount1, gitAccount2, googleAccount, accountForm)
        val actual = repo.readByUser(validUserId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUser returns Null if user accounts not found`(){
        val actual = repo.readByUser(Int.MAX_VALUE)
        assertNull(actual)
    }

    @Test
    fun `method readByUserAndType returns all LinkedAccount associated with user of given type`(){
        val gitAccount1 = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GITHUB))
        val gitAccount2 = repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GITHUB, providerId = "2"))

        val expected = listOf(gitAccount1, gitAccount2)

        repo.create(newOAuthLinkedAccount(userId = validUserId, provider = AccountType.GOOGLE))

        val actual = repo.readByUserAndType(validUserId, expected.first().getType().type)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUserAndType returns Null if user accounts not found`(){
        val actual = repo.readByUserAndType(userId = Int.MAX_VALUE, type = validProvider.type)
        assertNull(actual)
    }

    @Test
    fun `method readByUserAndType returns Null if account type not found in user accounts`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount())
        val actual = repo.readByUserAndType(userId = testLinkedAccount.userId, type = "Unknown Provider")
        assertNull(actual)
    }

    @Test
    fun `method readByUserTypeAndKey returns specific linkedAccount`(){
        val expected = repo.create(newOAuthLinkedAccount(userId = validUserId))
        val actual = repo.readByUserTypeAndKey(expected.userId, type = expected.getType().type, expected.uniqueKey())
        assertEquals(expected, actual)
    }

    @Test
    fun `method readByUserTypeAndKey works for FormAccount (null key)`(){
        val expected = repo.create(newFormLinkedAccount(userId = validUserId))
        val actual = repo.readByUserTypeAndKey(expected.userId, type = expected.getType().type, expected.uniqueKey())
        assertEquals(expected, actual)
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
    fun `method delete remove provider key if all accounts of provider tpe were deleted`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount(userId = validUserId))
        repo.create(newFormLinkedAccount(userId = validUserId))

        repo.delete(testLinkedAccount.id)

        val actual = repo.readByUserAndType(validUserId, type = testLinkedAccount.getType().type)

        assertNull(actual)
        assertNotNull(repo.readByUser(validUserId))
    }

    @Test
    fun `method delete returns null if linked account not found`(){
        val actual = repo.delete(Int.MAX_VALUE)
        assertNull(actual)
    }

    @Test
    fun `method deleteByUserTypeAndKey returns deleted LinkedAccount`(){
        val expected = repo.create(newFormLinkedAccount())
        val actual = repo.deleteByUserTypeAndKey(
            userId = validUserId, type = expected.getType().type, key = expected.uniqueKey()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `method deleteByUserTypeAndKey returns null if user accounts not found`(){
        val actual = repo.deleteByUserTypeAndKey(userId = Int.MAX_VALUE, type = "Different", null)
        assertNull(actual)
    }

    @Test
    fun `method deleteByUserAndType returns null if type not found in user accounts`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount())
        val actual = repo.deleteByUserTypeAndKey(userId = testLinkedAccount.userId, type = "Different", null)
        assertNull(actual)
    }

    @Test
    fun `method deleteByUserAndType returns null if key for type not found in user accounts`(){
        val testLinkedAccount = repo.create(newOAuthLinkedAccount())
        val actual = repo.deleteByUserTypeAndKey(
            userId = testLinkedAccount.userId, type = testLinkedAccount.getType().type, "Some Unknown Key"
        )
        assertNull(actual)
    }
}