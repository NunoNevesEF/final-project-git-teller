package pt.isel.repository.jpa.linkedAccount

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.jpa.RepoJpaAdapter

@Repository
interface OAuthLinkedAccountRepoJpa: JpaRepository<OAuthLinkedAccountEntity, Int>{
    fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccountEntity?
    fun findByUserId(userId: Int): List<OAuthLinkedAccountEntity>
    fun findByUserIdAndProvider(userId: Int, provider: OAuthAccountProvider): List<OAuthLinkedAccountEntity>
    fun findByUserIdAndProviderAndProviderId(userId: Int, provider: OAuthAccountProvider, providerId: String): OAuthLinkedAccountEntity?
    fun findByUserIdAndProviderIn(userId: Int, providers: List<OAuthAccountProvider>): List<OAuthLinkedAccountEntity>
}

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class OAuthLinkedAccountRepoAdapter(
    jpa: OAuthLinkedAccountRepoJpa
) : RepoJpaAdapter<OAuthLinkedAccountEntity, OAuthLinkedAccountRepoJpa>(jpa), IOAuthLinkedAccountRepository{
    override fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccountEntity? =
        jpa.findByIdAndUserId(id, userId)

    override fun findByUserAndProvider(userId: Int, provider: OAuthAccountProvider): List<OAuthLinkedAccountEntity> =
        jpa.findByUserIdAndProvider(userId, provider)

    override fun findByUserAndProviderAndProviderId(userId: Int, provider: OAuthAccountProvider, providerId: String): OAuthLinkedAccountEntity? =
        jpa.findByUserIdAndProviderAndProviderId(userId, provider, providerId)

    override fun findGitAccounts(userId: Int): List<OAuthLinkedAccountEntity> =
        jpa.findByUserIdAndProviderIn(userId, OAuthAccountProvider.gitAccounts)
}