package pt.isel.repository.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.IScheduledReportRepository
import java.time.Duration
import java.time.Instant

@Repository
interface ScheduledReportRepoJpa : JpaRepository<ScheduledReportEntity<*,*>, Int>{
    fun findByUserId(userId: Int): List<ScheduledReportEntity<*,*>>
}

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class ScheduledReportJpaAdapter(
    jpa: ScheduledReportRepoJpa
) : RepoJpaAdapter<ScheduledReportEntity<*,*>, ScheduledReportRepoJpa>(jpa), IScheduledReportRepository {
    override fun findByUserId(userId: Int): List<ScheduledReportEntity<*, *>> =
        jpa.findByUserId(userId)

    override fun findDue(): List<ScheduledReportEntity<*, *>>{ //TODO: CAN BE PROBABLY BE IMPLEMENTED WITH BETTER EFFICIENCY
        val limit = Instant.now().plus(Duration.ofMinutes(15))
        return jpa.findAll()
            .filter{ it.isDue(limit) }
            .sortedBy{ it.nextRunAt }
    }
}