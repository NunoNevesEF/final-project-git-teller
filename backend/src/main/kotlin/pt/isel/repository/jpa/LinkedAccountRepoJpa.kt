package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.entity.LinkedAccountEntity

interface LinkedAccountRepoJpa : JpaRepository<LinkedAccountEntity, Int> {

    fun findByUserId(userId: Int): List<LinkedAccountEntity>

    fun findByUserIdAndType(userId: Int, type: String): List<LinkedAccountEntity>

    fun findByUserIdAndTypeAndProviderId(
        userId: Int,
        type: String,
        providerId: String?
    ): LinkedAccountEntity?

    fun findFirstByTypeAndProviderId(
        type: String,
        providerId: String?
    ): LinkedAccountEntity?
}