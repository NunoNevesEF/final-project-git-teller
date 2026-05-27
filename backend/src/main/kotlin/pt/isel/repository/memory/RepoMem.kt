package pt.isel.repository.memory

import pt.isel.entity.IsEntity
import pt.isel.repository.interfaces.IRepository
import java.util.concurrent.atomic.AtomicInteger

abstract class RepoMem<T: IsEntity>: IRepository<T> {
    private val idCounter = AtomicInteger(0)
    protected val persistence = mutableMapOf<Int,T>()

    override fun create(entity: T): T {
        entity.id = idCounter.getAndIncrement()
        persistence[entity.id] = entity
        return entity
    }

    override fun findById(id: Int): T? =
        persistence[id]

    override fun update(entity: T): T? {
        if (!persistence.containsKey(entity.id)) return null
        persistence[entity.id] = entity
        return entity
    }

    override fun delete(id: Int): T? =
        persistence.remove(id)
}