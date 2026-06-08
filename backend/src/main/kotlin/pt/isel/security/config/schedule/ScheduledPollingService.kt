package pt.isel.security.config.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pt.isel.service.ScheduledReportNotFoundException
import pt.isel.service.ScheduledReportService
import pt.isel.utils.rightOrNull

@Service
class ScheduledPollingService(
    private val scheduledReportService: ScheduledReportService,
    private val scheduledJobExecutor: ScheduledJobExecutor,
) {
    //@Scheduled(cron = "0 0,15,30,45 * * * *")
    @Scheduled(fixedDelay = 6000)
    fun pollSchedules() {
        val schedules = scheduledReportService.listDueJobs()

        schedules.forEach { (scheduleId, repoUri, userId) ->
            try{
                val job = scheduledReportService.createScheduledReportJob(scheduleId)
                scheduledReportService.calculateNextReport(scheduleId)
                scheduledJobExecutor.schedule(job, repoUri, userId)
            } catch(e: ScheduledReportNotFoundException){
                println("Scheduled report has been deleted") //TODO: THINK IF SOMETHING ELSE HAS TO BE DONE.
            }

        }
    }
}

