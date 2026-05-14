package pt.isel.domain.schedule

import pt.isel.utils.CronInput
import pt.isel.utils.CronUtils
import java.time.Instant


sealed class ScheduledReport(
    id: Int,
    userId: Int,
    repoUri: String,
    nextRun: Instant?,
    dataStart: Instant
){
    abstract fun completeCurrentExecution(runExecTime: Instant): ScheduledReport
    abstract fun createJob(): ScheduledReportJob
    abstract fun scheduledReportCopy(id: Int? = null): ScheduledReport

    init{
        require(id >= 0){"id must be greater than or equal to zero."}
        require(userId >= 0){"userId must be greater than or equal to zero."}
        require(repoUri.isNotBlank()){"repoURI must not be blank"}
        if(nextRun != null) require(dataStart < nextRun){ "Data search for report generation must be before Job run time" }
    }

    abstract val id : Int
    abstract val userId : Int
    abstract val repoUri: String
    abstract val nextRun: Instant?
    abstract val lastRun: Instant?
    abstract val dataStart: Instant
}

data class OneTimeScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRun: Instant?,
    override val lastRun: Instant? = null,
    override val dataStart: Instant,
) : ScheduledReport(id, userId, repoUri, nextRun, dataStart){
    init{
        require((nextRun == null) != (lastRun == null)){ "One between nextRun and lastRun must be not null" }
    }
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String,
            nextRun: Instant, dataStart: Instant = Instant.now()
        ): OneTimeScheduledReport =
            OneTimeScheduledReport(
                id = id, userId = userId, repoUri = repoURI,
                nextRun = nextRun, dataStart = dataStart
            )
    }

    override fun completeCurrentExecution(runExecTime: Instant): ScheduledReport = copy(nextRun = null, lastRun = runExecTime)

    override fun createJob(): ScheduledReportJob {
        require(!isCompleted()) { "Scheduled already completed" }
        return ScheduledReportJob.create(
            scheduledReportId = id, repoUri = repoUri,
            scheduledRunAt = nextRun!!, dataFrom = dataStart
        )
    }

    override fun scheduledReportCopy(id: Int?): ScheduledReport = copy(id = id ?: this.id)

    private fun isCompleted() = nextRun == null
}

data class PeriodicScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRun: Instant,
    override val lastRun: Instant? = null,
    override val dataStart: Instant,

    val active: Boolean = true,

    val timeZone: String,
    val cronExpression: String,
) : ScheduledReport(id, userId, repoUri, nextRun, dataStart) {
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String, timeZone: String, cronInput: CronInput,
        ): PeriodicScheduledReport {
            val cronExpression = CronUtils.build(cronInput)
            val nextRun = CronUtils.calculateNext(cronExpression, timeZone)
            return PeriodicScheduledReport(
                id = id, userId = userId, repoUri = repoURI,
                nextRun = nextRun, timeZone = timeZone, cronExpression = cronExpression,
                dataStart = CronUtils.calculatePrev(cronInput.mode, timeZone, nextRun)
            )
        }
    }

    override fun completeCurrentExecution(runExecTime: Instant): ScheduledReport =
        this.copy(nextRun = calculateNextRunTime(), lastRun = runExecTime, dataStart = nextRun)

    override fun scheduledReportCopy(id: Int?): ScheduledReport = copy(id = id ?: this.id)

    override fun createJob(): ScheduledReportJob {
        return ScheduledReportJob.create(
            scheduledReportId = id, repoUri = repoUri,
            scheduledRunAt = nextRun, dataFrom = dataStart
        )
    }

    private fun calculateNextRunTime(): Instant =
        CronUtils.calculateNext(
            cronExpression = cronExpression,
            timeZone = timeZone,
            from = nextRun.plusSeconds(1) //Start checking from after nextRun.
        )
}

