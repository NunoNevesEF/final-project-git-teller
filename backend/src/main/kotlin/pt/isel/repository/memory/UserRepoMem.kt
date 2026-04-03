package pt.isel.repository.memory

import jakarta.inject.Named
import pt.isel.domain.User
import java.util.concurrent.atomic.AtomicInteger

@Named
class UserRepoMem: IRepositoryMem<User> {
    private val idCounter = AtomicInteger(0)
    private val users = mutableMapOf<Int, User>()

    override fun create(entity: User): User =
        entity.copy(data = entity.data.copy(id = nextId()))
            .also{ user -> users[user.data.id] = user }

    override fun read(id: Int): User? = users[id]

    fun read(email: String): User? = users.values.firstOrNull { it.data.email == email }

    fun readOrCreateByEmail(entity: User): User =
        read(entity.data.email) ?: create(entity)

    override fun update(entity: User): User?{
        if(users.containsKey(entity.data.id)){
            users[entity.data.id] = entity
            return entity
        }
        return null
    }

    override fun delete(id: Int): User? = users.remove(id)

    override fun nextId(): Int = idCounter.getAndIncrement()
    override fun currId(): Int = idCounter.get()
}