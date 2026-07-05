package pt.isel.repository.memory

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.IScheduledReportRepository
import java.time.Duration
import java.time.Instant

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class ScheduledReportRepoMem(): RepoMem<ScheduledReportEntity>(), IScheduledReportRepository {
    override fun findByIdAndUserId(id: Int, userId: Int): ScheduledReportEntity? =
        persistence.values.firstOrNull{ it.id == id && it.user.id == userId}

    override fun findByUserId(userId: Int): List<ScheduledReportEntity> =
        persistence.values.filter{ it.user.id == userId }

    override fun findDue(): List<ScheduledReportEntity> {
        val limit = Instant.now().plus(Duration.ofMinutes(15))
        return persistence.values
            .filter { it.isDue(limit) }
            .sortedBy{ it.nextRunAt }
    }
}