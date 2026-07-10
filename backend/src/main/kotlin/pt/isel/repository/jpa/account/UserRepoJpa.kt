package pt.isel.repository.jpa.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.jpa.RepoJpaAdapter

/**
 *  `UserRepositoryJpa`
 *
 * Interface that extends the [JpaRepository] with [User] entity specific operations.
 * The implementation of these is automatically resolved by JPA.
 * */
@Repository
interface UserRepoJpa : JpaRepository<User, Int> {
    /** @param [email] The email used in the registration of the [User]
     * @return The [User] which the [email] corresponds to or null if not found.
     * **/
    fun findByEmail(email: String): User?
    /** @param [username] The [User]'s username
     * @return The [User] which the [username] corresponds to or null if not found.
     * **/
    fun findByUsername(username: String): User?
}

/**
 *  `UserRepoJpaAdapter`
 *
 * Class that implements the jpa operations of the [User] entity, also extends CRUD implementation of [RepoJpaAdapter].
 * */
@Repository
@ConditionalOnProperty( //defines that this repository is to be used when application is utilizing is jpa implementation.
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class UserRepoJpaAdapter(
    jpa: UserRepoJpa
) : RepoJpaAdapter<User, UserRepoJpa>(jpa), IUserRepository{
    /** @param [email] The email used in the registration of the [User]
     * @return The [User] which the [email] corresponds to or null if not found.
     * **/
    override fun findByEmail(email: String): User? =
        jpa.findByEmail(email)

    /** @param [username] The [User]'s username
     * @return The [User] which the [username] corresponds to or null if not found.
     * **/
    override fun findByUsername(username: String): User? =
        jpa.findByUsername(username)
}