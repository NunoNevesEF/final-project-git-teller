package pt.isel.repository.interfaces.account

import pt.isel.entity.account.User
import pt.isel.repository.interfaces.IRepository

/**
 *  `IUserRepository`
 *
 * Interface that establishes the actions upon the [User] entity, extends the CRUD operations of [IRepository]
 * */
interface IUserRepository : IRepository<User> {
    /** @param [email] The email used in the registration of the [User]
     * @return The [User] which the [email] corresponds to or null if not found.
     * **/
    fun findByEmail(email: String): User?

    /** @param [username] The [User]'s username
     * @return The [User] which the [username] corresponds to or null if not found.
     * **/
    fun findByUsername(username: String): User?
}