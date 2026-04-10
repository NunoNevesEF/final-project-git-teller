package pt.isel.repository.memory.account

import org.springframework.stereotype.Repository
import pt.isel.domain.account.User
import pt.isel.repository.IRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
class UserRepoMem: IRepository<User> {
    private val idCounter = AtomicInteger(0)
    private val users = mutableMapOf<Int, User>()

    override fun create(entity: User): User =
        entity.copy(id = nextId()).also{ user -> users[user.id] = user }

    override fun read(id: Int): User? = users[id]

    fun read(email: String): User? = users.values.firstOrNull { it.email == email }

    fun readOrCreateByEmail(entity: User): User =
        read(entity.email) ?: create(entity)

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