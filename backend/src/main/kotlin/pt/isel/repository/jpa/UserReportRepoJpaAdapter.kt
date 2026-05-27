package pt.isel.repository.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.ReportEntity
import pt.isel.repository.interfaces.IUserReportRepository

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class UserReportRepoJpaAdapter(
    jpa: UserReportRepoJpa
) : RepoJpaAdapter<ReportEntity, UserReportRepoJpa>(jpa), IUserReportRepository {
    override fun findByUserId(userId: Int): List<ReportEntity> {
        return jpa.findByUserId(userId)
    }

    override fun findByIdAndUserId(id: Int, userId: Int): ReportEntity? {
        return jpa.findByIdAndUserId(id, userId)
    }
}