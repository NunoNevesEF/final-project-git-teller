package pt.isel.repository.memory

import pt.isel.repository.IRepository

interface IRepositoryMem<E>: IRepository<E> {
    fun nextId(): Int
    fun currId(): Int
}