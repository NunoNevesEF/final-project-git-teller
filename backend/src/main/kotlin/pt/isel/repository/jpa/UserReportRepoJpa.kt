package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.ReportEntity

@Repository
interface UserReportRepoJpa : JpaRepository<ReportEntity, Int> {
    fun findByUserId(userId: Int): List<ReportEntity>
    fun findByIdAndUserId(id: Int, userId: Int): ReportEntity?
}