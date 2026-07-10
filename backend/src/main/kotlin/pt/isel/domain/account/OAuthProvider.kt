package pt.isel.domain.account

import pt.isel.entity.account.User
import pt.isel.entity.account.LinkedAccount

/**
 *  `LinkedAccountDomainError`
 *
 * Represents exceptions that can occur while constructing a [OAuthAccountProviderException] object.
 *
 * These exceptions are thrown by the domain layer and can be translated into
 * service error objects by the service layer.
 * */
sealed class OAuthAccountProviderException : RuntimeException()
/** Invalid or unsupported provider **/
class InvalidOAuthProviderException : OAuthAccountProviderException()

/**
 *  `OAuthProvider`
 *
 * Represents one of the providers supported by the application
 *
 * @property [providerName] the identification of the provider within the application.
 * @property [maxPerUser] the max number of [LinkedAccount] that a [User] can link for a given provider.
 * */
enum class OAuthProvider(val providerName: String, val maxPerUser: Int?) {
    GOOGLE("google", 1),
    GITHUB("github", null),
    GITLAB("gitlab", null);

    companion object {
        private val providers = entries.associateBy { it.providerName }

        /**
         * @param providerName string being mapped to the respective [OAuthProvider].
         * @return the [OAuthProvider] mapped from the [providerName]
         */
        fun fromString(providerName: String): OAuthProvider =
            providers[providerName.lowercase()] ?: throw InvalidOAuthProviderException()

        val gitAccounts = setOf(GITHUB, GITLAB)
    }
}