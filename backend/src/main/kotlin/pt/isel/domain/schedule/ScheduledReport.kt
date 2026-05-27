package pt.isel.domain.schedule

import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.utils.CronInput
import pt.isel.utils.CronUtils
import java.time.Instant


sealed class ScheduledReport(
    userId: Int,
    repoUri: String,
    nextRun: Instant?,
    dataStart: Instant
){
    abstract fun completeCurrentExecution(runExecTime: Instant): ScheduledReport
    abstract fun createJob(): ScheduledReportJob
    abstract fun scheduledReportCopy(): ScheduledReport

    init{
        require(userId >= 0){"userId must be greater than or equal to zero."}
        require(repoUri.isNotBlank()){"repoURI must not be blank"}
        if(nextRun != null) require(dataStart < nextRun){ "Data search for report generation must be before Job run time" }
    }

    abstract val userId : Int
    abstract val repoUri: String
    abstract val nextRun: Instant?
    abstract val lastRun: Instant?
    abstract val dataStart: Instant

    abstract fun toEntity(user: User): ScheduledReportEntity
}

data class OneTimeScheduledReport(
    override val userId: Int,
    override val repoUri: String,
    override val nextRun: Instant?,
    override val lastRun: Instant? = null,
    override val dataStart: Instant,
) : ScheduledReport(userId, repoUri, nextRun, dataStart){
    init{
        require((nextRun == null) != (lastRun == null)){ "One between nextRun and lastRun must be not null" }
    }
    companion object {
        fun create(
            userId: Int, repoURI: String,
            nextRun: Instant, dataStart: Instant = Instant.now()
        ): OneTimeScheduledReport =
            OneTimeScheduledReport(
                userId = userId, repoUri = repoURI,
                nextRun = nextRun, dataStart = dataStart
            )
    }

    override fun completeCurrentExecution(runExecTime: Instant): ScheduledReport = copy(nextRun = null, lastRun = runExecTime)

    override fun createJob(): ScheduledReportJob {
        require(!isCompleted()) { "Scheduled already completed" }
        return ScheduledReportJob.create(
            repoUri = repoUri, scheduledRunAt = nextRun!!, dataFrom = dataStart
        )
    }

    override fun scheduledReportCopy(): ScheduledReport = copy()

    override fun toEntity(user: User): ScheduledReportEntity =
        OneTimeScheduledReportEntity(
            repoUri = repoUri,
            nextRun = nextRun,
            lastRun = lastRun,
            dataStart = dataStart
        ).apply{
            this.user = user
        }

    private fun isCompleted() = nextRun == null


}

data class PeriodicScheduledReport(
    override val userId: Int,
    override val repoUri: String,
    override val nextRun: Instant,
    override val lastRun: Instant? = null,
    override val dataStart: Instant,

    val active: Boolean = true,

    val timeZone: String,
    val cronExpression: String,
) : ScheduledReport(userId, repoUri, nextRun, dataStart) {
    companion object {
        fun create(
            userId: Int, repoURI: String, timeZone: String, cronInput: CronInput,
        ): PeriodicScheduledReport {
            val cronExpression = CronUtils.build(cronInput)
            val nextRun = CronUtils.calculateNext(cronExpression, timeZone)
            return PeriodicScheduledReport(
                userId = userId, repoUri = repoURI,
                nextRun = nextRun, timeZone = timeZone, cronExpression = cronExpression,
                dataStart = CronUtils.calculatePrev(cronInput.mode, timeZone, nextRun)
            )
        }
    }

    override fun completeCurrentExecution(runExecTime: Instant): ScheduledReport =
        this.copy(nextRun = calculateNextRunTime(), lastRun = runExecTime, dataStart = nextRun)

    override fun scheduledReportCopy(): ScheduledReport = copy()

    override fun createJob(): ScheduledReportJob {
        return ScheduledReportJob.create(
            repoUri = repoUri, scheduledRunAt = nextRun, dataFrom = dataStart
        )
    }

    override fun toEntity(user: User): ScheduledReportEntity =
        PeriodicScheduledReportEntity(
            repoUri = repoUri,
            nextRun = nextRun,
            lastRun = lastRun,
            dataStart = dataStart,
            active = active,
            timeZone = timeZone,
            cronExpression = cronExpression,
        ).also{
            it.user = user
        }

    private fun calculateNextRunTime(): Instant =
        CronUtils.calculateNext(
            cronExpression = cronExpression,
            timeZone = timeZone,
            from = nextRun.plusSeconds(1) //Start checking from after nextRun.
        )
}

