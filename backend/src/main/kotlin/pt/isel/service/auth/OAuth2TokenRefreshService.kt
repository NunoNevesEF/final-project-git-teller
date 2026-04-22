package pt.isel.service.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.LinkedAccountServiceError
import pt.isel.utils.Either
import pt.isel.utils.isFailure
import pt.isel.utils.rightOrNull

@Service
class OAuth2TokenRefreshService(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val linkedAccountService: LinkedAccountService,
) {
    fun refreshAccessToken(userId: Int, provider: AccountType): LinkedAccount? {
        val account = linkedAccountService.readByUserAndType(userId, provider)
        if(account.isFailure()) { return null } //TODO: THINK ABOUT BEHAVIOR ON EARLY RETURN

        val oauthAccount = account.rightOrNull() as OAuthLinkedAccount

        val clientRegistration = clientRegistrationRepository
            .findByRegistrationId(provider.type)

        if(oauthAccount.refreshToken == null) { return null } //TODO: THINK ABOUT BEHAVIOR ON EARLY RETURN

        val refreshRequest = OAuth2RefreshTokenGrantRequest(
            clientRegistration,
            oauthAccount.accessToken,
            oauthAccount.refreshToken
        )

        val response = RestClientRefreshTokenTokenResponseClient().getTokenResponse(refreshRequest)

        return linkedAccountService.update(
            userId, provider,
            accessToken = response.accessToken, refreshToken = response.refreshToken
        ).rightOrNull()
    }
}

//TODO: TEST THIS FUNCTION WHEN GOOGLE REFRESH TOKEN AND GITHUB REFRESH TOKEN OBTAINED.
//For google - need to look at scopes.
//For github - move from oauth app to github app