package pt.isel.repository

import pt.isel.domain.account.User

interface IUserRepository : IRepository<User> {
    fun read(email: String): User?
    fun readOrCreateByEmail(entity: User): User
}