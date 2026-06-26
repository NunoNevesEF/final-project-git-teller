package pt.isel.repository.jpa.linkedAccount

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.jpa.RepoJpaAdapter

@Repository
interface FormLinkedAccountRepoJpa : JpaRepository<FormLinkedAccountEntity, Int> {
    fun findByUserId(userId: Int): FormLinkedAccountEntity?
}

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class FormLinkedAccountRepoAdapter(
    jpa: FormLinkedAccountRepoJpa
) : RepoJpaAdapter<FormLinkedAccountEntity, FormLinkedAccountRepoJpa>(jpa), IFormLinkedAccountRepository {
    override fun findByUserId(userId: Int): FormLinkedAccountEntity? =
        jpa.findByUserId(userId)
}