package pt.isel.entity.schedule

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.entity.IsEntity
import pt.isel.entity.User
import java.time.Instant

@Entity
@Table(name = "scheduled_reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type")
abstract class ScheduledReportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @Column(name = "repo_uri", nullable = false)
    var repoUri: String = "",

    @Column(name = "next_run", nullable = true)
    var nextRun: Instant?,

    @Column(name = "last_run", nullable = true)
    var lastRun: Instant?,

    @Column(name = "data_start", nullable = false)
    var dataStart: Instant
): IsEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user : User

    @OneToMany(mappedBy = "scheduledReport", cascade = [CascadeType.ALL])
    var jobs: MutableList<ScheduledReportJobEntity> = mutableListOf()

    abstract fun toDomain() : ScheduledReport

    fun addJob(job: ScheduledReportJobEntity) {
        job.scheduledReport = this
        jobs.add(job)
    }

    fun findJob(jobId: Int): ScheduledReportJobEntity? =
        jobs.firstOrNull { it.id == jobId }

    fun updateJob(jobId: Int, update: (ScheduledReportJobEntity) -> ScheduledReportJobEntity): ScheduledReportJobEntity? {
        val job = jobs.firstOrNull { it.id == jobId } ?: return null
        return update(job)
    }

    fun isJobScheduled(scheduledRun: Instant) =
        jobs.any{ it.state.scheduledAt == scheduledRun }

    fun isDue(limit: Instant): Boolean{
        val runAt = nextRun ?: return false
        return runAt <= limit && !isJobScheduled(runAt)
    }


}

@Entity
@DiscriminatorValue("ONE_TIME")
class OneTimeScheduledReportEntity(
    id: Int = 0,
    repoUri: String = "",
    nextRun: Instant? = null,
    lastRun: Instant? = null,
    dataStart: Instant = Instant.now()
) : ScheduledReportEntity(id, repoUri, nextRun, lastRun, dataStart) {
    override fun toDomain(): ScheduledReport =
        OneTimeScheduledReport(
            userId = user.id,
            repoUri = repoUri,
            nextRun = nextRun,
            lastRun = lastRun,
            dataStart = dataStart
        )
}

@Entity
@DiscriminatorValue("PERIODIC")
class PeriodicScheduledReportEntity(
    id: Int = 0,
    repoUri: String,
    nextRun: Instant,
    lastRun: Instant? = null,
    dataStart: Instant,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "timezone", nullable = false)
    var timeZone: String,

    @Column(name = "cron_expression", nullable = false)
    var cronExpression: String
) : ScheduledReportEntity(id, repoUri, nextRun, lastRun, dataStart) {
    override fun toDomain(): ScheduledReport =
        PeriodicScheduledReport(
            userId = user.id,
            repoUri = repoUri,
            nextRun = nextRun!!,
            lastRun = lastRun,
            dataStart = dataStart,
            timeZone = timeZone,
            cronExpression = cronExpression,
            active = active
        )
}