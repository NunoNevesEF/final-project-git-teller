package pt.isel.repository.memory.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.memory.RepoMem

/**
 *  `FormLinkedAccountRepoMem`
 *
 * Class that implements the in-memory operations of the [FormLinkedAccount] entity, also extends CRUD implementation of [RepoMem].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class FormLinkedAccountRepoMem(): RepoMem<FormLinkedAccount>(), IFormLinkedAccountRepository{
    /** @param [userId] The identifier of the [User] this account is linked to
     * @return The [FormLinkedAccount] which the [userId] corresponds to or null if not found.
     * **/
    override fun findByUserId(userId: Int): FormLinkedAccount? =
        persistence.values.firstOrNull { it.user.id == userId }
}