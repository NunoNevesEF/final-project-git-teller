package pt.isel.domain.schedule

import pt.isel.entity.schedule.ScheduledReportJobEntity
import pt.isel.entity.schedule.ScheduledReportJobStateEmbeddable
import pt.isel.entity.schedule.ScheduledReportJobStateEnum
import java.time.Duration
import java.time.Instant

sealed class ScheduledReportJob(
    id: Int,
    repoUri: String,
    dataFrom: Instant,
    dataTo: Instant,
){
    companion object {
        fun create(scheduledRunAt: Instant, repoUri: String, dataFrom: Instant) = PendingJob(
            repoUri = repoUri, dataFrom = dataFrom, dataTo = scheduledRunAt, scheduledAt = scheduledRunAt
        )
    }

    init{
        require(id >= 0){ "id must be greater than or equal to 0" }
        require(dataFrom < dataTo) { "dataFrom must be lesser than dataTo" }
        require(repoUri.isNotBlank()) { "repoUri must not be blank" }
    }

    abstract val id: Int
    abstract val repoUri: String
    abstract val dataFrom: Instant
    abstract val dataTo: Instant
    abstract val retryCount: Int
    abstract val scheduledAt: Instant

    abstract fun toEntity(): ScheduledReportJobEntity
}

sealed class NonPendingJob(
    id: Int,
    repoUri: String,
    dataFrom: Instant,
    dataTo: Instant,
): ScheduledReportJob(id, repoUri, dataFrom, dataTo){
    abstract val startedAt: Instant
}

sealed class CompletedJob(
    id: Int,
    repoUri: String,
    dataFrom: Instant,
    dataTo: Instant,
): NonPendingJob(id, repoUri, dataFrom, dataTo){
    abstract val endedAt: Instant
}

data class PendingJob(
    override val id: Int = 0,
    override val repoUri: String,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val retryCount: Int = 0,
    override val scheduledAt: Instant,
    val runAt: Instant = scheduledAt,
): ScheduledReportJob(id, repoUri, dataFrom, dataTo){
    fun run() = RunningJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt)

    override fun toEntity() =
        ScheduledReportJobEntity(
            id = id,
            repoUri = repoUri,
            dataFrom = dataFrom,
            dataTo = dataTo,
            state = ScheduledReportJobStateEmbeddable(
                type = ScheduledReportJobStateEnum.PENDING,
                retryCount = retryCount,
                scheduledAt = scheduledAt,
                runAt = runAt
            )
        )
}

data class RunningJob(
    override val id: Int,
    override val repoUri: String,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val retryCount: Int,
    override val scheduledAt: Instant,
    override val startedAt: Instant = Instant.now()
): NonPendingJob(id, repoUri, dataFrom, dataTo){
    fun end(isSuccess: Boolean, errorMsg: String = ""): ScheduledReportJob {
        return if(isSuccess) success()
        else{
            if(ScheduledJobReportPolicy.maxedRetries(retryCount)) failure(errorMsg)
            else retry()
        }
    }

    private fun success() = SuccessfulJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, startedAt)

    private fun failure(errorMsg: String) = FailedJob(
        id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, startedAt, errorMsg = errorMsg
    )

    private fun retry() = PendingJob(
        id, repoUri, dataFrom, dataTo, retryCount+1, scheduledAt,
        runAt = Instant.now().plus(ScheduledJobReportPolicy.nextRetry(retryCount))
    )

    override fun toEntity() =
        ScheduledReportJobEntity(
            id = id,
            repoUri = repoUri,
            dataFrom = dataFrom,
            dataTo = dataTo,
            state = ScheduledReportJobStateEmbeddable(
                type = ScheduledReportJobStateEnum.RUNNING,
                retryCount = retryCount,
                scheduledAt = scheduledAt,
                startedAt = startedAt
            )
        )
}

data class SuccessfulJob(
    override val id: Int,
    override val repoUri: String,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val retryCount: Int,
    override val scheduledAt: Instant,
    override val startedAt: Instant,
    override val endedAt: Instant = Instant.now()
): CompletedJob(id, repoUri, dataFrom, dataTo){
    override fun toEntity() =
        ScheduledReportJobEntity(
            id = id,
            repoUri = repoUri,
            dataFrom = dataFrom,
            dataTo = dataTo,
            state = ScheduledReportJobStateEmbeddable(
                type = ScheduledReportJobStateEnum.SUCCESS,
                retryCount = retryCount,
                scheduledAt = scheduledAt,
                startedAt = startedAt,
                endedAt = endedAt
            )
        )
}

data class FailedJob(
    override val id: Int,
    override val repoUri: String,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val retryCount: Int,
    override val scheduledAt: Instant,
    override val startedAt: Instant,
    override val endedAt: Instant = Instant.now(),
    val errorMsg: String,
): CompletedJob(id, repoUri, dataFrom, dataTo){
    override fun toEntity() =
        ScheduledReportJobEntity(
            id = id,
            repoUri = repoUri,
            dataFrom = dataFrom,
            dataTo = dataTo,
            state = ScheduledReportJobStateEmbeddable(
                type = ScheduledReportJobStateEnum.FAILURE,
                retryCount = retryCount,
                scheduledAt = scheduledAt,
                startedAt = startedAt,
                endedAt = endedAt,
                errorMsg = errorMsg
            )
        )
}

data object ScheduledJobReportPolicy{
    const val MAX_RETRIES = 2

    fun nextRetry(retryCount: Int): Duration =
        when (retryCount) {
            0 -> Duration.ofSeconds(30)
            1 -> Duration.ofMinutes(2)
            2 -> Duration.ofMinutes(5)
            else -> Duration.ofMinutes(Long.MAX_VALUE)
        }

    fun maxedRetries(attempt: Int) = attempt >= MAX_RETRIES
}