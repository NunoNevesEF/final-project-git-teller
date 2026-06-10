package pt.isel.domain.schedule

import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.utils.CronInput
import pt.isel.utils.CronUtils
import java.time.Instant

sealed class ScheduledReport<SELF : ScheduledReport<SELF, ENTITY>, ENTITY : ScheduledReportEntity<ENTITY, SELF>>(
    id: Int, userId: Int, repoUri: String, nextRun: Instant?, dataStart: Instant
) {
    init {
        require(id >= 0) { "id must be greater than or equal to 0" }
        require(userId >= 0) { "userId must be greater than or equal to zero." }
        require(repoUri.isNotBlank()) { "repoURI must not be blank" }
        if (nextRun != null) require(dataStart < nextRun) { "Data search for report generation must be before Job run time" }
    }

    abstract val id: Int
    abstract val userId: Int
    abstract val repoUri: String
    abstract val nextRunAt: Instant?
    abstract val lastRunAt: Instant?
    abstract val dataFrom: Instant

    fun createJob(): PendingJob = ScheduledReportJob.create(
        scheduledReportId = id, scheduledFor = nextRunAt!!, dataFrom = dataFrom
    )

    abstract fun advanceSchedule(): SELF
    abstract fun recordExecution(jobExecTime: Instant): SELF
    abstract fun toEntity(user: User): ENTITY
}

data class OneTimeScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRunAt: Instant?,
    override val lastRunAt: Instant? = null,
    override val dataFrom: Instant,
) : ScheduledReport<OneTimeScheduledReport, OneTimeScheduledReportEntity>(id, userId, repoUri, nextRunAt, dataFrom) {
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String, nextRun: Instant, dataStart: Instant = Instant.now()
        ): OneTimeScheduledReport = OneTimeScheduledReport(
            id = id, userId = userId, repoUri = repoURI, nextRunAt = nextRun, dataFrom = dataStart
        )
    }

    override fun advanceSchedule() = copy(nextRunAt = null)
    override fun recordExecution(jobExecTime: Instant): OneTimeScheduledReport = copy(lastRunAt = jobExecTime)

    override fun toEntity(user: User) = OneTimeScheduledReportEntity(
        id = id, repoUri = repoUri, nextRunAt = nextRunAt, lastRunAt = lastRunAt, dataFrom = dataFrom
    ).apply { this.user = user }
}

data class PeriodicScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRunAt: Instant,
    override val lastRunAt: Instant? = null,
    override val dataFrom: Instant,

    val active: Boolean = true,
    val timeZone: String,
    val cronExpression: String,
) : ScheduledReport<PeriodicScheduledReport, PeriodicScheduledReportEntity>(id, userId, repoUri, nextRunAt, dataFrom) {
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String, timeZone: String, cronInput: CronInput,
        ): PeriodicScheduledReport {
            val cronExpression = CronUtils.build(cronInput)
            val nextRun = CronUtils.calculateNext(cronExpression, timeZone)
            return PeriodicScheduledReport(
                id = id,
                userId = userId,
                repoUri = repoURI,
                nextRunAt = nextRun,
                timeZone = timeZone,
                cronExpression = cronExpression,
                dataFrom = CronUtils.calculatePrev(cronInput.mode, timeZone, nextRun)
            )
        }
    }

    override fun advanceSchedule() = copy(nextRunAt = calculateNextRunTime(), dataFrom = nextRunAt)

    override fun recordExecution(jobExecTime: Instant) = copy(lastRunAt = jobExecTime)

    override fun toEntity(user: User) = PeriodicScheduledReportEntity(
        id = id,
        repoUri = repoUri,
        nextRunAt = nextRunAt,
        lastRunAt = lastRunAt,
        dataFrom = dataFrom,
        active = active,
        timeZone = timeZone,
        cronExpression = cronExpression
    ).also { it.user = user }

    private fun calculateNextRunTime(): Instant = CronUtils.calculateNext(
        cronExpression = cronExpression,
        timeZone = timeZone,
        from = nextRunAt.plusSeconds(1) //Start checking from after nextRun.
    )
}

