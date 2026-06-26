package pt.isel.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pt.isel.domain.schedule.CompletedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import pt.isel.repository.interfaces.IScheduledReportRepository
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.flatMap
import pt.isel.utils.success


interface ScheduledReportServiceError: ServiceError

class InvalidScheduledReportDomainArguments(val msg: String) : ScheduledReportServiceError

class ScheduledReportNotFoundException(scheduleId: Int) : Exception("Scheduled report $scheduleId not found")
class ScheduledReportJobNotFoundException(jobId: Int) : Exception("Job $jobId not found")

@Service
class ScheduledReportService(
    private val scheduledReportRepo: IScheduledReportRepository,
    private val userService: UserService,
) {

    fun createScheduledReport(
        dto: CreateScheduleReportDTO<*>, userId: Int
    ): Either<ServiceError, Int> {
        return try {
            userService.findById(userId).flatMap { user ->
                success(scheduledReportRepo.create(dto.toDomain(userId).toEntity(user)).id)
            }
        } catch (e: IllegalArgumentException) {
            failure(InvalidScheduledReportDomainArguments(e.message ?: ""))
        }
    }

    fun getUserScheduledReports(userId: Int): Either<UserNotFound, List<GetScheduledReportDTO>> {
        return userService.findById(userId).flatMap{ user ->
            success(scheduledReportRepo.findByUserId(user.id).map{ it.toDTO() })
        }
    }

    fun getUserScheduledJobs(userId: Int): Either<UserNotFound, List<List<ScheduledReportJob>>> {
        return userService.findById(userId).flatMap{ user ->
            success(scheduledReportRepo.findByUserId(user.id).map{
                schedule -> schedule.jobs.map{ it.toDomain() }
            })
        }
    }

    @Transactional
    fun createScheduledReportJob(scheduleId: Int): PendingJob {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val pendingJob = schedule.toDomain().createJob() //Note: Check if it's needed to handle illegal argument here. Should not be needed since tested on report but best be safe.
        schedule.addJob(pendingJob.toEntity())

        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(scheduleId)

        return pendingJob
    }

    fun listDueJobs() = scheduledReportRepo.findDue().map { Triple(it.id, it.repoUri, it.user.id) }

    @Transactional
    fun calculateNextReport(scheduleId: Int): ScheduledReport<*, *> = updateReport(scheduleId) { schedule ->
        schedule.advanceSchedule()
    }

    @Transactional
    fun updateReportLastRun(completedJob: CompletedJob): ScheduledReport<*, *> =
        updateReport(completedJob.scheduledReportId) { schedule ->
            schedule.recordExecution(completedJob.startedAt)
        }

    @Transactional
    fun cancelReport(scheduleId: Int, errorMsg: String){
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)
        schedule.cancel(errorMsg)
        scheduledReportRepo.update(schedule) ?: throw ScheduledReportNotFoundException(scheduleId)
    }

    @Transactional
    fun runJob(pendingJob: PendingJob): RunningJob = updateJob(pendingJob) { pendingJob.run() }

    @Transactional
    fun endJob(runningJob: RunningJob, isSuccess: Boolean, errorMsg: String = "", allowRetry: Boolean = true) =
        updateJob(runningJob) { runningJob.end(isSuccess, errorMsg, allowRetry) }

    private fun updateReport(
        scheduleId: Int, update: (ScheduledReport<*, *>) -> ScheduledReport<*, *>
    ): ScheduledReport<*, *> {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: throw ScheduledReportNotFoundException(scheduleId)

        val updated = update(schedule.toDomain())

        val entity = updated.toEntity(schedule.user).also { it.jobs = schedule.jobs }

        scheduledReportRepo.update(entity) ?: throw ScheduledReportNotFoundException(scheduleId)

        return updated
    }

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
}