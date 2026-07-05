package pt.isel.model.report

import pt.isel.entity.ReportEntity
import java.time.Instant

data class UserReportDTO(
    val id: Int,
    val createdAt: Instant,
    val repoUri: String,
){
    companion object{
        fun create(report: ReportEntity) =
            UserReportDTO(
                report.id,
                report.createdAt,
                report.gitAnalysis.searchInfo.repositoryUrl
            )
    }
}

data class CreateUserReportDTO(
    val gitAnalysis: GitAnalysis,
    val repoUri: String,
)