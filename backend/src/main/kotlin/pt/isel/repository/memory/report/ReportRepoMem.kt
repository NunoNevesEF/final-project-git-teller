package pt.isel.repository.memory.report

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.entity.report.Report
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.repository.memory.RepoMem

/**
 *  `ReportRepoMem`
 *
 * Class that implements the in-memory operations of the [Report] entity, also extends CRUD implementation of [RepoMem].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "memory",
    matchIfMissing = true
)
class ReportRepoMem: RepoMem<Report>(), IReportRepository{
    /** @param [userId] The identifier of the [User] who owns this report.
     * @return The list of [Report]s which [userId] corresponds to (can be empty).
     * **/
    override fun findByUserId(userId: Int): List<Report> =
        persistence.values.filter{ report -> report.user.id == userId }
    /** @param [id] the identifier of this report in the persistence.
     * @param [userId] The identifier of the [User] that owns the report. Used to guarantee ownership.
     * @return The [Report] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    override fun findByIdAndUserId(id: Int, userId: Int): Report? =
        persistence.values.firstOrNull { report -> report.user.id == userId && report.id == id }
}