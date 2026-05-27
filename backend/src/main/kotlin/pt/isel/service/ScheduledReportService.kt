package pt.isel.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pt.isel.domain.schedule.CompletedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.model.CreateScheduleReportDTO
import pt.isel.repository.interfaces.IScheduledReportRepository
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success

sealed interface ScheduledReportServiceError

object ScheduledReportNotFound : ScheduledReportServiceError
object InvalidScheduleDomainConstructorParameters: ScheduledReportServiceError
object InvalidJobDomainConstructorParameters: ScheduledReportServiceError
object ScheduledReportJobNotFound: ScheduledReportServiceError
object UserNotFound : ScheduledReportServiceError

@Service
class ScheduledReportService(
    private val scheduledReportRepo: IScheduledReportRepository,
    private val userRepo: IUserRepository,
) {
    fun createScheduledReport(dto: CreateScheduleReportDTO, userId: Int): Either<ScheduledReportServiceError, ScheduledReport> =
        try{
            val user = userRepo.findById(userId) ?: return failure(UserNotFound)
            val result = scheduledReportRepo.create(dto.toDomain().toEntity(user))
            success(result.toDomain())
        } catch (e: IllegalArgumentException) {
            failure(InvalidScheduleDomainConstructorParameters)
        }

    @Transactional
    fun createScheduledReportJob(scheduleId: Int): Either<ScheduledReportServiceError, ScheduledReportJob> {
        try{
            val schedule = scheduledReportRepo.findById(scheduleId) ?: return failure(ScheduledReportNotFound)
            val job = schedule.toDomain().createJob()

            schedule.addJob(job.toEntity())
            scheduledReportRepo.update(schedule) ?: return failure(ScheduledReportNotFound)

            return success(job)
        } catch (e: IllegalArgumentException) {
            return failure(InvalidJobDomainConstructorParameters)
        }
    }

    fun listDueJobs(): List<Int> = scheduledReportRepo.findDue().map{ it.id }

    @Transactional
    fun runJob(pendingJob: PendingJob, scheduleId: Int): Either<ScheduledReportServiceError, ScheduledReportJob>{
        val schedule = scheduledReportRepo.findById(scheduleId) ?: return failure(ScheduledReportNotFound)
        val runningJob = pendingJob.run()

        schedule.updateJob(pendingJob.id){
            it.updateState(runningJob.toEntity().state)
        } ?: return failure(ScheduledReportJobNotFound)
        scheduledReportRepo.update(schedule)

        return success(runningJob)
    }

    @Transactional
    fun endJob(runningJob: RunningJob, scheduleId: Int, isSuccess: Boolean, errorMsg: String = ""): Either<ScheduledReportServiceError, ScheduledReportJob>{
        val schedule = scheduledReportRepo.findById(scheduleId) ?: return failure(ScheduledReportNotFound)
        val endedJob = runningJob.end(isSuccess, errorMsg)

        schedule.updateJob(runningJob.id){
            it.updateState(endedJob.toEntity().state)
        } ?: return failure(ScheduledReportJobNotFound)
        scheduledReportRepo.update(schedule)

        return success(endedJob)
    }


    fun calculateNextReport(completedJob: CompletedJob, scheduleId: Int): Either<ScheduledReportServiceError, ScheduledReport> {
        val schedule = scheduledReportRepo.findById(scheduleId) ?: return failure(ScheduledReportNotFound)
        val updated = schedule.toDomain().completeCurrentExecution(completedJob.endedAt) //TODO: ADD WAY TO REDO JOB IF FAILED

        val entity = updated.toEntity(schedule.user).also{
            it.id = scheduleId
            it.jobs = schedule.jobs
        }

        scheduledReportRepo.update(entity)
        return success(updated)
    }
}