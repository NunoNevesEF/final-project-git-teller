package pt.isel.repository.memory

import org.springframework.stereotype.Repository
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.IScheduledReportRepository
import java.time.Duration
import java.time.Instant

@Repository
class ScheduledReportRepoMem(): RepoMem<ScheduledReportEntity<*,*>>(), IScheduledReportRepository {
    override fun findByUserId(userId: Int): List<ScheduledReportEntity<*,*>> =
        persistence.values.filter{ it.user.id == userId }

    override fun findDue(): List<ScheduledReportEntity<*,*>> {
        val limit = Instant.now().plus(Duration.ofMinutes(15))
        return persistence.values
            .filter { it.isDue(limit) }
            .sortedBy{ it.nextRunAt }
    }
}