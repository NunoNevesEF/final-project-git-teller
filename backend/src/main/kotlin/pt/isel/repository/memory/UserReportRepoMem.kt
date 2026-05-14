package pt.isel.repository.memory

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.UserReport
import pt.isel.repository.IUserReportRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class UserReportRepoMem : IUserReportRepository {

    private val idCounter = AtomicInteger(0)
    private val reports = mutableMapOf<Int, UserReport>()

    override fun create(entity: UserReport): UserReport {
        val report = UserReport(
            id = nextId(),
            user = entity.user,
            createdAt = entity.createdAt,
            data = entity.data
        )

        reports[report.id] = report
        return report
    }

    override fun findById(id: Int): UserReport? {
        return reports[id]
    }

    override fun update(entity: UserReport): UserReport? {
        if (reports.containsKey(entity.id)) {
            reports[entity.id] = entity
            return entity
        }

        return null
    }

    override fun delete(id: Int): UserReport? {
        return reports.remove(id)
    }

    override fun findByUserId(userId: Int): List<UserReport> {
        return reports.values.filter { it.user.id == userId }
    }

    override fun findByIdAndUserId(reportId: Int, userId: Int): UserReport? {
        return reports.values.firstOrNull { it.user.id == userId && it.id == reportId }
    }

    private fun nextId(): Int = idCounter.incrementAndGet()
}