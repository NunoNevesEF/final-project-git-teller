package pt.isel.repository.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import pt.isel.entity.UserReport
import pt.isel.repository.IUserReportRepository

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class UserReportRepoJpaAdapter (private val jpa: UserReportRepoJpa) : IUserReportRepository {
    override fun create(entity: UserReport): UserReport {
        return jpa.save(entity)
    }

    override fun findById(id: Int): UserReport? {
        return jpa.findByIdOrNull(id)
    }

    override fun update(entity: UserReport): UserReport? {
        return jpa.save(entity)
    }

    override fun delete(id: Int): UserReport? {
        val report = jpa.findByIdOrNull(id) ?: return null
        jpa.delete(report)
        return report
    }

    override fun findByUserId(userId: Int): List<UserReport> {
        return jpa.findByUserId(userId)
    }

    override fun findByIdAndUserId(reportId: Int, userId: Int): UserReport? {
        return jpa.findByIdAndUserId(reportId, userId)
    }
}