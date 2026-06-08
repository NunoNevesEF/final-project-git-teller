package pt.isel.security.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.service.ScheduledReportService
import pt.isel.service.account.UserNotFound
import pt.isel.service.report.FailureDoNotRetry
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success

@Service
class ScheduledJobExecutor(
    private val taskScheduler: ThreadPoolTaskScheduler,
    private val scheduledReportService: ScheduledReportService,
    private val reportService: UserReportService
){
    fun schedule(job: PendingJob, repoUri: String, userId: Int){
        taskScheduler.schedule({ execute(job, repoUri, userId) }, job.runAt)
    }

    private fun execute(pendingJob: PendingJob, repoUri: String, userId: Int){
        try{
            val runningJob = scheduledReportService.runJob(pendingJob)

            when(val result = reportService.createReport(userId, repoUri)) {
                is Success -> {
                    val successJob = scheduledReportService.endJob(runningJob, true)
                            as? SuccessfulJob ?: throw IllegalStateException("what")
                    scheduledReportService.updateReportLastRun(successJob)
                }

                is Failure -> {
                    when(val error = result.left){
                        is FailureDoNotRetry -> {
                            val failedJob = scheduledReportService.endJob(runningJob, false, error.err.name, false)
                                as? FailedJob ?: throw IllegalStateException("what")
                            scheduledReportService.cancelReport(failedJob.scheduledReportId, error.err.name)
                            scheduledReportService.updateReportLastRun(failedJob)
                        }
                        is UserNotFound -> {
                            return //No need to do anything further. JPA cascade deals with report and job deletion.
                        }
                        else -> {
                            when(val failedJob = scheduledReportService.endJob(runningJob, false)) {
                                is FailedJob -> scheduledReportService.updateReportLastRun(failedJob)
                                is PendingJob -> schedule(failedJob, repoUri, userId)
                                else -> throw IllegalStateException("what")
                            }
                        }
                    }


                }
            }
        } catch(e: Exception) {

        }


    }
}

@Configuration
class SchedulerConfig{
    @Bean
    fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler{
        return ThreadPoolTaskScheduler().apply{
            poolSize = 5
            setWaitForTasksToCompleteOnShutdown(true)
            initialize()
        }
    }
}