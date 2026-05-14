package pt.isel.security.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.schedule.Pending
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.service.ScheduledReportService
import pt.isel.utils.rightOrNull

@Service
class ScheduledJobExecutor(
    private val taskScheduler: ThreadPoolTaskScheduler,
    private val scheduledReportService: ScheduledReportService,
){
    fun schedule(job: ScheduledReportJob){
        val pendingState = job.state as? Pending ?: return
        taskScheduler.schedule({ execute(job) }, pendingState.scheduledRunAt)
    }

    private fun execute(job: ScheduledReportJob){
        val runningJob = scheduledReportService.runJob(job).rightOrNull() ?: return //TODO: Handle error properly
        try{
            println("Called") //TODO: REPLACE WITH GIT REPO STORE FUNCTION
            val successJob = scheduledReportService.endJob(runningJob, true).rightOrNull() ?: return //TODO: Handle error properly
            scheduledReportService.calculateNextReport(successJob)
        } catch(ex: Exception) {
            val failedJob = scheduledReportService.endJob(runningJob, false).rightOrNull() ?: return //TODO: Handle error properly
            if(failedJob.state is Pending) schedule(failedJob)
            else{ scheduledReportService.calculateNextReport(failedJob) }
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