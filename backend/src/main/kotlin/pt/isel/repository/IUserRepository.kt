package pt.isel.repository

import pt.isel.entity.User

interface IUserRepository : IRepository<User> {
    fun findByEmail(email: String): User?
}