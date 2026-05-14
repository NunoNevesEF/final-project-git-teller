package pt.isel.gitteller.domain.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class LinkedAccountTest<T: LinkedAccount>{
    val validId = 0
    val validUserId = 0

    abstract fun createLinkedAccount(id: Int = validId, userId: Int = validUserId): T

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

class FormLinkedAccountTest: LinkedAccountTest<FormLinkedAccount>(){
    val validPasswordHash = "testPasswordHash"

    override fun createLinkedAccount(id: Int, userId: Int) =
        FormLinkedAccount(id, userId, validPasswordHash)

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = FormLinkedAccount.create(userId = validUserId, passwordHash = validPasswordHash)
        val expected = FormLinkedAccount(0, validUserId, validPasswordHash)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails if passwordHash is blank`(passwordHash: String) {
        assertFailsWith<IllegalArgumentException>{
            FormLinkedAccount.create(validId, validUserId, passwordHash)
        }
    }

    @Test
    fun `creations succeeds if password is not blank`(){
        assertDoesNotThrow{ FormLinkedAccount.create(validId, validUserId, validPasswordHash) }
    }

    @Test
    fun `method getType returns form`(){
        val testLinkedAccount = createLinkedAccount()

        val expected = AccountType.FORM
        val actual = testLinkedAccount.getType()

        assertEquals(expected, actual)
    }

    @Test
    fun `method uniqueKey returns null`(){
        val testLinkedAccount = createLinkedAccount()
        val actual = testLinkedAccount.uniqueKey()
        assertNull(actual)
    }
}

class OAuthLinkedAccountTest: LinkedAccountTest<OAuthLinkedAccount>() {
    val validProvider = AccountType.GOOGLE.type
    val validProviderId = "1"

    private fun mockAccessToken(value: String) =
        OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, value,
            Instant.now(), Instant.now().plusSeconds(3600),
        )

    private fun mockRefreshToken(value: String) =
        OAuth2RefreshToken(value, Instant.now())

    override fun createLinkedAccount(id: Int, userId: Int): OAuthLinkedAccount =
        OAuthLinkedAccount.create(id, userId, provider = validProvider, providerId = validProviderId)

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails if providerId is blank`(providerId: String){
        assertFailsWith<IllegalArgumentException> {
            OAuthLinkedAccount.create(userId = validUserId, provider = validProvider, providerId = providerId)
        }
    }

    @Test
    fun `companion method create defaults parameters if not passed`() {
        val actual = createLinkedAccount(validId, validUserId)

        assertEquals(0, actual.id)
        assertNull(actual.accessToken)
        assertNull(actual.refreshToken)
    }

    @Test
    fun `companion method create fails if provider string doesn't match any supported accountType`() {
        assertFailsWith<IllegalArgumentException> {
            OAuthLinkedAccount.create(validId, validUserId, provider = "unknown", providerId = validProviderId)
        }
    }


    @Test
    fun `method getType returns provider`() {
        val testLinkedAccount = createLinkedAccount()

        val expected = testLinkedAccount.provider
        val actual = testLinkedAccount.getType()

        assertEquals(expected, actual)
    }

    @Test
    fun `method uniqueKey returns providerId`() {
        val testLinkedAccount = createLinkedAccount()

        val expected = testLinkedAccount.providerId
        val actual = testLinkedAccount.uniqueKey()

        assertEquals(expected, actual)
    }
}