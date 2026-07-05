package pt.isel.service

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import pt.isel.domain.report.AnalysisRequestWrapper
import pt.isel.entity.schedule.JobStatus
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.model.scheduledReport.DueScheduledJobItem
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import pt.isel.model.scheduledReport.ScheduledReportJobListItemDTO
import pt.isel.repository.interfaces.IScheduledReportRepository
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.flatMap
import pt.isel.utils.success
import java.time.Instant


interface ScheduledReportServiceError: ServiceError

class InvalidScheduledReportDomainArguments(val msg: String) : ScheduledReportServiceError
class ScheduledReportNotFound(): ScheduledReportServiceError
class UnexpectedReportTypeException(val msg: String): ScheduledReportServiceError

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ScheduledReportNotFoundException(scheduleId: Int) : Exception("Scheduled report $scheduleId not found")

class ScheduledReportJobNotFoundException(jobId: Int) : Exception("Job $jobId not found")

class UnexpectedJobTypeException(jobStatus: JobStatus) : Exception("Unexpected job type ${jobStatus.name}")

@Service
class ScheduledReportService(
    private val scheduledReportRepo: IScheduledReportRepository,
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService,
) {

    fun createScheduledReport(
        dto: CreateScheduleReportDTO, userId: Int
    ): Either<ServiceError, Int> {
        return try {
            userService.findById(userId).flatMap { user ->
                val gitAccountId = dto.gitAccountId
                if (gitAccountId == null) {
                    success(scheduledReportRepo.create(dto.toDomain(userId).toEntity(user, null)).id)
                } else {
                    linkedAccountService.findUserOAuthAccount(gitAccountId, userId)
                        .flatMap { account ->
                            success(
                                scheduledReportRepo.create(
                                    dto.toDomain(userId).toEntity(user, account.toEntity(user))
                                ).id
                            )
                        }
                }
            }
        } catch (e: IllegalArgumentException) {
            failure(InvalidScheduledReportDomainArguments(e.message ?: ""))
        }
    }

    fun getUserScheduledReports(userId: Int): List<GetScheduledReportDTO> {
        return scheduledReportRepo.findByUserId(userId).map{ it.toDTO() }
    }

    fun getUserQueuedJobs(userId: Int): List<ScheduledReportJobListItemDTO>{
        val result = scheduledReportRepo.findByUserId(userId).flatMap{ schedule ->
            schedule.jobs.filter{it.isQueued()}.map{ job ->
                ScheduledReportJobListItemDTO.create(job, schedule.repoUri)
            }
        }
        return result
    }

    @Transactional
    fun calculateNextReport(scheduleId: Int) {
        updateReport(scheduleId) { schedule ->
            schedule.advanceSchedule()
        }
    }

    @Transactional
    fun updateReportLastRun(scheduledReportId: Int, startedAt: Instant){
        updateReport(scheduledReportId) { schedule ->
            schedule.recordExecution(startedAt)
        }
    }

    fun resumeReport(scheduledReportId: Int, userId: Int): Either<ScheduledReportServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?:
        return failure(ScheduledReportNotFound())

        if (scheduledReport !is PeriodicScheduledReportEntity)
            return failure(UnexpectedReportTypeException("Only periodic reports can be paused"))

        scheduledReport.active = true

        scheduledReportRepo.update(scheduledReport) ?:
            return failure(ScheduledReportNotFound())

        return success(Unit)
    }

    fun pauseReport(scheduledReportId: Int, userId: Int): Either<ScheduledReportServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?:
            return failure(ScheduledReportNotFound())

        if (scheduledReport !is PeriodicScheduledReportEntity)
            return failure(UnexpectedReportTypeException("Only periodic reports can be paused"))

        scheduledReport.active = false

        scheduledReportRepo.update(scheduledReport) ?:
            return failure(ScheduledReportNotFound())

        return success(Unit)
    }

    fun deleteReport(scheduledReportId: Int, userId: Int): Either<ServiceError, Unit> {
        val scheduledReport = scheduledReportRepo.findByIdAndUserId(scheduledReportId, userId) ?:
            return failure(ScheduledReportNotFound())

        scheduledReportRepo.delete(scheduledReport.id)
        return success(Unit)
    }

    @Transactional
    fun cancelReport(scheduleId: Int, errorMsg: String) {
        updateReport(scheduleId) { schedule ->
            schedule.cancel(errorMsg)
        }
    }

    @Transactional
    fun createScheduledReportJob(scheduleId: Int): PendingJob {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val pendingJobEntity = schedule.toDomain().createJob().toEntity() //Note: Check if it's needed to handle illegal argument here. Should not be needed since tested on report but best be safe.
        schedule.addJob(pendingJobEntity)

        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(scheduleId)

        return pendingJobEntity.toDomain() as? PendingJob ?: throw UnexpectedJobTypeException(pendingJobEntity.state.status)
    }

    fun getScheduleLlmConfig(scheduleId: Int): AnalysisRequestWrapper? =
        scheduledReportRepo.findById(scheduleId)?.getLlmConfig()

    @Transactional
    fun listDueJobs() = scheduledReportRepo.findDue().map {
        DueScheduledJobItem(it.id,it.user.id, it.repoUri, it.gitAccount?.id)
    }

    @Transactional
    fun runJob(pendingJob: PendingJob): RunningJob = updateJob(pendingJob) { pendingJob.run() }

    @Transactional
    fun endJob(runningJob: RunningJob, isSuccess: Boolean, errorMsg: String = "", allowRetry: Boolean = true) =
        updateJob(runningJob) { runningJob.end(isSuccess, errorMsg, allowRetry) }

    private fun updateReport(
        scheduleId: Int, update: (ScheduledReport) -> ScheduledReport
    ): ScheduledReport {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val updated = update(schedule.toDomain())

        val entity = updated.toEntity(schedule.user, schedule.gitAccount).also { it.jobs = schedule.jobs }

        scheduledReportRepo.update(entity) ?: throw ScheduledReportNotFoundException(scheduleId)

        return updated
    }

    private fun <T : ScheduledReportJob> updateJob(job: ScheduledReportJob, update: (ScheduledReportJob) -> T): T {
        val schedule = scheduledReportRepo.findById(job.scheduledReportId)
            ?: throw ScheduledReportNotFoundException(job.scheduledReportId)

        val updated = update(job)

        schedule.updateJob(job.id){
            it.updateState(updated.getStateEmbeddable())
        } ?: throw ScheduledReportJobNotFoundException(job.scheduledReportId)

        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(job.scheduledReportId)

        return updated
    }
}