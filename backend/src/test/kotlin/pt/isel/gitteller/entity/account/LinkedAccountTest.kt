package pt.isel.gitteller.entity.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.domain.account.OAuthProvider
import pt.isel.domain.account.InvalidOAuthProviderException
import pt.isel.entity.account.BlankProviderIdException
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.LinkedAccount
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class LinkedAccountTest{
    val validId = 0

    val validEmail = "test@email.com"
    val validUsername = "test"
    val validUser = User(validId + 1, validEmail, validUsername)

    abstract fun createLinkedAccount(user: User): LinkedAccount

    @Test
    fun `creation properly assigns user`(){
        val expected = validUser
        val actual = createLinkedAccount(expected).user
        assertEquals(expected, actual)
    }
}

class FormLinkedAccountTest: LinkedAccountTest(){
    val validPasswordHash = "testPasswordHash"

    @Test
    fun `constructor defaults id to 0 if not passed`(){
        val expected = FormLinkedAccount(0, validUser, validPasswordHash)
        val actual = FormLinkedAccount(user = validUser, passwordHash = validPasswordHash)

        assertEquals(expected.id, actual.id)
    }

    override fun createLinkedAccount(user: User): LinkedAccount =
        FormLinkedAccount(user = user, passwordHash = validPasswordHash)
}

class OAuthLinkedAccountTest: LinkedAccountTest() {
    val validProvider = OAuthProvider.GOOGLE
    val validProviderId = "someId"

    @Test
    fun `companion method create defaults parameters if not passed`() {
        val actual = OAuthLinkedAccount(user = validUser, providerName = validProvider.providerName, providerId = validProviderId)

        assertEquals(0, actual.id)
        assertEquals("", actual.accessToken)
        assertEquals("", actual.refreshToken)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails with BlankProviderIdException if providerId is blank`(providerId: String){
        assertFailsWith<BlankProviderIdException> {
            OAuthLinkedAccount(user = validUser, providerName = validProvider.providerName, providerId = providerId)
        }
    }

    @Test
    fun `secondary constructor fails with UnknownProviderException if provider string doesn't match any supported accountType`() {
        assertFailsWith<InvalidOAuthProviderException> {
            OAuthLinkedAccount(user = validUser, providerName = "unknown", providerId = validProviderId)
        }
    }

    @Test
    fun `method copy returns object with same parameters if none passed`(){
        val expected = OAuthLinkedAccount(
            id = validId,
            user = validUser,
            providerId = validProviderId,
            provider = validProvider,
            accessToken = "some token",
            refreshToken = "some token",
        )
        val actual = expected.copy()

        assertEquals(expected.id, actual.id)
        assertEquals(expected.user, actual.user)
        assertEquals(expected.providerId, actual.providerId)
        assertEquals(expected.provider, actual.provider)
        assertEquals(expected.accessToken, actual.accessToken)
        assertEquals(expected.refreshToken, actual.refreshToken)
    }

    @Test
    fun `method copy returns object with changed parameters if passed`(){
        val expected = OAuthLinkedAccount(
            id = validId,
            user = validUser,
            providerId = validProviderId,
            provider = validProvider,
            accessToken = "some token",
            refreshToken = "some token",
        )
        val newAccessToken = "some other token"

        val actual = expected.copy(accessToken = newAccessToken)

        assertEquals(expected.id, actual.id)
        assertEquals(expected.user, actual.user)
        assertEquals(expected.providerId, actual.providerId)
        assertEquals(expected.provider, actual.provider)
        assertEquals(newAccessToken, actual.accessToken)
        assertEquals(expected.refreshToken, actual.refreshToken)
    }

    override fun createLinkedAccount(user: User): LinkedAccount
        = OAuthLinkedAccount(user = user, providerName = validProvider.providerName, providerId = validProviderId)
}