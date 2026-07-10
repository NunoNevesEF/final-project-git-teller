package pt.isel.infraestructure.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pt.isel.service.report.ScheduledReportNotFoundException
import pt.isel.service.report.ScheduledReportService

//TODO: DOCUMENT

@Service
class ScheduledPollingService(
    private val scheduledReportService: ScheduledReportService,
    private val scheduledJobExecutor: ScheduledJobExecutor,
) {
    //@Scheduled(cron = "0 0,15,30,45 * * * *")
    @Scheduled(fixedDelay = 6000)
    fun pollSchedules() {
        val schedules = scheduledReportService.listDueJobs()

        schedules.forEach { (scheduleId, userId, repoUri, gitAccountId) ->
            try{
                val job = scheduledReportService.createScheduledReportJob(scheduleId)
                scheduledReportService.calculateNextReport(scheduleId)
                scheduledJobExecutor.schedule(job, userId, repoUri, gitAccountId)
            } catch(_: ScheduledReportNotFoundException){
                println("Scheduled report has been deleted") //TODO: THINK IF SOMETHING ELSE HAS TO BE DONE.
            }

        }
    }
}

