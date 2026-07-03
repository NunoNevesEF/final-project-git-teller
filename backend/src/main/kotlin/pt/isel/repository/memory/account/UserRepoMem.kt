package pt.isel.repository.memory.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.User
import pt.isel.repository.interfaces.account.IUserRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class UserRepoMem : IUserRepository {
    private val idCounter = AtomicInteger(0)
    private val users = mutableMapOf<Int, User>()

    override fun create(entity: User): User =
        entity.copy(id = nextId()).also{ user -> users[user.id] = user }

    override fun findById(id: Int): User? {
        return users.values.find { it.id == id }
    }

    override fun findByEmail(email: String): User? {
        return users.values.firstOrNull { it.email == email }
    }

    override fun findByUserName(userName: String): User? {
        return users.values.firstOrNull { it.userName == userName }
    }

    override fun update(entity: User): User?{
        if(users.containsKey(entity.id)){
            users[entity.id] = entity
            return entity
        }
        return null
    }

    override fun delete(id: Int): User? = users.remove(id)

    fun nextId(): Int = idCounter.getAndIncrement()
    fun currId(): Int = idCounter.get()
}