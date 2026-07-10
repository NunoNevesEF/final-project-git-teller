package pt.isel.repository.jpa.report

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.report.IScheduledReportRepository
import pt.isel.repository.jpa.RepoJpaAdapter
import java.time.Duration
import java.time.Instant

/**
 *  `ScheduledReportRepoJpa`
 *
 * Interface that extends the [JpaRepository] with [ScheduledReportEntity] entity specific operations.
 * The implementation of these is automatically resolved by JPA.
 * */
@Repository
interface ScheduledReportRepoJpa : JpaRepository<ScheduledReportEntity, Int>{
    /** @param [id] the identifier of this report in the persistence.
     * @param [userId] The identifier of the [User] that owns the schedule. Used to guarantee ownership.
     * @return The [ScheduledReportEntity] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    fun findByIdAndUserId(id : Int, userId : Int) : ScheduledReportEntity?
    /** @param [userId] The identifier of the [User] who owns this report.
     * @return The list of [ScheduledReportEntity]s which [userId] corresponds to (can be empty).
     * **/
    fun findByUserId(userId: Int): List<ScheduledReportEntity>
}

/**
 *  `ScheduledReportRepoJpaAdapter`
 *
 * Class that implements the jpa operations of the [ScheduledReportEntity] entity, also extends CRUD implementation of [RepoJpaAdapter].
 * */
@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class ScheduledReportRepoJpaAdapter(
    jpa: ScheduledReportRepoJpa
) : RepoJpaAdapter<ScheduledReportEntity, ScheduledReportRepoJpa>(jpa), IScheduledReportRepository {
    override fun findByIdAndUserId(id: Int, userId: Int): ScheduledReportEntity? =
        jpa.findByIdAndUserId(id, userId)

    override fun findByUserId(userId: Int): List<ScheduledReportEntity> =
        jpa.findByUserId(userId)

    override fun findDue(): List<ScheduledReportEntity>{ //TODO: CAN BE PROBABLY BE IMPLEMENTED WITH BETTER EFFICIENCY USING A QUERY TO CHECK FOR LIMIT
        val limit = Instant.now().plus(Duration.ofMinutes(15))
        return jpa.findAll()
            .filter{ it.isDue(limit) }
            .sortedBy{ it.nextRunAt }
    }
}