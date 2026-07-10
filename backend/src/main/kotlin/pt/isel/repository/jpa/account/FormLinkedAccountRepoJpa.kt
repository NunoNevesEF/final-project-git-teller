package pt.isel.repository.jpa.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.jpa.RepoJpaAdapter

/**
 *  `FormLinkedAccountRepoJpa`
 *
 * Interface that extends the [JpaRepository] with [FormLinkedAccount] entity specific operations.
 * The implementation of these is automatically resolved by JPA.
 * */
@Repository
interface FormLinkedAccountRepoJpa : JpaRepository<FormLinkedAccount, Int> {
    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The [FormLinkedAccount] which the [userId] corresponds to or null if not found.
     * **/
    fun findByUserId(userId: Int): FormLinkedAccount?
}

/**
 *  `FormLinkedAccountRepoJpaAdapter`
 *
 * Class that implements the jpa operations of the [FormLinkedAccount] entity, also extends CRUD implementation of [RepoJpaAdapter].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class FormLinkedAccountRepoJpaAdapter(
    jpa: FormLinkedAccountRepoJpa
) : RepoJpaAdapter<FormLinkedAccount, FormLinkedAccountRepoJpa>(jpa), IFormLinkedAccountRepository {
    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The [FormLinkedAccount] which the [userId] corresponds to or null if not found.
     * **/
    override fun findByUserId(userId: Int): FormLinkedAccount? =
        jpa.findByUserId(userId)
}