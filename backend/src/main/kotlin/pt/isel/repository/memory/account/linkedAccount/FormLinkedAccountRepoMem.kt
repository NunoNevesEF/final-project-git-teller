package pt.isel.repository.memory.account.linkedAccount

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.memory.RepoMem

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class FormLinkedAccountRepoMem(): RepoMem<FormLinkedAccountEntity>(), IFormLinkedAccountRepository{
    override fun findByUserId(userId: Int): FormLinkedAccountEntity? =
        persistence.values.firstOrNull { it.user.id == userId }
}