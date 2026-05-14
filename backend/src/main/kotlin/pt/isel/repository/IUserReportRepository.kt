package pt.isel.repository

import pt.isel.entity.UserReport

interface IUserReportRepository : IRepository<UserReport> {
    fun findByUserId(userId: Int): List<UserReport>
    fun findByIdAndUserId(reportId: Int, userId: Int): UserReport?
}