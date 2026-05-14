package pt.isel.repository

import pt.isel.domain.schedule.ScheduledReportJob

interface IScheduledReportJobRepository: IRepository<ScheduledReportJob> {
    fun readIncompleteJobs(): List<ScheduledReportJob>
    fun readAll(): List<ScheduledReportJob>
}