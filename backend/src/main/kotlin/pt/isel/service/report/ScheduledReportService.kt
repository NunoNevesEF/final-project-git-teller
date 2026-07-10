package pt.isel.service.report

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pt.isel.domain.report.AnalysisRequestWrapper
import pt.isel.domain.report.schedule.FailedJob
import pt.isel.domain.report.schedule.PendingJob
import pt.isel.domain.report.schedule.RunningJob
import pt.isel.domain.report.schedule.ScheduledReport
import pt.isel.domain.report.schedule.ScheduledReportDomainException
import pt.isel.domain.report.schedule.ScheduledReportJob
import pt.isel.domain.report.schedule.SuccessfulJob
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.entity.report.model.JobStatus
import pt.isel.entity.report.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.model.scheduledReport.DueScheduledJobItem
import pt.isel.repository.interfaces.report.IScheduledReportRepository
import pt.isel.service.error.ServiceError
import pt.isel.service.error.ScheduledReportNotFound
import pt.isel.service.error.ScheduledReportServiceError
import pt.isel.service.error.UnexpectedReportType
import pt.isel.service.error.toScheduledReportServiceError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import java.time.Instant
import pt.isel.service.error.InvalidScheduledReportDomainArguments

/** Thrown when no scheduled report with the specified identifier exists. */
class ScheduledReportNotFoundException(scheduleId: Int) : Exception("Scheduled report $scheduleId not found")
/** Thrown when no job with the specified identifier exists. */
class ScheduledReportJobNotFoundException(jobId: Int) : Exception("Job $jobId not found")
/** Thrown when a job has an unexpected status or type. */
class UnexpectedJobTypeException(jobStatus: JobStatus) : Exception("Unexpected job type ${jobStatus.name}")

/**
 * `ScheduledReportService`
 *
 * The service layer responsible for scheduled report-related operations.
 *
 * @property scheduledReportRepo The scheduled report repository used to
 * communicate with the persistence layer.
 */
@Service
class ScheduledReportService(
    private val scheduledReportRepo: IScheduledReportRepository,
) {
    /**
     * Creates a new scheduled report.
     *
     * @param dto The scheduled report creation request.
     * @param user The user who owns the scheduled report.
     * @param gitAccount The Git account associated with the scheduled report, if any.
     *
     * @return
     * - `Unit` on success.
     * - [InvalidScheduledReportDomainArguments] if the supplied arguments are invalid.
     */
    fun createScheduledReport(
        dto: CreateScheduleReportDTO,
        user: User,
        gitAccount: OAuthLinkedAccount?
    ): Either<ScheduledReportServiceError, Unit> {
        try {
            val scheduledReport = dto.toDomain(user.id)
            scheduledReportRepo.create(scheduledReport.toEntity(user, gitAccount))
            return success(Unit)
        } catch (e: ScheduledReportDomainException) {
            return failure(e.toScheduledReportServiceError())
        }
    }

    /**
     * Retrieves all scheduled reports owned by the specified user.
     *
     * @param userId The user's unique identifier.
     * @return The scheduled reports owned by the user.
     */
    fun getUserScheduledReports(userId: Int): List<ScheduledReportEntity> {
        return scheduledReportRepo.findByUserId(userId)
    }

    /**
     * Retrieves all queued scheduled report jobs owned by the specified user.
     *
     * @param userId The user's unique identifier.
     * @return The queued scheduled report jobs.
     */
    fun getUserQueuedJobs(userId: Int): List<ScheduledReportJobEntity> {
        val result = scheduledReportRepo.findByUserId(userId).flatMap { schedule ->
            schedule.jobs.filter { it.isQueued() }
        }
        return result
    }

    /**
     * Resumes a periodic scheduled report.
     *
     * @param scheduledReportId The scheduled report's unique identifier.
     * @param userId The owner's unique identifier.
     *
     * @return
     * - `Unit` on success.
     * - [ScheduledReportNotFound] if no matching scheduled report exists.
     * - [UnexpectedReportType] if the scheduled report cannot be resumed.
     */
    fun resumeReport(scheduledReportId: Int, userId: Int): Either<ScheduledReportServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?: return failure(
            ScheduledReportNotFound
        )

        if (scheduledReport !is PeriodicScheduledReportEntity)
            return failure(UnexpectedReportType)

        scheduledReport.active = true

        scheduledReportRepo.update(scheduledReport) ?: return failure(ScheduledReportNotFound)

        return success(Unit)
    }

    /**
     * Pauses a periodic scheduled report.
     *
     * @param scheduledReportId The scheduled report's unique identifier.
     * @param userId The owner's unique identifier.
     *
     * @return
     * - `Unit` on success.
     * - [ScheduledReportNotFound] if no matching scheduled report exists.
     * - [UnexpectedReportType] if the scheduled report cannot be paused.
     */
    fun pauseReport(scheduledReportId: Int, userId: Int): Either<ScheduledReportServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?: return failure(
            ScheduledReportNotFound
        )

        if (scheduledReport !is PeriodicScheduledReportEntity)
            return failure(UnexpectedReportType)

        scheduledReport.active = false

        scheduledReportRepo.update(scheduledReport) ?: return failure(ScheduledReportNotFound)

        return success(Unit)
    }

    /**
     * Deletes a scheduled report.
     *
     * @param scheduledReportId The scheduled report's unique identifier.
     * @param userId The owner's unique identifier.
     *
     * @return
     * - `Unit` on success.
     * - [ScheduledReportNotFound] if no matching scheduled report exists.
     */
    fun deleteReport(scheduledReportId: Int, userId: Int): Either<ServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?: return failure(
            ScheduledReportNotFound
        )

        scheduledReportRepo.delete(scheduledReport.id)
        return success(Unit)
    }

    /**
     * Advances the schedule to its next execution time.
     *
     * @param scheduleId The scheduled report's unique identifier.
     *
     * @throws ScheduledReportNotFoundException if the scheduled report does not exist.
     */
    @Transactional
    fun calculateNextReport(scheduleId: Int) {
        updateReport(scheduleId) { schedule ->
            schedule.advanceSchedule()
        }
    }

    /**
     * Records the last execution time of a scheduled report.
     *
     * @param scheduledReportId The scheduled report's unique identifier.
     * @param startedAt The execution start time.
     *
     * @throws ScheduledReportNotFoundException if the scheduled report does not exist.
     */
    @Transactional
    fun updateReportLastRun(scheduledReportId: Int, startedAt: Instant) {
        updateReport(scheduledReportId) { schedule ->
            schedule.recordExecution(startedAt)
        }
    }

    /**
     * Cancels a scheduled report.
     *
     * @param scheduleId The scheduled report's unique identifier.
     * @param errorMsg The reason for cancellation.
     *
     * @throws ScheduledReportNotFoundException if the scheduled report does not exist.
     */
    @Transactional
    fun cancelReport(scheduleId: Int, errorMsg: String) {
        updateReport(scheduleId) { schedule ->
            schedule.cancel(errorMsg)
        }
    }

    /**
     * Creates a pending job for a scheduled report.
     *
     * @param scheduleId The scheduled report's unique identifier.
     *
     * @return The newly created pending job.
     *
     * @throws ScheduledReportNotFoundException if the scheduled report does not exist.
     * @throws UnexpectedJobTypeException if the created job is not pending.
     */
    @Transactional
    fun createScheduledReportJob(scheduleId: Int): PendingJob {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val pendingJobEntity = schedule.toDomain().createJob().toEntity()

        schedule.addJob(pendingJobEntity)

        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(scheduleId)

        return pendingJobEntity.toDomain() as? PendingJob
            ?: throw UnexpectedJobTypeException(pendingJobEntity.state.status)
    }

    /**
     * Retrieves all scheduled jobs that are due for execution.
     *
     * @return The due scheduled jobs.
     */
    @Transactional
    fun listDueJobs() = scheduledReportRepo.findDue().map {
        DueScheduledJobItem(it.id, it.user.id, it.repoUri, it.gitAccount?.id)
    }

    /**
     * Marks a pending job as running.
     *
     * @param pendingJob The pending job.
     * @return The running job.
     */
    @Transactional
    fun runJob(pendingJob: PendingJob): RunningJob = updateJob(pendingJob) { pendingJob.run() }

    /**
     * Marks a running job as successfully completed.
     *
     * @param runningJob The running job.
     * @return The completed job.
     */
    @Transactional
    fun endJobSuccess(runningJob: RunningJob) : SuccessfulJob =
        updateJob(runningJob) { runningJob.success() }

    /**
     * Marks a running job as failed.
     *
     * @param runningJob The running job.
     * @param errorMsg The failure reason.
     * @return The failed job.
     */
    @Transactional
    fun endJobFailure(runningJob: RunningJob, errorMsg: String) : FailedJob =
        updateJob(runningJob) { runningJob.failure(errorMsg) }

    /**
     * Fails a running job or schedules it for retry.
     *
     * @param runningJob The running job.
     * @param errorMsg The failure reason.
     * @return The updated job.
     */
    @Transactional
    fun failOrRetryJob(
        runningJob: RunningJob,
        errorMsg: String = ""
    ) = updateJob(runningJob) { runningJob.failureOrRetry(errorMsg) }


    /**
     * Updates the data of a report
     *
     * @param scheduleId The scheduled report's unique identifier.
     * @param update The update to execute
     * @return The updated [ScheduledReport]
     */
    private fun updateReport(
        scheduleId: Int, update: (ScheduledReport) -> ScheduledReport
    ): ScheduledReport {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val updated = update(schedule.toDomain())

        val entity = updated.toEntity(schedule.user, schedule.gitAccount).also { it.jobs = schedule.jobs }

        scheduledReportRepo.update(entity) ?: throw ScheduledReportNotFoundException(scheduleId)

        return updated
    }

    /**
     * Updates the data of a report
     *
     * @param job The job being updated.
     * @param update The update to execute
     * @param T The type of [ScheduledReport]
     * @return The updated [ScheduledReportJob] of type [T]
     */
    private fun <T : ScheduledReportJob> updateJob(job: ScheduledReportJob, update: (ScheduledReportJob) -> T): T {
        val schedule = scheduledReportRepo.findById(job.scheduledReportId)
            ?: throw ScheduledReportNotFoundException(job.scheduledReportId)

        val updated = update(job)

        schedule.updateJob(job.id) {
            it.updateState(updated.getStateEmbeddable())
        } ?: throw ScheduledReportJobNotFoundException(job.scheduledReportId)

        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(job.scheduledReportId)

        return updated
    }

    /**
     * Retrieves the LLM configuration associated with a scheduled report.
     *
     * @param scheduleId The scheduled report's unique identifier.
     * @return The analysis request configuration, or `null` if the scheduled report does not exist.
     */
    fun getScheduleLlmConfig(scheduleId: Int): AnalysisRequestWrapper? =
        scheduledReportRepo.findById(scheduleId)?.getLlmConfig()
}