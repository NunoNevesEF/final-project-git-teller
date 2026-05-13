package pt.isel.repository.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.User
import pt.isel.repository.IUserRepository

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class UserRepoJpaAdapter(
    private val jpa: UserRepositoryJpa
) : IUserRepository {

    override fun create(entity: User): User =
        jpa.save(entity)

    override fun findById(id: Int): User? =
        jpa.findById(id).orElse(null)

    override fun findByEmail(email: String): User? =
        jpa.findByEmail(email)

    override fun update(entity: User): User? =
        if (jpa.existsById(entity.id)) jpa.save(entity) else null

    override fun delete(id: Int): User? {
        val user = findById(id) ?: return null
        jpa.deleteById(id)
        return user
    }
}