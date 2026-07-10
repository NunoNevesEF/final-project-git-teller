package pt.isel.entity.report.schedule

import jakarta.persistence.*
import pt.isel.domain.report.schedule.*
import pt.isel.entity.report.model.JobStatus
import java.time.Instant

/**
 * `ScheduledReportJobEntity`
 *
 * Persistence representation of a scheduled report execution.
 *
 * Each job corresponds to a single execution attempt generated from a
 * [ScheduledReportEntity]. The entity stores the analysed time window,
 * scheduling information, retry count and current execution state.
 *
 * The execution lifecycle is represented by the embedded
 * [JobStateEmbeddable], allowing the entity to transition between pending,
 * running, successful and failed states.
 *
 * @property id Unique identifier of the job.
 * @property scheduledReport the scheduled report this job was executed from.
 * @property dataFrom Beginning of the repository history analysed by this job.
 * @property dataTo End of the repository history analysed by this job.
 * @property scheduledFor Original execution time of the job.
 * @property retryCount Number of retry attempts already performed.
 * @property state Current execution state and its associated metadata.
 */
@Entity
@Table(name = "scheduled_report_jobs")
class ScheduledReportJobEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "data_from", nullable = false)
    var dataFrom: Instant,

    @Column(name = "data_to", nullable = false)
    var dataTo: Instant,

    @Column(name = "scheduled_for", nullable = false)
    var scheduledFor: Instant,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Embedded
    var state: JobStateEmbeddable = JobStateEmbeddable.pending(scheduledFor),
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_report_id", nullable = false)
    lateinit var scheduledReport: ScheduledReportEntity

    /**
     * Updates the execution state of this scheduled job.
     *
     * @param newState The new execution state.
     * @return This entity after the state has been updated.
     */
    fun updateState(newState: JobStateEmbeddable): ScheduledReportJobEntity {
        state = newState
        return this
    }

    /**
     * Determines whether this job is still awaiting completion.
     *
     * A job is considered queued while it is either pending execution or currently
     * running.
     *
     * @return `true` if the job has not yet reached a terminal state.
     */
    fun isQueued() = state.status == JobStatus.PENDING || state.status == JobStatus.RUNNING

    /**
     * Converts this persistence entity into its corresponding domain model.
     *
     * The concrete domain object created depends on the stored execution status.
     *
     * @return The equivalent [ScheduledReportJob].
     */
    fun toDomain(): ScheduledReportJob {
        return when (state.status) {
            JobStatus.PENDING -> PendingJob(
                id,
                scheduledReport.id,
                dataFrom,
                dataTo,
                scheduledFor,
                retryCount,
                state.runAt!!
            )
            JobStatus.RUNNING -> RunningJob(
                id,
                scheduledReport.id,
                dataFrom,
                dataTo,
                scheduledFor,
                retryCount,
                state.startedAt!!
            )
            JobStatus.SUCCESS -> SuccessfulJob(
                id,
                scheduledReport.id,
                dataFrom,
                dataTo,
                scheduledFor,
                retryCount,
                state.startedAt!!,
                state.endedAt!!
            )
            JobStatus.FAILURE -> FailedJob(
                id,
                scheduledReport.id,
                dataFrom,
                dataTo,
                scheduledFor,
                retryCount,
                state.startedAt!!,
                state.endedAt!!,
                state.errorMsg ?: ""
            )
        }
    }
}

/**
 * `JobStateEmbeddable`
 *
 * Embeddable persistence object describing the current execution state of a
 * [ScheduledReportJobEntity].
 *
 * Only a subset of the stored timestamps is meaningful depending on the
 * current [status]:
 *
 * - [JobStatus.PENDING] uses [runAt].
 * - [JobStatus.RUNNING] uses [startedAt].
 * - [JobStatus.SUCCESS] uses [startedAt] and [endedAt].
 * - [JobStatus.FAILURE] uses [startedAt], [endedAt] and [errorMsg].
 *
 * Factory methods are provided for constructing valid state instances for
 * each lifecycle stage.
 *
 * @property status Current execution status.
 * @property runAt Time when a pending job should be executed.
 * @property startedAt Time when execution started.
 * @property endedAt Time when execution finished.
 * @property errorMsg Failure reason when the execution ends unsuccessfully.
 *
 * @constructor [pending], [running], [successful], [failed] - for each corresponding [ScheduledReportJob] state
 */
@Embeddable
class JobStateEmbeddable(
    @Enumerated(EnumType.STRING)
    var status: JobStatus = JobStatus.PENDING,

    @Column(name = "run_at")
    var runAt: Instant? = null,

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "ended_at")
    var endedAt: Instant? = null,

    @Column(name = "error_msg")
    var errorMsg: String? = null
){
    companion object {
        /**
         * Creates the persistence representation of a pending job state.
         *
         * @param runAt Time when the job should be executed.
         * @return A pending [JobStateEmbeddable].
         */
        fun pending(runAt: Instant) =
            JobStateEmbeddable(JobStatus.PENDING, runAt = runAt)

        /**
         * Creates the persistence representation of a running job state.
         *
         * @param startedAt Time when execution started.
         * @return A running [JobStateEmbeddable].
         */
        fun running(startedAt: Instant) =
            JobStateEmbeddable(JobStatus.RUNNING, startedAt = startedAt)

        /**
         * Creates the persistence representation of a successfully completed job.
         *
         * @param startedAt Time when execution started.
         * @param endedAt Time when execution finished.
         * @return A successful [JobStateEmbeddable].
         */
        fun successful(startedAt: Instant, endedAt: Instant) =
            JobStateEmbeddable(JobStatus.SUCCESS, startedAt = startedAt, endedAt = endedAt)

        /**
         * Creates the persistence representation of a failed job.
         *
         * @param startedAt Time when execution started.
         * @param endedAt Time when execution finished.
         * @param errorMsg Description of the failure.
         * @return A failed [JobStateEmbeddable].
         */
        fun failed(startedAt: Instant, endedAt: Instant, errorMsg: String) =
            JobStateEmbeddable(JobStatus.FAILURE, startedAt = startedAt, endedAt = endedAt, errorMsg = errorMsg)
    }
}