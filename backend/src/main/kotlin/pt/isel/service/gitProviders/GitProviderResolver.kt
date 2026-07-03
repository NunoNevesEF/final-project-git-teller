package pt.isel.service.gitProviders

import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthAccountProvider

@Service
class GitProviderResolver(
    services: List<IGitProviderService>
) {
    private val servicesByProvider = services.associateBy { it.provider }

    fun get(provider: OAuthAccountProvider): IGitProviderService? =
        servicesByProvider[provider]
}