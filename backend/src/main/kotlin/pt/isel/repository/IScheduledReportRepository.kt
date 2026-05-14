package pt.isel.repository

import pt.isel.domain.schedule.ScheduledReport
import java.time.Instant

interface IScheduledReportRepository: IRepository<ScheduledReport> {
    fun readScheduledReportsByUser(userId: Int): List<ScheduledReport>
    fun readPending(): List<ScheduledReport>
    fun readAll(): List<ScheduledReport>
}