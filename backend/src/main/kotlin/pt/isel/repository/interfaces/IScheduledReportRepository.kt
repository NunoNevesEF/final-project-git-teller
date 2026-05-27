package pt.isel.repository.interfaces

import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportJobEntity

interface IScheduledReportRepository: IRepository<ScheduledReportEntity> {
    fun readScheduledReportsByUser(userId: Int): List<ScheduledReportEntity>
    fun findDue(): List<ScheduledReportEntity>
}