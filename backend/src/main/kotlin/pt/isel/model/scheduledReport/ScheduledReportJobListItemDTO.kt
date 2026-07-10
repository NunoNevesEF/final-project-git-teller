package pt.isel.model.scheduledReport

import pt.isel.entity.report.model.JobStatus
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import java.time.Instant

data class ScheduledReportJobListItemDTO(
    val dataTo: Instant,
    val dataFrom: Instant,
    val scheduledFor: Instant,
    val repoUri: String,
    val status: JobStatus,
    val retryCount: Int,
){
    companion object{
        fun create(job: ScheduledReportJobEntity, repoUri: String) =
            ScheduledReportJobListItemDTO(
                job.dataTo, job.dataFrom,
                job.scheduledFor, repoUri,
                job.state.status,
                job.retryCount
            )
    }
}