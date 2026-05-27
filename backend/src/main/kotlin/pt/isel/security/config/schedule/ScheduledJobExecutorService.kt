package pt.isel.security.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.service.ScheduledReportService
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.utils.rightOrNull

@Service
class ScheduledJobExecutor(
    private val taskScheduler: ThreadPoolTaskScheduler,
    private val scheduledReportService: ScheduledReportService,
    private val reportService: UserReportService
){
    fun schedule(job: ScheduledReportJob, userId: Int){
        val pendingJobState = job.state as? PendingJob ?: return //TODO: HANDLE not Pending properly
        taskScheduler.schedule({ execute(job, userId) }, pendingJobState.runAt)
    }

    private fun execute(job: ScheduledReportJob, userId: Int){
        val runningJob = scheduledReportService.runJob(job).rightOrNull() ?: return //TODO: Handle error properly

        when(reportService.createReport(userId, job.repoUri)) {
            is Success -> {
                val successJob = scheduledReportService.endJob(runningJob, true).rightOrNull() ?: return //TODO: Handle error properly
                scheduledReportService.calculateNextReport(successJob)
            }
            is Failure -> {
                val failedJob = scheduledReportService.endJob(runningJob, false).rightOrNull() ?: return //TODO: Handle error properly
                if(failedJob.state is PendingJob) schedule(failedJob)
                else{ scheduledReportService.calculateNextReport(failedJob) }
            }
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