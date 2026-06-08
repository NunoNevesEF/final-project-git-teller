package pt.isel.entity.schedule

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int = 0,

    @Column(name = "data_from", nullable = false) var dataFrom: Instant,

    @Column(name = "scheduled_for", nullable = false) var scheduledFor: Instant,

    @Column(name = "retry_count", nullable = false) var retryCount: Int = 0,

    @Embedded var state: ScheduledReportJobStateEmbeddable
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_report_id", nullable = false)
    lateinit var scheduledReport: ScheduledReportEntity<*, *>

    fun updateState(newState: ScheduledReportJobStateEmbeddable): ScheduledReportJobEntity {
        state = newState
        return this
    }

    fun toDomain() = state.toDomain(
        id = id,
        scheduledReportId = scheduledReport.id,
        dataFrom = dataFrom,
        scheduledFor = scheduledFor,
        retryCount = retryCount,
    )
}

@Embeddable
@DiscriminatorColumn(name = "state")
abstract class ScheduledReportJobStateEmbeddable() {
    abstract fun toDomain(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant, retryCount: Int
    ): ScheduledReportJob
}

@Embeddable
@DiscriminatorValue("PENDING")
class PendingJobStateEmbeddable(
    @Column(name = "run_at") var runAt: Instant
) : ScheduledReportJobStateEmbeddable() {
    override fun toDomain(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant, retryCount: Int
    ) = PendingJob(
        id, scheduledReportId, dataFrom, scheduledFor, retryCount, runAt
    )
}

@Embeddable
@DiscriminatorValue("RUNNING")
class RunningJobStateEmbeddable(
    @Column(name = "started_at") var startedAt: Instant,
) : ScheduledReportJobStateEmbeddable() {
    override fun toDomain(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant, retryCount: Int
    ) = RunningJob(
        id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt
    )
}

@Embeddable
@DiscriminatorValue("SUCCESS")
class SuccessfulJobStateEmbeddable(
    @Column(name = "started_at") var startedAt: Instant,
    @Column(name = "ended_at") var endedAt: Instant,
) : ScheduledReportJobStateEmbeddable() {
    override fun toDomain(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant, retryCount: Int
    ) = SuccessfulJob(
        id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt, endedAt
    )
}

@Embeddable
@DiscriminatorValue("FAILURE")
class FailedJobStateEmbeddable(
    @Column(name = "started_at") var startedAt: Instant,
    @Column(name = "ended_at") var endedAt: Instant,
    @Column(name = "error_msg") var errorMsg: String
) : ScheduledReportJobStateEmbeddable() {
    override fun toDomain(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant, retryCount: Int
    ) = FailedJob(
        id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt, endedAt, errorMsg
    )
}


