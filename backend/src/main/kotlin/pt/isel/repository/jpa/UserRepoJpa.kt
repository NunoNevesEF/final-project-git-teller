package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.User

@Repository
interface UserRepositoryJpa : JpaRepository<User, Int> {
    fun findByEmail(email: String): User?
    fun findByUserName(userName: String): User?
}