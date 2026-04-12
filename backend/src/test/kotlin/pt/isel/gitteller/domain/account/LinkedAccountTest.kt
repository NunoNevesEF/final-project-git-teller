package pt.isel.gitteller.domain.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class LinkedAccountTest<T: LinkedAccount>{
    abstract fun createLinkedAccount(id: Int = 0, userId: Int = 0): T
    val validUserId = 0

    @Test
    fun `creation fails if id less than 0`(){
        assertFailsWith<IllegalArgumentException> { createLinkedAccount(id = -1) }
    }

    @Test
    fun `creation succeeds if id is 0`(){ assertDoesNotThrow { createLinkedAccount(id = 0) } }

    @Test
    fun `creation fails if userId less than 0`(){
        assertFailsWith<IllegalArgumentException> { createLinkedAccount(userId = -1) }
    }

    @Test
    fun `creation succeeds if userId is 0`(){
        assertDoesNotThrow { createLinkedAccount(userId = 0) }
    }

    @Test
    fun `method accountCopy returns LinkedAccount with updated id`(){
        val testLinkedAccount = createLinkedAccount()
        val updatedId = testLinkedAccount.id + 1
        val actual = testLinkedAccount.accountCopy(id = updatedId)

        assertNotEquals(testLinkedAccount, actual)
        assertEquals(updatedId, actual.id)
        assertEquals(testLinkedAccount.userId, actual.userId)
    }
}

class OAuthLinkedAccountTest: LinkedAccountTest<OAuthLinkedAccount>(){
    val validProvider = "testProvider"

    override fun createLinkedAccount(id: Int, userId: Int): OAuthLinkedAccount =
        OAuthLinkedAccount(id, userId, validProvider)

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails if provider is blank`(provider: String){
        assertFailsWith<IllegalArgumentException> {
            OAuthLinkedAccount.create(userId = validUserId, provider = provider)
        }
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = OAuthLinkedAccount.create(userId = validUserId, provider = validProvider)
        val expected = OAuthLinkedAccount(0, validUserId, validProvider)
        assertEquals(expected, actual)
    }

    @Test
    fun `method getType returns provider`(){
        val testLinkedAccount = createLinkedAccount()
        val expected = testLinkedAccount.provider
        val actual = testLinkedAccount.getType()
        assertEquals(expected, actual)
    }
}

class FormLinkedAccountTest: LinkedAccountTest<FormLinkedAccount>(){
    val validPassword = "testPassword"
    override fun createLinkedAccount(id: Int, userId: Int): FormLinkedAccount =
        FormLinkedAccount(id, userId, validPassword)

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails if password is blank`(password: String){
        assertFailsWith<IllegalArgumentException> {
            FormLinkedAccount.create(userId = validUserId, passwordHash = password)
        }
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = FormLinkedAccount.create(userId = validUserId, passwordHash = validPassword)
        val expected = FormLinkedAccount(0, validUserId, validPassword)
        assertEquals(expected, actual)
    }

    @Test
    fun `method getType returns form`(){
        val testLinkedAccount = createLinkedAccount()
        val expected = "form"
        val actual = testLinkedAccount.getType()
        assertEquals(expected, actual)
    }
}