package pt.isel.repository.memory.account.linkedAccount

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.domain.account.OAuthAccountProvider.Companion.gitAccounts
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.memory.RepoMem

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class OAuthLinkedAccountRepoMem : RepoMem<OAuthLinkedAccountEntity>(), IOAuthLinkedAccountRepository {
    override fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccountEntity? =
        persistence.values.firstOrNull{ it.id == id && it.user.id == userId }

    override fun findByUserAndProvider(userId: Int, provider: OAuthAccountProvider): List<OAuthLinkedAccountEntity> =
        persistence.values.filter{ it.user.id == userId && it.provider == provider }

    override fun findByUserAndProviderAndProviderId(userId: Int, provider: OAuthAccountProvider, providerId: String): OAuthLinkedAccountEntity? =
        persistence.values.firstOrNull{ it.user.id == userId && it.provider == provider && it.providerId == providerId }

    override fun findGitAccounts(userId: Int) : List<OAuthLinkedAccountEntity> =
        persistence.values.filter{ it.user.id == userId && it.provider in gitAccounts }
}