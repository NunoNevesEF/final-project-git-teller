package pt.isel.gitteller.repo.memory.schedule


import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.gitteller.repo.memory.RepoMemTest
import pt.isel.repository.memory.ScheduledReportRepoMem
import java.time.Duration
import java.time.Instant
import pt.isel.gitteller.repo.interfaces.ScheduledReportRepoTest
import pt.isel.repository.interfaces.IScheduledReportRepository


class ScheduledReportRepoMemTest : RepoMemTest<ScheduledReportEntity<*, *>>, ScheduledReportRepoTest() {
    val repo = ScheduledReportRepoMem()

    override fun createScheduledReportEntity(
        id: Int,
        repoUri: String,
        nextRunAt: Instant?,
        lastRunAt: Instant?,
        dataFrom: Instant,
        user: User,
        active: Boolean
    ): OneTimeScheduledReportEntity {
        val scheduledReport = OneTimeScheduledReportEntity(
            id, repoUri, if (active) nextRunAt else null, lastRunAt, dataFrom
        )
        scheduledReport.user = user
        return scheduledReport
    }

    override fun createRepo() = repo
    override fun repo(): IScheduledReportRepository = repo

    override fun createEntity() = createScheduledReportEntity()

    override fun updateEntity(entity: ScheduledReportEntity<*, *>): ScheduledReportEntity<*, *> {
        entity.lastRunAt = validNextRunAt
        return entity
    }
}