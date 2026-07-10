package pt.isel.entity

/**
 *  `IsEntity`
 *
 * A interface that indicates to other components of the application when an object is an entity.
 * Used to create common implementations for entity objects, like CRUD operations in a repository.
 *
 * @property [id] the unique identifier for this entity in the persistence.
 * */
interface IsEntity{
    var id: Int
}