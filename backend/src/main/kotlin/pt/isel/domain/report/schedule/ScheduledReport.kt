package pt.isel.domain.report.schedule

import pt.isel.domain.report.AnalysisRequestWrapper
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.utils.CronInput
import pt.isel.utils.CronUtils
import java.net.URI
import java.net.URISyntaxException
import java.time.Instant


/**
 *  `ScheduledReportDomainException`
 *
 * Represents exceptions that can occur while constructing a [ScheduledReport] domain object.
 *
 * These exceptions are thrown by the domain layer and can be translated into
 * service error objects by the service layer.
 * */
sealed class ScheduledReportDomainException: RuntimeException()
/**Provided repoUri is blank**/
class BlankRepoUriException : ScheduledReportDomainException()
/**Provided repoUri is not a valid uri**/
class MalformedRepoUriException : ScheduledReportDomainException()
/**NextRunAt is before the data collection starting point**/
class ScheduleInvalidDateRangeException : ScheduledReportDomainException()
/**Invalid Job creation attempt**/
class InvalidJobCreationException: ScheduledReportDomainException()

/**
 *  `ScheduledReport`
 *
 * Represents a registration of a scheduled generation of a report.
 *
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property userId the unique identifier of the [User] who scheduled the generation.
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be and the endpoint of the data to analyse. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property llmConfig the configuration for the llm analysis. (null if no llm analysis)
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * */
sealed class ScheduledReport(
    repoUri: String, nextRunAt: Instant?, dataFrom: Instant
) {
    init {
        if(repoUri.isBlank()) throw BlankRepoUriException()
        if(nextRunAt != null && (nextRunAt <= dataFrom)) throw ScheduleInvalidDateRangeException()

        try{
            URI(repoUri) }
        catch(_: URISyntaxException){ throw MalformedRepoUriException() }
    }

    abstract val id: Int
    abstract val userId: Int
    abstract val repoUri: String
    abstract val nextRunAt: Instant?
    abstract val lastRunAt: Instant?
    abstract val dataFrom: Instant
    abstract val llmConfig: AnalysisRequestWrapper?
    abstract val isCancelled: Boolean
    abstract val cancellationReason: String?

    /**Creates a [PendingJob] to execute the next report generation for this schedule
     * @return The [PendingJob] on success
     * @throws InvalidJobCreationException**/
    fun createJob(): PendingJob{
        if(nextRunAt == null || isCancelled) throw ScheduleInvalidDateRangeException()

        return ScheduledReportJob.create(scheduledReportId = id, scheduledFor = nextRunAt!!, dataFrom = dataFrom)
    }

    /** Cancels the schedule because of an error
     * @param errorMsg the message of the error that caused the cancellation.
     * @return The [ScheduledReport] with isCancelled as `true` and with cancellationReason as [errorMsg]**/
    abstract fun cancel(errorMsg: String): ScheduledReport

    /**Advances the schedule to it's state after the current execution
     * @return The [ScheduledReport] with the data ready for the next report generation**/
    abstract fun advanceSchedule(): ScheduledReport

    /**Records the end of a successful execution of the schedule
     * @param jobExecTime when the report generation finished executing
     * @return The [ScheduledReport] with the updated [lastRunAt]**/
    abstract fun recordExecution(jobExecTime: Instant): ScheduledReport

    /**Maps [ScheduledReport] domain to the [ScheduledReportEntity] entity.
     * @param user The [User] who scheduled this generation.
     * @param gitAccount The git provider oauth linked account used for the analysis. Null if no account to be used.
     * @return The [ScheduledReportEntity] equivalent of [ScheduledReport]**/
    abstract fun toEntity(user: User, gitAccount: OAuthLinkedAccount?): ScheduledReportEntity
}

/**
 *  `OneTimeScheduledReport`
 *
 * Represents a registration of a scheduled generation of a report that will execute exactly once.
 *
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property userId the unique identifier of the [User] who scheduled the generation.
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property llmConfig the configuration for the llm analysis. (null if no llm analysis)
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * */
data class OneTimeScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRunAt: Instant?,
    override val lastRunAt: Instant? = null,
    override val dataFrom: Instant,
    override val llmConfig: AnalysisRequestWrapper? = null,

    override val isCancelled: Boolean = false,
    override val cancellationReason: String? = null,
) : ScheduledReport(repoUri, nextRunAt, dataFrom) {
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String, nextRun: Instant,
            dataStart: Instant = Instant.now(), llmConfig: AnalysisRequestWrapper? = null
        ): OneTimeScheduledReport = OneTimeScheduledReport(
            id = id, userId = userId, repoUri = repoURI, nextRunAt = nextRun,
            dataFrom = dataStart, llmConfig = llmConfig
        )
    }

    /** Cancels the schedule because of an error
     * @param errorMsg the message of the error that caused the cancellation.
     * @return The [ScheduledReport] with isCancelled as `true` and with cancellationReason as [errorMsg]**/
    override fun cancel(errorMsg: String): ScheduledReport = this.copy(
        isCancelled = true, cancellationReason = errorMsg
    )

    /**Advances the schedule to it's state after the current execution
     * @return The [ScheduledReport] with the nextRunAt at null to indicate no more executions*/
    override fun advanceSchedule() = copy(nextRunAt = null)

    /**Records the end of a successful execution of the schedule
     * @param jobExecTime when the report generation finished executing
     * @return The [ScheduledReport] with the updated [lastRunAt]**/
    override fun recordExecution(jobExecTime: Instant): OneTimeScheduledReport = copy(lastRunAt = jobExecTime)

    /**Maps [OneTimeScheduledReport] domain to the [OneTimeScheduledReportEntity] entity.
     * @param user The [User] who scheduled this generation.
     * @param gitAccount The git provider oauth linked account used for the analysis. Null if no account to be used.
     * @return The [OneTimeScheduledReportEntity] equivalent of [OneTimeScheduledReport]**/
    override fun toEntity(user: User, gitAccount: OAuthLinkedAccount?) = OneTimeScheduledReportEntity(
        id = id, repoUri = repoUri, nextRunAt = nextRunAt, lastRunAt = lastRunAt, dataFrom = dataFrom,
        isCancelled = isCancelled, cancellationReason = cancellationReason
    ).apply {
        this.user = user
        this.gitAccount = gitAccount
        llmConfig?.let {
            llmComplexity = it.byDetailedSettings?.promptComplexity
            llmMode = it.byDetailedSettings?.analysisMode
            llmAnalyses = it.byDetailedSettings?.requestedAnalyses?.joinToString(",")
        }
    }
}

/**
 *  `OneTimeScheduledReport`
 *
 * Represents a registration of a scheduled generation of a report that will execute periodically.
 *
 * @property id the unique identifier for the [ScheduledReportEntity] in the persistence.
 * @property userId the unique identifier of the [User] who scheduled the generation.
 * @property repoUri the uri identifier of the repository which is being acted upon.
 * @property nextRunAt when the next scheduled generation will be. (null if no more generations).
 * @property lastRunAt when the last report generation was done for this schedule. (null if no prior generation).
 * @property dataFrom the starting point of the repository data to analyse.
 * @property llmConfig the configuration for the llm analysis. (null if no llm analysis)
 * @property isCancelled if the scheduled report generation was cancelled due to an error in execution. (won't execute again)
 * @property cancellationReason what error caused a cancellation of the schedule.
 * @property active if the schedule is active. False indicates it was paused by the user.
 * @property timeZone the timezone of the user who scheduled the generation.
 * @property cronExpression The time expression used to calculate when will be next execution
 * */
data class PeriodicScheduledReport(
    override val id: Int,
    override val userId: Int,
    override val repoUri: String,
    override val nextRunAt: Instant,
    override val lastRunAt: Instant? = null,
    override val dataFrom: Instant,

    override val isCancelled: Boolean = false,
    override val cancellationReason: String? = null,

    val active: Boolean = true,
    val timeZone: String,
    val cronExpression: String,
    override val llmConfig: AnalysisRequestWrapper? = null,
) : ScheduledReport(repoUri, nextRunAt, dataFrom) {
    companion object {
        fun create(
            id: Int = 0, userId: Int, repoURI: String, timeZone: String,
            cronInput: CronInput, llmConfig: AnalysisRequestWrapper? = null,
        ): PeriodicScheduledReport {
            val cronExpression = CronUtils.build(cronInput)
            val nextRun = CronUtils.calculateNext(cronExpression, timeZone)
            return PeriodicScheduledReport(
                id = id,
                userId = userId,
                repoUri = repoURI,
                nextRunAt = nextRun,
                timeZone = timeZone,
                cronExpression = cronExpression,
                dataFrom = CronUtils.calculatePrev(cronInput.mode, timeZone, nextRun),
                llmConfig = llmConfig,
            )
        }
    }

    /** Cancels the schedule because of an error
     * @param errorMsg the message of the error that caused the cancellation.
     * @return The [ScheduledReport] with isCancelled as `true` and with cancellationReason as [errorMsg]**/
    override fun cancel(errorMsg: String): ScheduledReport = this.copy(
        isCancelled = true, cancellationReason = errorMsg
    )

    /**Advances the schedule to it's state after the current execution
     * @return The [ScheduledReport] dataFrom as the current scheduled execution time and nextRunAt as the next calculated run*/
    override fun advanceSchedule() = copy(nextRunAt = calculateNextRunTime(), dataFrom = nextRunAt)

    /**Records the end of a successful execution of the schedule
     * @param jobExecTime when the report generation finished executing
     * @return The [ScheduledReport] with the updated [lastRunAt]**/
    override fun recordExecution(jobExecTime: Instant) = copy(lastRunAt = jobExecTime)

    /**Maps [PeriodicScheduledReport] domain to the [PeriodicScheduledReportEntity] entity.
     * @param user The [User] who scheduled this generation.
     * @param gitAccount The git provider oauth linked account used for the analysis. Null if no account to be used.
     * @return The [PeriodicScheduledReportEntity] equivalent of [PeriodicScheduledReport]**/
    override fun toEntity(user: User, gitAccount: OAuthLinkedAccount?) = PeriodicScheduledReportEntity(
        id = id,
        repoUri = repoUri,
        nextRunAt = nextRunAt,
        lastRunAt = lastRunAt,
        dataFrom = dataFrom,
        active = active,
        timeZone = timeZone,
        cronExpression = cronExpression,
        isCancelled = isCancelled,
        cancellationReason = cancellationReason
    ).also {
        it.user = user
        it.gitAccount = gitAccount
        llmConfig?.let { cfg ->
            it.llmComplexity = cfg.byDetailedSettings?.promptComplexity
            it.llmMode = cfg.byDetailedSettings?.analysisMode
            it.llmAnalyses = cfg.byDetailedSettings?.requestedAnalyses?.joinToString(",")
        }
    }

    fun unpauseSchedule(): Instant{
        TODO(
            "TBD. " +
                    "THIS FUNCTIONS SHOULD UNPAUSE AND DO THE SAME SCHEMA AS START (calc start point + next)" +
                    "However this would require storing frequency mode which is not currently being done."
        )
    }

    /**@return the next instant when the periodic scheduled report is to be executed**/
    private fun calculateNextRunTime(): Instant = CronUtils.calculateNext(
        cronExpression = cronExpression,
        timeZone = timeZone,
        from = nextRunAt.plusSeconds(1) //Start checking from after nextRun.
    )
}
