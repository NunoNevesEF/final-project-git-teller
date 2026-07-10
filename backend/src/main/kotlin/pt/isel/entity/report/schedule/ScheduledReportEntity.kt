package pt.isel.entity.report.schedule

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
import pt.isel.domain.report.AnalysisRequestWrapper
import pt.isel.domain.report.CommitDetailedSettingsAnalysisRequest
import pt.isel.domain.report.schedule.OneTimeScheduledReport
import pt.isel.domain.report.schedule.PeriodicScheduledReport
import pt.isel.domain.report.schedule.ScheduledReport
import pt.isel.entity.IsEntity
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.model.scheduledReport.GetOneTimeScheduledReportDTO
import pt.isel.model.scheduledReport.GetPeriodicScheduledReportDTO
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import java.time.Instant

/**
 * `ScheduledReportEntity`
 *
 * Base persistence entity for all scheduled report configurations.
 *
 * Stores the information required to execute scheduled report generations and
 * acts as the persistence counterpart of the [ScheduledReport] domain model.
 *
 * This entity uses joined-table inheritance, allowing specialized scheduled
 * report types to extend the common scheduling data while storing their
 * specific fields in separate tables.
 *
 *
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property user the user generating the report.
 * @property gitAccount the git service [OAuthLinkedAccount] whose tokens are used for private repository access. (null if not using account)
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be and the endpoint of the data to analyse. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * @property llmComplexity Stored LLM prompt complexity configuration.
 * @property llmMode Stored LLM analysis mode.
 * @property llmAnalyses Comma-separated list of requested LLM analyses.
 */
@Entity
@Table(name = "scheduled_reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type")
abstract class ScheduledReportEntity(
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

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "git_account_id", nullable = true)
    var gitAccount: OAuthLinkedAccount? = null

    @OneToMany(mappedBy = "scheduledReport", cascade = [CascadeType.ALL])
    var jobs: MutableList<ScheduledReportJobEntity> = mutableListOf()

    /**
     * Converts this persistence entity into its corresponding domain model.
     *
     * @return The [ScheduledReport] represented by this entity.
     */
    abstract fun toDomain(): ScheduledReport

    /**
     * Converts this persistence entity into its API response DTO.
     *
     * @return The [GetScheduledReportDTO] representation of this scheduled report.
     */
    abstract fun toDTO(): GetScheduledReportDTO

    /**
     * Associates a newly created job with this scheduled report.
     *
     * This method establishes the bidirectional relationship by assigning this
     * scheduled report as the owner of the provided job before adding it to the
     * collection of scheduled jobs.
     *
     * @param job The job to associate with this scheduled report.
     */
    fun addJob(job: ScheduledReportJobEntity) {
        job.scheduledReport = this
        jobs.add(job)
    }

    /**
     * Updates one of this scheduled report's jobs.
     *
     * Searches for a job with the provided identifier and, if found, applies the
     * supplied update function.
     *
     * @param jobId Identifier of the job to update.
     * @param update Function that produces the updated job entity.
     * @return The updated [ScheduledReportJobEntity], or `null` if no matching job
     * exists.
     */
    fun updateJob(
        jobId: Int, update: (ScheduledReportJobEntity) -> ScheduledReportJobEntity
    ): ScheduledReportJobEntity? {
        val job = jobs.firstOrNull { it.id == jobId } ?: return null
        return update(job)
    }

    /**
     * Determines whether this scheduled report should generate a new execution job.
     *
     * A report is considered due when it is active, has no pending or running job
     * already scheduled for its next execution, and its next execution time is
     * before or equal to the provided limit.
     *
     * @param limit Upper bound used when determining whether the schedule is due.
     * @return `true` if a new job should be created; otherwise `false`.
     */
    fun isDue(limit: Instant): Boolean = isActive() && !isJobScheduled() && limit >= nextRunAt

    /**
     * Reconstructs the stored LLM configuration from the persisted values.
     *
     * @return The equivalent [AnalysisRequestWrapper], or `null` if this scheduled
     * report has no LLM configuration.
     */
    fun getLlmConfig(): AnalysisRequestWrapper? {
        if (llmComplexity == null) return null

        return AnalysisRequestWrapper(
            flag = false,
            byShas = null,
            byDetailedSettings = CommitDetailedSettingsAnalysisRequest(
                maxCommits = 20,
                maxCharsPerFile = 5000,
                promptComplexity = llmComplexity!!,
                analysisMode = llmMode ?: "DIFF",
                requestedAnalyses = llmAnalyses?.split(",") ?: listOf("DEFAULT")
            )
        )
    }

    /**Determines if job is already scheduled for current execution**/
    private fun isJobScheduled() = jobs.any { it.scheduledFor == nextRunAt }

    /**
     * Determines whether this scheduled report is currently eligible for execution.
     *
     * Concrete implementations define what "active" means according to their
     * scheduling semantics.
     *
     * @return `true` if the schedule is active; otherwise `false`.
     */
    protected abstract fun isActive(): Boolean
}

/**
 * `OneTimeScheduledReportEntity`
 *
 * Persistence representation of a [OneTimeScheduledReport].
 *
 * Represents a schedule that executes exactly once. After a successful
 * execution its next execution time becomes null, making the schedule inactive.
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property user the user generating the report.
 * @property gitAccount the git service [OAuthLinkedAccount] whose tokens are used for private repository access. (null if not using account)
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be and the endpoint of the data to analyse. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * @property llmComplexity Stored LLM prompt complexity configuration.
 * @property llmMode Stored LLM analysis mode.
 * @property llmAnalyses Comma-separated list of requested LLM analyses.
 */
@Entity
@DiscriminatorValue("ONE_TIME")
class OneTimeScheduledReportEntity(
    id: Int = 0,
    repoUri: String = "",
    nextRunAt: Instant? = null,
    lastRunAt: Instant? = null,
    dataFrom: Instant = Instant.now(),
    isCancelled: Boolean = false,
    cancellationReason: String? = null,
) : ScheduledReportEntity(
    id, repoUri, nextRunAt, lastRunAt, dataFrom, isCancelled, cancellationReason
) {
    /**
     * Converts this persistence entity into its corresponding domain model.
     *
     * @return The [ScheduledReport] represented by this entity.
     */
    override fun toDomain() : ScheduledReport = OneTimeScheduledReport(
        id,
        user.id,
        repoUri,
        nextRunAt,
        lastRunAt,
        dataFrom,
        getLlmConfig(),
        isCancelled,
        cancellationReason
    )

    /**
     * Converts this persistence entity into its API response DTO.
     *
     * @return The [GetOneTimeScheduledReportDTO] representation of this scheduled report.
     */
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

    /**
     * Determines whether this scheduled report is currently eligible for execution.
     *
     * @return `true` if the schedule is active; otherwise `false`.
     */
    override fun isActive(): Boolean = nextRunAt != null && !isCancelled
}

/**
 * `PeriodicScheduledReportEntity`
 *
 * Persistence representation of a [PeriodicScheduledReport].
 *
 * Represents a recurring scheduled report whose execution times are determined
 * by a cron expression and a time zone.
 *
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property user the user generating the report.
 * @property gitAccount the git service [OAuthLinkedAccount] whose tokens are used for private repository access. (null if not using account)
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be and the endpoint of the data to analyse. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * @property llmComplexity Stored LLM prompt complexity configuration.
 * @property llmMode Stored LLM analysis mode.
 * @property llmAnalyses Comma-separated list of requested LLM analyses.
 * @property active Indicates whether the schedule is enabled by the user.
 * Disabled schedules remain persisted but are ignored by the scheduler.
 * @property timeZone IANA timezone used when calculating execution times.
 * @property cronExpression Cron expression describing the execution schedule.
 */
@Entity
@DiscriminatorValue("PERIODIC")
class PeriodicScheduledReportEntity(
    id: Int = 0,
    repoUri: String,
    nextRunAt: Instant,
    lastRunAt: Instant? = null,
    dataFrom: Instant,
    isCancelled: Boolean = false,
    cancellationReason: String? = null,

    @Column(nullable = false) var active: Boolean = true,

    @Column(name = "timezone", nullable = false) var timeZone: String,

    @Column(name = "cron_expression", nullable = false) var cronExpression: String
) : ScheduledReportEntity(
    id, repoUri, nextRunAt, lastRunAt, dataFrom, isCancelled, cancellationReason
) {
    /**
     * Converts this persistence entity into its corresponding domain model.
     *
     * @return The [ScheduledReport] represented by this entity.
     */
    override fun toDomain() = PeriodicScheduledReport(
        id,
        user.id,
        repoUri,
        nextRunAt!!,
        lastRunAt,
        dataFrom,
        isCancelled,
        cancellationReason,
        active,
        timeZone,
        cronExpression,
        getLlmConfig()
    )

    /**
     * Converts this persistence entity into its API response DTO.
     *
     * @return The [GetPeriodicScheduledReportDTO] representation of this scheduled report.
     */
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

    /**
     * Determines whether this scheduled report is currently eligible for execution.
     *
     * @return `true` if the schedule is active; otherwise `false`.
     */
    override fun isActive(): Boolean = active && !isCancelled
}