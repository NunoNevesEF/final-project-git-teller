package pt.isel.entity.report.model

import pt.isel.domain.report.schedule.*
import pt.isel.entity.report.model.JobStatus.*
import pt.isel.entity.report.schedule.ScheduledReportJobEntity

/**
 *  `JobStatus`
 *
 * Represents an [ScheduledReportJobEntity]'s current status.
 * Used to map between entity to one of the [ScheduledReportJob]s
 *
 * @property [PENDING] represents a [PendingJob]
 * @property [RUNNING] represents a [RunningJob]
 * @property [SUCCESS] represents a [SuccessfulJob]
 * @property [FAILURE] represents a [FailedJob]
 * */
enum class JobStatus {
    PENDING, RUNNING, SUCCESS, FAILURE
}