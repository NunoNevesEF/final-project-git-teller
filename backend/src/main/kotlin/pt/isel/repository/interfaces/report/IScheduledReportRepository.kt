package pt.isel.repository.interfaces.report

import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.IRepository

/**
 *  `IScheduledReportRepository`
 *
 * Interface that establishes the actions upon the [ScheduledReportEntity] entity, extends the CRUD operations of [IRepository]
 * */
interface IScheduledReportRepository: IRepository<ScheduledReportEntity> {
    /** @param [id] the identifier of this report in the persistence.
     * @param [userId] The identifier of the [User] that owns the schedule. Used to guarantee ownership.
     * @return The [ScheduledReportEntity] which the [id] + [userId] pair corresponds to or null if none match.
     * **/
    fun findByIdAndUserId(id: Int, userId: Int): ScheduledReportEntity?
    /** @param [userId] The identifier of the [User] who owns this report.
     * @return The list of [ScheduledReportEntity]s which [userId] corresponds to (can be empty).
     * **/
    fun findByUserId(userId: Int): List<ScheduledReportEntity>
    /**@return The list of [ScheduledReportEntity] which fulfill the "due" condition as defined in [ScheduledReportEntity.isDue]**/
    fun findDue(): List<ScheduledReportEntity>
}