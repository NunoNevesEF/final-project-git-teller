package pt.isel.repository.interfaces

import pt.isel.entity.ReportEntity

interface IUserReportRepository: IRepository<ReportEntity> {
    fun findByUserId(userId: Int): List<ReportEntity>
    fun findByIdAndUserId(id: Int, userId: Int): ReportEntity?
}