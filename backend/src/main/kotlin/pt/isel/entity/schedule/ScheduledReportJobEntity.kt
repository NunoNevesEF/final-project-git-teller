package pt.isel.entity.schedule

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.SuccessfulJob
import java.time.Instant

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

    fun updateState(newState: JobStateEmbeddable): ScheduledReportJobEntity {
        state = newState
        return this
    }

    fun isQueued() = state.status == JobStatus.PENDING || state.status == JobStatus.RUNNING

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
        fun pending(runAt: Instant) =
            JobStateEmbeddable(JobStatus.PENDING, runAt = runAt)

        fun running(startedAt: Instant) =
            JobStateEmbeddable(JobStatus.RUNNING, startedAt = startedAt)

        fun successful(startedAt: Instant, endedAt: Instant) =
            JobStateEmbeddable(JobStatus.SUCCESS, startedAt = startedAt, endedAt = endedAt)

        fun failed(startedAt: Instant, endedAt: Instant, errorMsg: String) =
            JobStateEmbeddable(JobStatus.FAILURE, startedAt = startedAt, endedAt = endedAt, errorMsg = errorMsg)
    }
}