package pt.isel.gitteller.repository.memory.report

import pt.isel.entity.account.User
import pt.isel.entity.report.model.JobStatus
import pt.isel.entity.report.schedule.JobStateEmbeddable
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import pt.isel.gitteller.repository.interfaces.report.ScheduledReportRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IScheduledReportRepository
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.repository.memory.report.ScheduledReportRepoMem
import java.time.Instant


class ScheduledReportRepoMemTest : ScheduledReportRepoTest() {
    override fun repo(): IScheduledReportRepository = ScheduledReportRepoMem()

    override fun userRepo(): IUserRepository = UserRepoMem()

    override fun createScheduledReportEntity(
        repoUri: String,
        nextRunAt: Instant,
        lastRunAt: Instant,
        dataFrom: Instant,
        user: User,
        isCancelled: Boolean,
        doAddJob: Boolean,
    ): ScheduledReportEntity = OneTimeScheduledReportEntity(
        0, repoUri, nextRunAt, lastRunAt, lastRunAt, isCancelled, ""
    ).apply{
        this.user = user
        if(doAddJob) this.addJob(
            ScheduledReportJobEntity(
                dataFrom = dataFrom,
                dataTo = nextRunAt,
                scheduledFor = nextRunAt,
                retryCount = 0,
                state = JobStateEmbeddable.pending(nextRunAt),
            )
        )
    }

    override fun updateEntity(entity: ScheduledReportEntity): ScheduledReportEntity {
        val job = entity.jobs.first()

        entity.updateJob(job.id){
            job.state = JobStateEmbeddable.running(entity.nextRunAt!!)
            job
        }!!
        return entity
    }
}