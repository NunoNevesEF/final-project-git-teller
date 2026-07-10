package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import pt.isel.entity.IsEntity
import pt.isel.repository.interfaces.IRepository
import pt.isel.repository.memory.RepoMem

/**
 *  `RepoJpaAdapter`
 *
 * Abstract class that implements the jpa CRUD operation logic for each repository.
 *
 * @param E the type of entity managed by this repository.
 * @param J the type of the [JpaRepository] for the managed entity.
 * @property jpa the [JpaRepository] used to perform persistence operations.
 * */
abstract class RepoJpaAdapter<E : IsEntity, J : JpaRepository<E, Int>>(protected val jpa: J) : IRepository<E> {
    /** @param [entity] The [entity] that is to be inserted in the persistence.
     * @return The inserted [entity]
     * **/
    override fun create(entity: E): E =
        jpa.save(entity)
    /** @param [id] The [id] of entity being looked up from the persistence.
     * @return The entity if found, else null.
     * **/
    override fun findById(id: Int): E? =
        jpa.findByIdOrNull(id)
    /** @param [entity] The updated [entity] to be persisted in place of the old one.
     * @return The updated [entity] if it existed in the persistence, else null.
     * **/
    override fun update(entity: E): E? =
        if (jpa.existsById(entity.id)) jpa.save(entity)
        else null
    /** @param [id] The [id] of the entity being deleted from the persistence.
     * @return The deleted entity if it existed in the persistence, else null.
     * **/
    override fun delete(id: Int): E? {
        val entity = findById(id) ?: return null
        jpa.deleteById(id)
        return entity
    }
}