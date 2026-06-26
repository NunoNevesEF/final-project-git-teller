package pt.isel.gitteller.repo.memory.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.entity.User
import pt.isel.gitteller.repo.memory.RepoMemTest
import pt.isel.repository.memory.RepoMem
import pt.isel.repository.memory.account.linkedAccount.FormLinkedAccountRepoMem
import kotlin.test.Test

class FormLinkedAccountRepoMemTest: RepoMemTest<FormLinkedAccountEntity> {
    private lateinit var repo: FormLinkedAccountRepoMem

    @BeforeEach
    fun setup(){ repo = FormLinkedAccountRepoMem() }

    val validUserId = 0
    val validUser = User(validUserId, "test@email.com", "test")

    private fun newFormLinkedAccount(
        id: Int = 0, user: User = validUser,
        passwordHash: String = "testPasswordHash"
    ) = FormLinkedAccountEntity(id, user, passwordHash)

    @Test
    fun `method findByUserId returns FormLinkedAccount associated with user`(){
        val account = repo.create(newFormLinkedAccount(user = validUser))
        val actual = repo.findByUserId(account.id)
        assertEquals(account, actual)
    }

    @Test
    fun `method findByUserId returns nulls if no form account found`(){
        val actual = repo.findByUserId(validUserId)
        assertNull(actual)
    }

    override fun createRepo(): RepoMem<FormLinkedAccountEntity> = FormLinkedAccountRepoMem()

    override fun createEntity(): FormLinkedAccountEntity = newFormLinkedAccount()

    override fun updateEntity(entity: FormLinkedAccountEntity): FormLinkedAccountEntity {
        entity.passwordHash = "anotherPasswordHash"
        return entity
    }
}