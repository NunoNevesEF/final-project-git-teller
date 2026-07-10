package pt.isel.repository.memory.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.memory.RepoMem

/**
 *  `UserRepoMem`
 *
 * Class that implements the in-memory operations of the [User] entity, also extends CRUD implementation of [RepoMem].
 * */
@Repository
@ConditionalOnProperty( //defines that this repository is to be used when application is utilizing is in-memory implementation.
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class UserRepoMem(): RepoMem<User>(), IUserRepository {
    /** @param [email] The email used in the registration of the [User]
     * @return The [User] which the [email] corresponds to or null if not found.
     * **/
    override fun findByEmail(email: String): User? {
        return persistence.values.firstOrNull { it.email == email }
    }
    /** @param [username] The [User]'s username
     * @return The [User] which the [username] corresponds to or null if not found.
     * **/
    override fun findByUsername(username: String): User? {
        return persistence.values.firstOrNull { it.username == username }
    }
}