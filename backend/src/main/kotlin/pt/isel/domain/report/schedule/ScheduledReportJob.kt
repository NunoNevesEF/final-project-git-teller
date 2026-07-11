package pt.isel.domain.report.schedule

import pt.isel.entity.report.schedule.JobStateEmbeddable
import pt.isel.entity.report.model.JobStatus
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant

/**
 *  `ScheduledReportJobDomainException`
 *
 * Represents exceptions that can occur while constructing a [ScheduledReportJob] domain object.
 *
 * These exceptions are thrown by the domain layer and can be translated into
 * service error objects by the service layer.
 * */
sealed class ScheduledReportJobDomainException: RuntimeException()
/**NextRunAt is before the data collection starting point**/
class JobInvalidDateRangeException: ScheduledReportJobDomainException()

/**@property id the unique identifier for the [ScheduledReportJobEntity] in the persistence.
 * @property scheduledReportId the unique identifier for the [ScheduledReport] that created this job.
 * @property dataFrom the starting point of the repository data to analyse.
 * @property dataTo the ending point of the repository data to analyse.
 * @property scheduledFor when the report generation was originally scheduled for.
 * @property retryCount how many retries has this job done.
 * @throws JobInvalidDateRangeException when data starting point after data ending point
 * @constructor [create] initiates a fresh [PendingJob] with [dataTo]=[scheduledFor] and [retryCount]=0,
 * **/
sealed class ScheduledReportJob(
    dataFrom: Instant,
    dataTo: Instant
) {
    /**
     * Creates a new pending execution for a scheduled report.
     *
     * The resulting job analyses the repository history in the interval
     * [[dataFrom], [scheduledFor]).
     *
     * @param scheduledReportId identifier of the scheduled report that owns the job.
     * @param dataFrom beginning of the repository history to analyse.
     * @param scheduledFor scheduled execution time, also used as the end of the
     * analysed interval.
     * @return a newly created [PendingJob].
     */
    companion object {
        fun create(scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant) = PendingJob(
            scheduledReportId = scheduledReportId, dataFrom = dataFrom, dataTo = scheduledFor, scheduledFor = scheduledFor
        )
    }

    init { if(dataFrom >= dataTo) throw JobInvalidDateRangeException() }

    abstract val id: Int
    abstract val scheduledReportId: Int
    abstract val dataFrom: Instant
    abstract val dataTo: Instant
    abstract val scheduledFor: Instant
    abstract val retryCount: Int

    /**
     * Maps this domain object to its persistence representation.
     *
     * @return the equivalent [ScheduledReportJobEntity].
     */
    fun toEntity(): ScheduledReportJobEntity =
        ScheduledReportJobEntity(
            id, dataFrom, dataTo, scheduledFor, retryCount,
            state = getStateEmbeddable(),
        )

    /**
     * Converts this job's current state into its persistence representation.
     *
     * @return the corresponding [JobStateEmbeddable].
     */
    abstract fun getStateEmbeddable(): JobStateEmbeddable

    /**
     * Returns the current execution status of this job.
     *
     * @return the corresponding [JobStatus].
     */
    abstract fun toStatus(): JobStatus
}

/**
 * `PendingJob`
 *
 * Represents a scheduled report execution waiting to be executed.
 *
 * Pending jobs may represent either an initial execution or a retry scheduled
 * after a previous failed attempt.
 *
 * @property id the unique identifier for the [ScheduledReportJobEntity] in the persistence.
 * @property scheduledReportId the unique identifier for the [ScheduledReport] that created this job.
 * @property dataFrom the starting point of the repository data to analyse.
 * @property dataTo the ending point of the repository data to analyse.
 * @property scheduledFor when the report generation was originally scheduled for.
 * @property retryCount how many retries has this job done.
 * @property runAt when this job should be executed.
 * @throws JobInvalidDateRangeException when data starting point after data ending point
 */
data class PendingJob(
    override val id: Int = 0,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int = 0,
    val runAt: Instant = scheduledFor,
) : ScheduledReportJob(dataFrom, dataTo) {
    /**
     * Starts execution of this job.
     *
     * @return a [RunningJob] representing the executing job.
     */
    fun run() = RunningJob(id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount)

    /**
     * Converts this pending state into its persistence representation.
     *
     * @return a pending [JobStateEmbeddable].
     */
    override fun getStateEmbeddable(): JobStateEmbeddable =
        JobStateEmbeddable.pending(runAt)

    /**
     * Returns the execution status of this job.
     *
     * @return [JobStatus.PENDING].
     */
    override fun toStatus(): JobStatus = JobStatus.PENDING
}

/**
 * `RunningJob`
 *
 * Represents a scheduled report that is currently executing.
 *
 * A running job may complete successfully, fail permanently or be scheduled
 * for another execution depending on the execution result and retry policy.
 *
 * @property id the unique identifier for the [ScheduledReportJobEntity] in the persistence.
 * @property scheduledReportId the unique identifier for the [ScheduledReport] that created this job.
 * @property dataFrom the starting point of the repository data to analyse.
 * @property dataTo the ending point of the repository data to analyse.
 * @property scheduledFor when the report generation was originally scheduled for.
 * @property retryCount how many retries has this job done.
 * @property startedAt when execution began.
 *
 * @throws JobInvalidDateRangeException when data starting point after data ending point
 */
data class RunningJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    val startedAt: Instant = Instant.now()
) : ScheduledReportJob(dataFrom, scheduledFor) {
    /**
     * Completes execution of this job.
     *
     * Depending on the execution result and retry policy, the returned job will
     * a permanent failure or a pending retry.
     *
     * @param errorMsg description of the execution failure.
     *
     * @return the resulting job state.
     */
    fun failureOrRetry(errorMsg: String = ""): ScheduledReportJob {
        return if (ScheduledJobReportPolicy.maxedRetries(retryCount)) failure("exceeded retries")
        else retry()
    }

    /**
     * Creates the successful terminal state for this execution.
     *
     * @return a [SuccessfulJob].
     */
    fun success() = SuccessfulJob(
        id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount, startedAt
    )

    /**
     * Creates the failed terminal state for this execution.
     *
     * @param errorMsg reason for the failure.
     * @return a [FailedJob].
     */
    fun failure(errorMsg: String) = FailedJob(
        id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount, startedAt, errorMsg = errorMsg
    )

    /**
     * Schedules this execution to be retried according to
     * [ScheduledJobReportPolicy].
     *
     * @return a new [PendingJob] representing the retry attempt.
     */
    private fun retry() = PendingJob(
        id,
        scheduledReportId,
        dataFrom,
        dataTo,
        scheduledFor,
        retryCount + 1,
        runAt = Instant.now().plus(ScheduledJobReportPolicy.nextRetry(retryCount))
    )

    /**
     * Converts this running state into its persistence representation.
     *
     * @return a running [JobStateEmbeddable].
     */
    override fun getStateEmbeddable(): JobStateEmbeddable =
        JobStateEmbeddable.running(startedAt)

    /**
     * Returns the execution status of this job.
     *
     * @return [JobStatus.RUNNING].
     */
    override fun toStatus(): JobStatus = JobStatus.RUNNING
}

/**
 * `SuccessfulJob`
 *
 * Represents a scheduled report execution that completed successfully.
 *
 * @property id the unique identifier for the [ScheduledReportJobEntity] in the persistence.
 * @property scheduledReportId the unique identifier for the [ScheduledReport] that created this job.
 * @property dataFrom the starting point of the repository data to analyse.
 * @property dataTo the ending point of the repository data to analyse.
 * @property scheduledFor when the report generation was originally scheduled for.
 * @property retryCount how many retries has this job done.
 * @property startedAt when execution began.
 * @property endedAt when execution finished successfully.
 * @throws JobInvalidDateRangeException when data starting point after data ending point
 */
data class SuccessfulJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    val startedAt: Instant,
    val endedAt: Instant = Instant.now()
) : ScheduledReportJob(dataFrom, scheduledFor) {
    /**
     * Converts this successful state into its persistence representation.
     *
     * @return a successful [JobStateEmbeddable].
     */
    override fun getStateEmbeddable(): JobStateEmbeddable =
        JobStateEmbeddable.successful(startedAt, endedAt)

    /**
     * Returns the execution status of this job.
     *
     * @return [JobStatus.SUCCESS].
     */
    override fun toStatus(): JobStatus = JobStatus.SUCCESS
}

/**
 * `FailedJob`
 *
 * Represents a scheduled report execution that terminated unsuccessfully.
 *
 * @property id the unique identifier for the [ScheduledReportJobEntity] in the persistence.
 * @property scheduledReportId the unique identifier for the [ScheduledReport] that created this job.
 * @property dataFrom the starting point of the repository data to analyse.
 * @property dataTo the ending point of the repository data to analyse.
 * @property scheduledFor when the report generation was originally scheduled for.
 * @property retryCount how many retries has this job done.
 * @property startedAt when execution began.
 * @property endedAt when execution finished.
 * @property errorMsg description of the failure.
 * @throws JobInvalidDateRangeException when data starting point after data ending point
 */
data class FailedJob(
    override val id: Int,
    override val scheduledReportId: Int,
    override val dataFrom: Instant,
    override val dataTo: Instant,
    override val scheduledFor: Instant,
    override val retryCount: Int,
    val startedAt: Instant,
    val endedAt: Instant = Instant.now(),
    val errorMsg: String,
) : ScheduledReportJob(dataFrom, scheduledFor) {
    /**
     * Converts this failed state into its persistence representation.
     *
     * @return a failed [JobStateEmbeddable].
     */
    override fun getStateEmbeddable(): JobStateEmbeddable =
        JobStateEmbeddable.failed(startedAt, endedAt, errorMsg)

    /**
     * Returns the execution status of this job.
     *
     * @return [JobStatus.FAILURE].
     */
    override fun toStatus(): JobStatus = JobStatus.FAILURE
}

/**
 * `ScheduledJobReportPolicy`
 *
 * Defines the retry policy for scheduled report executions.
 *
 * The policy specifies the maximum number of retry attempts and the delay
 * between consecutive retries.
 */
data object ScheduledJobReportPolicy {
    /** Maximum number of retry attempts allowed for a job. */
    const val MAX_RETRIES = 2

    /**
     * Returns the delay before the next retry attempt.
     *
     * @param retryCount the number of retries already performed.
     * @return the delay before scheduling the next retry.
     */
    fun nextRetry(retryCount: Int): Duration = when (retryCount) {
        0 -> Duration.ofSeconds(30)
        1 -> Duration.ofMinutes(2)
        2 -> Duration.ofMinutes(5)
        else -> Duration.ofMinutes(Long.MAX_VALUE)
    }

    /**
     * Determines whether the retry limit has been reached.
     *
     * @param attempt the number of retry attempts already performed.
     * @return `true` if no further retries should be scheduled.
     */
    fun maxedRetries(attempt: Int) = attempt >= MAX_RETRIES
}