package pt.isel.repository

interface IRepository<E> {

    fun create(entity: E): E
    fun findById(id: Int): E?
    fun update(entity: E): E?
    fun delete(id: Int): E?
}