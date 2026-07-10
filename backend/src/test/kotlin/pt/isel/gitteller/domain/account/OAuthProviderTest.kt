package pt.isel.gitteller.domain.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.domain.account.InvalidOAuthProviderException
import pt.isel.domain.account.OAuthProvider
import kotlin.test.Test
import kotlin.test.assertFailsWith

class OAuthProviderTest {
    @ParameterizedTest
    @ValueSource(strings = ["google", "github", "gitlab"])
    fun `method fromString returns matching provider`(providerName: String) {
        val provider = OAuthProvider.fromString(providerName)

        assertEquals(providerName, provider.providerName)
    }

    @ParameterizedTest
    @ValueSource(strings = ["GOOGLE", "GitHub", "GiTlAb"])
    fun `method fromString is case insensitive`(providerName: String) {
        val provider = OAuthProvider.fromString(providerName)

        assertEquals(providerName.lowercase(), provider.providerName)
    }

    @Test
    fun `method fromString throws for unsupported provider`() {
        assertFailsWith<InvalidOAuthProviderException> {
            OAuthProvider.fromString("invalid provider")
        }
    }
}