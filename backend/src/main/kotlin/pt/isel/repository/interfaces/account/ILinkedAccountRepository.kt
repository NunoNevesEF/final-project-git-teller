package pt.isel.repository.interfaces.account

import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.repository.interfaces.IRepository

interface IFormLinkedAccountRepository: IRepository<FormLinkedAccountEntity>{
    fun findByUserId(userId: Int): FormLinkedAccountEntity?
}

interface IOAuthLinkedAccountRepository : IRepository<OAuthLinkedAccountEntity>{
    fun findByIdAndUserId(id: Int, userId: Int): OAuthLinkedAccountEntity?
    fun findByUserAndProvider(userId: Int, provider: OAuthAccountProvider): List<OAuthLinkedAccountEntity>
    fun findByUserAndProviderAndProviderId(userId: Int, provider: OAuthAccountProvider, providerId: String): OAuthLinkedAccountEntity?
    fun findGitAccounts(userId: Int): List<OAuthLinkedAccountEntity>
}