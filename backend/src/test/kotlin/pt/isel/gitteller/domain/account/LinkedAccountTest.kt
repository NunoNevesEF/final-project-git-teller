package pt.isel.gitteller.domain.account

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.entity.LinkedAccountEntity
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.entity.User
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class LinkedAccountTest<
        DOMAIN: LinkedAccount<DOMAIN, ENTITY>,
        ENTITY: LinkedAccountEntity<ENTITY, DOMAIN>
>{
    val validId = 0
    val validUserId = 0

    val validEmail = "test@email.com"
    val validUserName = "test"
    val validUser = User(validId, validEmail, validUserName)

    abstract fun createLinkedAccount(id: Int = validId, userId: Int = validUserId): DOMAIN
    abstract fun assertToEntity(original: DOMAIN, result: ENTITY)

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
    fun `method toEntity properly creates Entity`(){
        val original = createLinkedAccount()
        val result = original.toEntity(validUser)
        assertToEntity(original, result)
    }
}

class FormLinkedAccountTest: LinkedAccountTest<FormLinkedAccount, FormLinkedAccountEntity>(){
    val validPasswordHash = "testPasswordHash"

    override fun createLinkedAccount(id: Int, userId: Int) =
        FormLinkedAccount(id, userId, validPasswordHash)

    override fun assertToEntity(
        original: FormLinkedAccount,
        result: FormLinkedAccountEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.userId, result.id)
        assertEquals(original.passwordHash, result.passwordHash)
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = FormLinkedAccount.create(userId = validUserId, passwordHash = validPasswordHash)
        val expected = FormLinkedAccount(0, validUserId, validPasswordHash)
        assertEquals(expected, actual)
    }
}

class OAuthLinkedAccountTest: LinkedAccountTest<OAuthLinkedAccount, OAuthLinkedAccountEntity>() {
    val validProvider = OAuthAccountProvider.GOOGLE.type
    val validProviderId = "1"

    override fun createLinkedAccount(id: Int, userId: Int): OAuthLinkedAccount =
        OAuthLinkedAccount.create(id, userId, provider = validProvider, providerId = validProviderId)

    override fun assertToEntity(
        original: OAuthLinkedAccount,
        result: OAuthLinkedAccountEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.userId, result.id)
        assertEquals(original.accessToken, result.accessToken)
        assertEquals(original.refreshToken, result.refreshToken)
        assertEquals(original.provider, result.provider)
        assertEquals(original.providerId, result.providerId)
    }

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
        assertEquals("", actual.accessToken)
        assertEquals("", actual.refreshToken)
    }

    @Test
    fun `companion method create fails if provider string doesn't match any supported accountType`() {
        assertFailsWith<IllegalArgumentException> {
            OAuthLinkedAccount.create(validId, validUserId, provider = "unknown", providerId = validProviderId)
        }
    }
}