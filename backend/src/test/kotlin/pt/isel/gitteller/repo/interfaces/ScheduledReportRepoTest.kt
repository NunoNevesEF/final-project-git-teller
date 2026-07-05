package pt.isel.gitteller.repo.interfaces

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.entity.User
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.repository.interfaces.IScheduledReportRepository
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

abstract class ScheduledReportRepoTest {
    val now = Instant.now()

    val validId = 0
    val validRepoURI = "gitTest.com/user/test"
    val validNextRunAt = now
    val validLastRunAt = null
    val validDataFrom = now.minus(Duration.ofDays(1))

    val validUser = User(validId, "test@email.com", "test")

    val validUserId = 0

    abstract fun repo(): IScheduledReportRepository

    abstract fun createScheduledReportEntity(
        id: Int = validId,
        repoUri: String = validRepoURI,
        nextRunAt: Instant? = validNextRunAt,
        lastRunAt: Instant? = validLastRunAt,
        dataFrom: Instant = validDataFrom,
        user: User = validUser,
        active: Boolean = true
    ): ScheduledReportEntity

    @Test
    fun `method findByUserId returns user scheduled reports`() {
        val repo = repo()
        val scheduledReportUser1 = createScheduledReportEntity(user = validUser.copy(id = validUserId))
        val scheduledReportUser2 = createScheduledReportEntity(user = validUser.copy(id = validUserId + 1))

        val report1 = repo.create(scheduledReportUser1)
        val report2 = repo.create(scheduledReportUser1)
        repo.create(scheduledReportUser2)

        val expected = listOf(report1, report2)
        val actual = repo.findByUserId(validUserId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method findDue returns due reports`() {
        val repo = repo()
        val limit = validNextRunAt.plus(Duration.ofMinutes(10))

        val scheduledReportDue1 = createScheduledReportEntity(validUserId, nextRunAt = limit.minus(Duration.ofMinutes(1)))
        val scheduledReportDue2 = createScheduledReportEntity(validUserId + 1, nextRunAt = limit.minus(Duration.ofMinutes(13)))
        val scheduledReportNotDue = createScheduledReportEntity(validUserId, active = false)

        val report1 = repo.create(scheduledReportDue1)
        val report2 = repo.create(scheduledReportDue2)
        repo.create(scheduledReportNotDue)

        val expected = listOf(report1, report2).sortedBy { it.nextRunAt }
        val actual = repo.findDue()

        assertEquals(expected, actual)
    }
}