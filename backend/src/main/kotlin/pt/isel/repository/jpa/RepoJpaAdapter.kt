package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import pt.isel.entity.IsEntity
import pt.isel.repository.interfaces.IRepository

abstract class RepoJpaAdapter<T : IsEntity, J : JpaRepository<T, Int>>(protected val jpa: J) : IRepository<T> {
    override fun create(entity: T): T =
        jpa.save(entity)

    override fun findById(id: Int): T? =
        jpa.findByIdOrNull(id)

    override fun update(entity: T): T? =
        if (jpa.existsById(entity.id)) jpa.save(entity)
        else null

    override fun delete(id: Int): T? {
        val entity = findById(id) ?: return null
        jpa.deleteById(id)
        return entity
    }
}