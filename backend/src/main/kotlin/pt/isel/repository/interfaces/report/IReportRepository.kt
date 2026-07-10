package pt.isel.repository.interfaces.report

import pt.isel.entity.report.Report
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.IRepository

/**
 *  `IReportRepository`
 *
 * Interface that establishes the actions upon the [Report] entity, extends the CRUD operations of [IRepository]
 * */
interface IReportRepository: IRepository<Report> {
    /** @param [userId] The identifier of the [User] who owns this report.
     * @return The list of [Report]s which [userId] corresponds to (can be empty).
     * **/
    fun findByUserId(userId: Int): List<Report>
    /** @param [id] the identifier of this report in the persistence.
     * @param [userId] The identifier of the [User] that owns the report. Used to guarantee ownership.
     * @return The [Report] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    fun findByIdAndUserId(id: Int, userId: Int): Report?
}