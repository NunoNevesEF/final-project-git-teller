package pt.isel.security.config.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
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
        val schedules = scheduledReportService.getDueSchedules()

        schedules.forEach { schedule ->
            val job = scheduledReportService.createScheduledReportJob(schedule).rightOrNull()
            if(job != null) scheduledJobExecutor.schedule(job) //TODO: Handle error
        }
    }
}

