package pt.isel.gitteller.repo.memory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import pt.isel.entity.IsEntity
import pt.isel.repository.memory.RepoMem
import kotlin.test.Test

interface RepoMemTest<T : IsEntity> {

    fun createRepo(): RepoMem<T>

    fun createEntity(): T
    fun updateEntity(entity: T): T

    @Test
    fun `method create persists entity`() {
        val repo = createRepo()
        val created = repo.create(createEntity())
        assertEquals(created, repo.findById(created.id))
    }

    @Test
    fun `method update persists and returns updated entity`(){
        val repo = createRepo()
        val updated = updateEntity(repo.create(createEntity()))
        assertEquals(updated, repo.update(updated))
        assertEquals(updated, repo.findById(updated.id))
    }

    @Test
    fun `method update returns null if entity not found`() {
        val repo = createRepo()
        assertNull(repo.update(createEntity()))
    }

    @Test
    fun `method delete returns and removes entity`() {
        val repo = createRepo()
        val created = repo.create(createEntity())
        val deleted = repo.delete(created.id)
        assertEquals(created, deleted)
        assertNull(repo.findById(created.id))
    }
}