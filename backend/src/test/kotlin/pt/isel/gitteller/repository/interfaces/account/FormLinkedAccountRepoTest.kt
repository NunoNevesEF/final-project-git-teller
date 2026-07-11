package pt.isel.gitteller.repository.interfaces.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.RepoTest
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IUserRepository
import kotlin.test.Test

abstract class FormLinkedAccountRepoTest: RepoTest<FormLinkedAccount> {
    val validPasswordHash = "test"

    val validEmail = "test@email.com"
    val validUsername = "test"
    val validUser = User(0, validEmail, validUsername)


    abstract override fun repo(): IFormLinkedAccountRepository
    abstract fun userRepo(): IUserRepository

    fun createFormLinkedAccount(
        user: User,
        passwordHash: String = validPasswordHash,
    ): FormLinkedAccount = FormLinkedAccount(0, user, passwordHash)

    override fun createEntity(): FormLinkedAccount{
        val user = userRepo().create(validUser)

        return createFormLinkedAccount(user = user)
    }

    override fun updateEntity(entity: FormLinkedAccount): FormLinkedAccount =
        entity.copy(passwordHash = entity.passwordHash)

    override fun assertEquality(expected: FormLinkedAccount, actual: FormLinkedAccount?) {
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.user.id, actual?.user?.id)
        assertEquals(expected.passwordHash, actual?.passwordHash)
    }

    @Test
    fun `method findByUserId returns correct FormLinkedAccount`() {
        val repo = repo()
        val userRepo = userRepo()

        val user1 = userRepo.create(validUser)
        val user2 = userRepo.create(User(email = "some other email", username = "some other username"))

        val expected = createFormLinkedAccount(user = user1)
        val anotherUser = createFormLinkedAccount(user = user2)

        repo.create(expected)
        repo.create(anotherUser)

        val actual = repo.findByUserId(expected.user.id)

        assertEquality(expected, actual)
    }

    @Test
    fun `method findByUserId returns null if userId not found`() {
        val repo = repo()

        val actual = repo.findByUserId(1)

        assertNull(actual)
    }
}