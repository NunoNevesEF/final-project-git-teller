package pt.isel.repository

import pt.isel.service.Either
import pt.isel.service.userServices.UserNotFound

interface IRepository<E> {
    fun create(entity: E): E
    fun read(id: Int): E?
    fun update(entity: E): E?
    fun delete(id: Int): E?
}