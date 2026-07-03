package pt.isel.repository.interfaces.account

import pt.isel.entity.User
import pt.isel.repository.interfaces.IRepository

interface IUserRepository : IRepository<User> {
    fun findByEmail(email: String): User?
    fun findByUserName(userName: String): User?
}