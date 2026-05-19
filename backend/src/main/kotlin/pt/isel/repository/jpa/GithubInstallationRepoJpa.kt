package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import pt.isel.entity.GithubInstallationEntity

interface GithubInstallationRepoJpa : JpaRepository<GithubInstallationEntity, Int> {
    fun findByUserId(userId: Int): List<GithubInstallationEntity>
    fun findByInstallationId(installationId: Long): GithubInstallationEntity?
    fun deleteByInstallationId(installationId: Long)
}