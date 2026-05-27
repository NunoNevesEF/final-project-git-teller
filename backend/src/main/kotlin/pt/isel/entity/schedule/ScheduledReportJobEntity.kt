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
import pt.isel.domain.schedule.SuccessfulJob
import java.time.Instant

@Entity
@Table(name = "scheduled_report_jobs")
class ScheduledReportJobEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "repo_uri", nullable = false)
    var repoUri: String,

    @Column(name = "data_from", nullable = false)
    var dataFrom: Instant,

    @Column(name = "data_to", nullable = false)
    var dataTo: Instant,

    @Embedded
    var state: ScheduledReportJobStateEmbeddable = ScheduledReportJobStateEmbeddable()
){
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_report_id", nullable = false)
    lateinit var scheduledReport: ScheduledReportEntity

    fun updateState(newState: ScheduledReportJobStateEmbeddable): ScheduledReportJobEntity {
        state = newState
        return this
    }

    fun toDomain() =
        state.toDomain(
            id = id,
            repoUri = repoUri,
            dataFrom = dataFrom,
            dataTo = dataTo
        )
}

@Embeddable
class ScheduledReportJobStateEmbeddable(
    @Enumerated(EnumType.STRING)
    var type: ScheduledReportJobStateEnum = ScheduledReportJobStateEnum.PENDING,
    var retryCount: Int = 1,
    var scheduledAt: Instant = Instant.now(),
    var runAt: Instant? = null,
    var startedAt: Instant? = null,
    var endedAt: Instant? = null,
    var errorMsg: String = ""
){
    fun toDomain(id: Int, repoUri: String, dataFrom: Instant, dataTo: Instant) =
        when(type){
            ScheduledReportJobStateEnum.PENDING ->
                PendingJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, runAt!!)
            ScheduledReportJobStateEnum.RUNNING ->
                RunningJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, startedAt!!)
            ScheduledReportJobStateEnum.SUCCESS ->
                SuccessfulJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, startedAt!!, endedAt!!)
            ScheduledReportJobStateEnum.FAILURE ->
                FailedJob(id, repoUri, dataFrom, dataTo, retryCount, scheduledAt, startedAt!!, endedAt!!, errorMsg)
        }
}

enum class ScheduledReportJobStateEnum {
    PENDING, RUNNING, SUCCESS, FAILURE;
}
