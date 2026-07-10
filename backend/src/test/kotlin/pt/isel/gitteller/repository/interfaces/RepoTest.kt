package pt.isel.gitteller.repository.interfaces

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import pt.isel.entity.IsEntity
import pt.isel.repository.interfaces.IRepository
import kotlin.test.Test
import kotlin.test.assertTrue

interface RepoTest <T : IsEntity>{
    fun repo(): IRepository<T>

    fun createEntity(): T
    fun updateEntity(entity: T): T
    fun assertEquality(expected: T, actual: T?)

    @Test
    fun `method create persists entity`() {
        val repo = repo()
        val created = repo.create(createEntity())

        val result = repo.findById(created.id)
        assertEquality(created, result)
    }

    @Test
    fun `method update persists and returns updated entity`(){
        val repo = repo()
        val updated = updateEntity(repo.create(createEntity()))
        assertEquality(updated, repo.update(updated))
        assertEquality(updated, repo.findById(updated.id))
    }

    @Test
    fun `method update returns null if entity not found`() {
        val repo = repo()
        assertNull(repo.update(createEntity()))
    }

    @Test
    fun `method delete returns and removes entity`() {
        val repo = repo()
        val created = repo.create(createEntity())
        val deleted = repo.delete(created.id)
        assertEquality(created, deleted)
        assertNull(repo.findById(created.id))
    }
}