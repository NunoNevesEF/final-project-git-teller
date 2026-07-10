package pt.isel.repository.interfaces

import pt.isel.entity.IsEntity

/**
 *  `IRepository`
 *
 * Interface that establishes the CRUD operations to be implemented for each repository.
 *
 * @param [E] the type of the entity to be managed for the repository implementation
 * */
interface IRepository<E: IsEntity> {
    /** @param [entity] The [entity] that is to be inserted in the persistence.
     * @return The inserted [entity]
     * **/
    fun create(entity: E): E
    /** @param [id] The [id] of the entity being looked up from the persistence.
     * @return The entity if found, else null.
     * **/
    fun findById(id: Int): E?
    /** @param [entity] The updated [entity] to be persisted in place of the old one.
     * @return The updated [entity] if it existed in the persistence, else null.
     * **/
    fun update(entity: E): E?
    /** @param [id] The [id] of the entity being deleted from the persistence.
     * @return The deleted entity if it existed in the persistence, else null.
     * **/
    fun delete(id: Int): E?
}