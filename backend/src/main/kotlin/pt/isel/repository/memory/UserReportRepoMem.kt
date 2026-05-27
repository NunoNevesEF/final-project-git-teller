package pt.isel.repository.memory

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.ReportEntity
import pt.isel.repository.interfaces.IUserReportRepository

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class UserReportRepoMem: RepoMem<ReportEntity>(), IUserReportRepository{
    override fun findByUserId(userId: Int): List<ReportEntity> =
        persistence.values.filter{ report -> report.user.id == userId }

    override fun findByIdAndUserId(id: Int, userId: Int): ReportEntity? =
        persistence.values.firstOrNull { report -> report.user.id == userId && report.id == id }
}