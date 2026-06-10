package pt.isel.domain.schedule

import pt.isel.entity.schedule.*
import java.time.Duration
import java.time.Instant

sealed class ScheduledReportJob(
    id: Int,
    scheduledReportId: Int,
    dataFrom: Instant,
    dataTo: Instant
) {
    companion object {
        fun create(scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant) = PendingJob(
            scheduledReportId = scheduledReportId, dataFrom = dataFrom, scheduledFor = scheduledFor
        )
    }

    init {
        require(id >= 0) { "id must be greater than or equal to 0" }
        require(scheduledReportId >= 0) { "scheduledReport id must be greater than or equal to 0" }
        require(dataFrom < dataTo) { "dataFrom must be lesser than dataTo" }
    }

    abstract val id: Int
    abstract val scheduledReportId: Int
    abstract val dataFrom: Instant
    abstract val scheduledFor: Instant
    abstract val retryCount: Int

    fun toEntity(): ScheduledReportJobEntity =
        ScheduledReportJobEntity(
            id, dataFrom, scheduledFor, retryCount,
            state = getStateEmbeddable(),
        )

    abstract fun getStateEmbeddable(): ScheduledReportJobStateEmbeddable
}

sealed class NonPendingJob(
    id: Int,
    scheduledReportId: Int,
    dataFrom: Instant,
    dataTo: Instant,
) : ScheduledReportJob(id, scheduledReportId, dataFrom, dataTo) {
    abstract val startedAt: Instant
}

sealed class CompletedJob(
    id: Int,
    scheduledReportId: Int,
    dataFrom: Instant,
    dataTo: Instant,
) : NonPendingJob(id, scheduledReportId, dataFrom, dataTo) {
    abstract val endedAt: Instant
}

data class PendingJob(
    override val id: Int = 0,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int = 0,
    val runAt: Instant = scheduledFor,
) : ScheduledReportJob(id, scheduledReportId, dataFrom, scheduledFor) {
    fun run() = RunningJob(id, scheduledReportId, dataFrom, scheduledFor, retryCount)
    override fun getStateEmbeddable(): ScheduledReportJobStateEmbeddable =
        PendingJobStateEmbeddable(runAt)
}

data class RunningJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    override val startedAt: Instant = Instant.now()
) : NonPendingJob(id, scheduledReportId, dataFrom, scheduledFor) {
    fun end(isSuccess: Boolean, errorMsg: String = "", allowRetry: Boolean): ScheduledReportJob {
        return if (isSuccess) success()
        else if (!allowRetry) failure(errorMsg)
        else {
            if (ScheduledJobReportPolicy.maxedRetries(retryCount)) failure("exceeded retries")
            else retry()
        }
    }

    private fun success() = SuccessfulJob(id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt)

    private fun failure(errorMsg: String) = FailedJob(
        id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt, errorMsg = errorMsg
    )

    private fun retry() = PendingJob(
        id,
        scheduledReportId,
        dataFrom,
        scheduledFor,
        retryCount + 1,
        runAt = Instant.now().plus(ScheduledJobReportPolicy.nextRetry(retryCount))
    )

    override fun getStateEmbeddable(): ScheduledReportJobStateEmbeddable =
        RunningJobStateEmbeddable(startedAt)
}

data class SuccessfulJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    override val startedAt: Instant,
    override val endedAt: Instant = Instant.now()
) : CompletedJob(id, scheduledReportId, dataFrom, scheduledFor) {
    override fun getStateEmbeddable(): ScheduledReportJobStateEmbeddable =
        SuccessfulJobStateEmbeddable(startedAt, endedAt)
}

data class FailedJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    override val startedAt: Instant,
    override val endedAt: Instant = Instant.now(),
    val errorMsg: String,
) : CompletedJob(id, scheduledReportId, dataFrom, scheduledFor) {
    override fun getStateEmbeddable(): ScheduledReportJobStateEmbeddable =
        FailedJobStateEmbeddable(startedAt, endedAt, errorMsg)
}

data object ScheduledJobReportPolicy {
    const val MAX_RETRIES = 2

    fun nextRetry(retryCount: Int): Duration = when (retryCount) {
        0 -> Duration.ofSeconds(30)
        1 -> Duration.ofMinutes(2)
        2 -> Duration.ofMinutes(5)
        else -> Duration.ofMinutes(Long.MAX_VALUE)
    }

    fun maxedRetries(attempt: Int) = attempt >= MAX_RETRIES
}