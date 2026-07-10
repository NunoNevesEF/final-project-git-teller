package pt.isel.repository.jpa.report

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.report.Report
import pt.isel.entity.account.User
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.repository.jpa.RepoJpaAdapter

/**
 *  `ReportRepoJpa`
 *
 * Interface that extends the [JpaRepository] with [Report] entity specific operations.
 * The implementation of these is automatically resolved by JPA.
 * */
@Repository
interface ReportRepoJpa : JpaRepository<Report, Int> {
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

/**
 *  `ReportRepoJpaAdapter`
 *
 * Class that implements the jpa operations of the [Report] entity, also extends CRUD implementation of [RepoJpaAdapter].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class ReportRepoJpaAdapter(
    jpa: ReportRepoJpa
) : RepoJpaAdapter<Report, ReportRepoJpa>(jpa), IReportRepository {
    /** @param [userId] The identifier of the [User] who owns this report.
     * @return The list of [Report]s which [userId] corresponds to (can be empty).
     * **/
    override fun findByUserId(userId: Int): List<Report> {
        return jpa.findByUserId(userId)
    }
    /** @param [id] the identifier of this report in the persistence.
     * @param [userId] The identifier of the [User] that owns the report. Used to guarantee ownership.
     * @return The [Report] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    override fun findByIdAndUserId(id: Int, userId: Int): Report? {
        return jpa.findByIdAndUserId(id, userId)
    }
}