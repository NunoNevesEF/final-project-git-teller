package pt.isel.service

import org.springframework.stereotype.Service
import pt.isel.domain.schedule.CompletedState
import pt.isel.domain.schedule.Failure
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.Pending
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.Running
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.Success
import pt.isel.model.CreateOneTimeScheduledReportDTO
import pt.isel.model.CreatePeriodicScheduledReportDTO
import pt.isel.model.CreateScheduleReportDTO
import pt.isel.repository.IScheduledReportJobRepository
import pt.isel.repository.IScheduledReportRepository
import pt.isel.utils.CronInput
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.toEither

sealed interface ScheduledReportServiceError
object ScheduledReportNotFound : ScheduledReportServiceError
object InvalidScheduleDomainConstructorParameters: ScheduledReportServiceError
object InvalidJobDomainConstructorParameters: ScheduledReportServiceError
object InvalidJobState: ScheduledReportServiceError

@Service
class ScheduledReportService(
    private val scheduledReportRepo: IScheduledReportRepository,
    private val scheduledReportJobRepo: IScheduledReportJobRepository
) {
    fun createScheduledReport(dto: CreateScheduleReportDTO): Either<InvalidScheduleDomainConstructorParameters, ScheduledReport> =
        try{
            val schedule = when(dto){
                is CreateOneTimeScheduledReportDTO -> createOneTimeScheduledReport(dto)
                is CreatePeriodicScheduledReportDTO -> createPeriodicScheduledReport(dto)
            }
            success(scheduledReportRepo.create(schedule))
        } catch(e: IllegalArgumentException){
            failure(InvalidScheduleDomainConstructorParameters)
        }

    fun createScheduledReportJob(schedule: ScheduledReport): Either<InvalidJobDomainConstructorParameters, ScheduledReportJob> =
        try{
            success(scheduledReportJobRepo.create(schedule.createJob()))
        } catch(e: IllegalArgumentException){
            failure(InvalidJobDomainConstructorParameters)
        }


    private fun createOneTimeScheduledReport(dto: CreateOneTimeScheduledReportDTO): OneTimeScheduledReport =
        OneTimeScheduledReport.create(
            userId = dto.userId, repoURI = dto.repoURI, nextRun = dto.runAt, dataStart = dto.dataStart
        )

    private fun createPeriodicScheduledReport(dto: CreatePeriodicScheduledReportDTO): PeriodicScheduledReport =
        PeriodicScheduledReport.create(
            userId = dto.userId, repoURI = dto.repoURI,
            timeZone = dto.timeZone, cronInput = CronInput(dto.time.minute, dto.time.hour, dto.freqMode)
        )

    fun getAllSchedules(): List<ScheduledReport> = scheduledReportRepo.readAll()

    fun getAllJobs(): List<ScheduledReportJob> = scheduledReportJobRepo.readAll()

    fun getDueSchedules() : List<ScheduledReport>{
        val test = scheduledReportRepo.readPending()
        return test
    }

    fun runJob(pendingJob: ScheduledReportJob): Either<ScheduledReportServiceError, ScheduledReportJob> =
        when(val state = pendingJob.state){
            is Pending -> updateScheduledJob(pendingJob.copy(state = state.run()))
            else -> failure(InvalidJobState)
        }

    fun endJob(pendingJob: ScheduledReportJob, isSuccess: Boolean, errorMsg: String = ""): Either<ScheduledReportServiceError, ScheduledReportJob> =
        when(val state = pendingJob.state){
            is Running -> updateScheduledJob(pendingJob.copy(
                state = state.end(isSuccess, errorMsg))
            )
            else -> failure(InvalidJobState)
        }

    fun calculateNextReport(endedJob: ScheduledReportJob): Either<ScheduledReportServiceError, ScheduledReport> {
        when(val state = endedJob.state){
            is CompletedState -> {
                val schedule = scheduledReportRepo.read(endedJob.id) ?: return failure(ScheduledReportNotFound)
                val updatedSchedule = schedule.completeCurrentExecution(state.startedAt)
                return updateSchedule(updatedSchedule)
            }
            else -> return failure(InvalidJobState)
        }
    }

    private fun updateScheduledJob(scheduledReportJob: ScheduledReportJob) =
        scheduledReportJobRepo.update(scheduledReportJob).toEither{ ScheduledReportNotFound }

    private fun updateSchedule(scheduledReport: ScheduledReport) =
        scheduledReportRepo.update(scheduledReport).toEither{ ScheduledReportNotFound }
}