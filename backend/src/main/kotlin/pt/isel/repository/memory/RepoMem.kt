package pt.isel.repository.memory

import pt.isel.entity.IsEntity
import pt.isel.repository.interfaces.IRepository
import java.util.concurrent.atomic.AtomicInteger

/**
 *  [RepoMem]
 *
 * Abstract class that implements the in-memory CRUD operation logic for each repository.
 *
 * @param [E] the entity being managed by this implementation.
 * @property [idCounter] an thread-safe counter that keeps track of the next id to give to an entity.
 * @property [persistence] the in-memory persistence for the application entities.
 * */
abstract class RepoMem<E: IsEntity>: IRepository<E> {
    private val idCounter = AtomicInteger(0)
    protected val persistence = mutableMapOf<Int,E>()

    /** @param [entity] The [entity] that is to be inserted in the persistence.
     * @return The inserted [entity]
     * **/
    override fun create(entity: E): E {
        entity.id = idCounter.getAndIncrement()
        persistence[entity.id] = entity
        return entity
    }
    /** @param [id] The [id] of entity being looked up from the persistence.
     * @return The entity if found, else null.
     * **/
    override fun findById(id: Int): E? =
        persistence[id]
    /** @param [entity] The updated [entity] to be persisted in place of the old one.
     * @return The updated [entity] if it existed in the persistence, else null.
     * **/
    override fun update(entity: E): E? {
        if (!persistence.containsKey(entity.id)) return null
        persistence[entity.id] = entity
        return entity
    }
    /** @param [id] The [id] of the entity being deleted from the persistence.
     * @return The deleted entity if it existed in the persistence, else null.
     * **/
    override fun delete(id: Int): E? =
        persistence.remove(id)
}