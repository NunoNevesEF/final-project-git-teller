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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.entity.IsEntity
import pt.isel.entity.User
import pt.isel.model.scheduledReport.GetOneTimeScheduledReportDTO
import pt.isel.model.scheduledReport.GetPeriodicScheduledReportDTO
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import java.time.Instant
import pt.isel.domain.schedule.LlmScheduleConfig

@Entity
@Table(name = "scheduled_reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type")
abstract class ScheduledReportEntity<SELF : ScheduledReportEntity<SELF, DOMAIN>, DOMAIN : ScheduledReport<DOMAIN, SELF>>(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Int = 0,

    @Column(name = "repo_uri", nullable = false) var repoUri: String = "",

    @Column(name = "next_run", nullable = true) var nextRunAt: Instant?,

    @Column(name = "last_run", nullable = true) var lastRunAt: Instant?,

    @Column(name = "data_start", nullable = false) var dataFrom: Instant,

    @Column(name = "is_cancelled", nullable = false) var isCancelled: Boolean = false,

    @Column(name = "cancellation_reason") var cancellationReason: String? = null,

    @Column(name = "llm_complexity") var llmComplexity: String? = null,

    @Column(name = "llm_mode") var llmMode: String? = null,

    @Column(name = "llm_analyses", length = 500) var llmAnalyses: String? = null
) : IsEntity {
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @OneToMany(mappedBy = "scheduledReport", cascade = [CascadeType.ALL])
    var jobs: MutableList<ScheduledReportJobEntity> = mutableListOf()

    abstract fun toDomain(): DOMAIN

    abstract fun toDTO(): GetScheduledReportDTO

    fun addJob(job: ScheduledReportJobEntity) {
        job.scheduledReport = this
        jobs.add(job)
    }

    fun updateJob(
        jobId: Int, update: (ScheduledReportJobEntity) -> ScheduledReportJobEntity
    ): ScheduledReportJobEntity? {
        val job = jobs.firstOrNull { it.id == jobId } ?: return null
        return update(job)
    }

    fun isDue(limit: Instant): Boolean = isActive() && !isJobScheduled() && limit >= nextRunAt

    fun cancel(errorMsg: String) {
        isCancelled = true
        cancellationReason = errorMsg
    }

    fun getLlmConfig(): LlmScheduleConfig? {
        if (llmComplexity == null) return null
        return LlmScheduleConfig(
            promptComplexity = llmComplexity!!,
            analysisMode = llmMode ?: "DIFF",
            requestedAnalyses = llmAnalyses?.split(",") ?: listOf("DEFAULT")
        )
    }

    private fun isJobScheduled() = jobs.any { it.scheduledFor == nextRunAt }

    protected abstract fun isActive(): Boolean
}

@Entity
@DiscriminatorValue("ONE_TIME")
class OneTimeScheduledReportEntity(
    id: Int = 0,
    repoUri: String = "",
    nextRunAt: Instant? = null,
    lastRunAt: Instant? = null,
    dataFrom: Instant = Instant.now()
) : ScheduledReportEntity<OneTimeScheduledReportEntity, OneTimeScheduledReport>(
    id, repoUri, nextRunAt, lastRunAt, dataFrom
) {
    override fun toDomain() = OneTimeScheduledReport(
        id, user.id, repoUri, nextRunAt, lastRunAt, dataFrom, getLlmConfig()
    )

    override fun toDTO(): GetScheduledReportDTO =
        GetOneTimeScheduledReportDTO(
            id = id,
            repoUri = repoUri,
            nextRunAt = nextRunAt,
            lastRunAt = lastRunAt,
            dataFrom = dataFrom,
            isCancelled = isCancelled,
            cancellationReason = cancellationReason
        )

    override fun isActive(): Boolean = nextRunAt != null && !isCancelled
}

@Entity
@DiscriminatorValue("PERIODIC")
class PeriodicScheduledReportEntity(
    id: Int = 0, repoUri: String, nextRunAt: Instant, lastRunAt: Instant? = null, dataFrom: Instant,

    @Column(nullable = false) var active: Boolean = true,

    @Column(name = "timezone", nullable = false) var timeZone: String,

    @Column(name = "cron_expression", nullable = false) var cronExpression: String
) : ScheduledReportEntity<PeriodicScheduledReportEntity, PeriodicScheduledReport>(
    id, repoUri, nextRunAt, lastRunAt, dataFrom
) {
    override fun toDomain() = PeriodicScheduledReport(
        id, user.id, repoUri, nextRunAt!!, lastRunAt, dataFrom, active, timeZone, cronExpression, getLlmConfig()
    )

    override fun toDTO(): GetScheduledReportDTO =
        GetPeriodicScheduledReportDTO(
            id = id,
            repoUri = repoUri,
            nextRunAt = nextRunAt,
            lastRunAt = lastRunAt,
            dataFrom = dataFrom,
            isCancelled = isCancelled,
            cancellationReason = cancellationReason,
            active = active,
            timeZone = timeZone
        )

    override fun isActive(): Boolean = active && !isCancelled
}