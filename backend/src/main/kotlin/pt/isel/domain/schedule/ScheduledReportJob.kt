package pt.isel.domain.schedule

import java.time.Duration
import java.time.Instant

data class ScheduledReportJob(
    val id: Int,
    val scheduledReportId: Int,
    val state: ScheduledReportJobState,

    val repoUri: String,
    val dataFrom: Instant,
    val dataTo: Instant,
){
    init{
        require(id >= 0) { "id must be greater than or equal to 0" }
        require(scheduledReportId >= 0) { "scheduledReportId must be greater than or equal to 0" }
        require(dataFrom < dataTo) { "dataFrom must be lesser than dataTo" }
        require(repoUri.isNotBlank()) { "repoUri must not be blank" }
    }

    companion object {
        fun create(
            id: Int = 0, scheduledReportId: Int, repoUri: String,
            scheduledRunAt: Instant, dataFrom: Instant, dataTo: Instant = scheduledRunAt,
        ) = ScheduledReportJob(
            id = id, scheduledReportId = scheduledReportId, state = Pending(scheduledRunAt),
            repoUri = repoUri, dataFrom = dataFrom, dataTo = dataTo
        )
    }
}

sealed class ScheduledReportJobState(open val attempt: Int){
    abstract val isComplete: Boolean
}
sealed class RunningState(open val startedAt: Instant, attempt: Int): ScheduledReportJobState(attempt)
sealed class CompletedState(open val endedAt: Instant, startedAt: Instant, attempt: Int): RunningState(startedAt, attempt)

data class Pending(val scheduledRunAt: Instant, override val attempt: Int = 1): ScheduledReportJobState(attempt){
    fun run() = Running(attempt = attempt)

    override val isComplete: Boolean = false
}


data class Running(
    override val startedAt: Instant = Instant.now(), override val attempt: Int
): RunningState(startedAt, attempt){
    fun end(isSuccess: Boolean, errorMsg: String = ""): ScheduledReportJobState {
        return if(isSuccess) success()
        else{
            if(ScheduledJobReportPolicy.maxedAttempts(attempt)) failure(errorMsg)
            else retry()
        }
    }

    private fun success() = Success(startedAt = startedAt, attempt = attempt)

    private fun failure(errorMsg: String) = Failure(
        startedAt = startedAt, attempt = attempt, errorMsg = errorMsg
    )

    private fun retry() = Pending(
        scheduledRunAt = Instant.now().plus(ScheduledJobReportPolicy.nextRetry(attempt)),
        attempt = attempt+1
    )

    override val isComplete: Boolean = false
}

data class Success(
    override val startedAt: Instant, override val endedAt: Instant = Instant.now(), override val attempt: Int
): CompletedState(startedAt = startedAt, endedAt = endedAt, attempt = attempt){
    override val isComplete: Boolean = true
}
data class Failure(
    override val startedAt: Instant, override val endedAt: Instant = Instant.now(), override val attempt: Int, val errorMsg: String
): CompletedState(startedAt = startedAt, endedAt = endedAt, attempt = attempt){
    override val isComplete: Boolean = true
}

data object ScheduledJobReportPolicy{
    const val MAX_ATTEMPTS = 3

    fun nextRetry(attempt: Int): Duration =
        when (attempt) {
            1 -> Duration.ofSeconds(30)
            2 -> Duration.ofMinutes(2)
            3 -> Duration.ofMinutes(5)
            else -> Duration.ofMinutes(Long.MAX_VALUE)
        }

    fun maxedAttempts(attempt: Int) = attempt >= MAX_ATTEMPTS
}