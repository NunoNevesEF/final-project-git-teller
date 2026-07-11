package pt.isel.gitteller.repository.interfaces.report

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.gitteller.repository.interfaces.RepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IScheduledReportRepository
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@Transactional
abstract class ScheduledReportRepoTest : RepoTest<ScheduledReportEntity> {
    val now = Instant.now()!!

    val validRepoURI = "gitTest.com/user/test"
    val validNextRunAt = now
    val validLastRunAt = now.minus(Duration.ofDays(1))!!
    val validDataFrom = now.minus(Duration.ofDays(1))!!

    val validUser = User(0, "test@email.com", "test")

    abstract override fun repo(): IScheduledReportRepository
    abstract fun userRepo(): IUserRepository

    abstract fun createScheduledReportEntity(
        repoUri: String = validRepoURI,
        nextRunAt: Instant = validNextRunAt,
        lastRunAt: Instant = validLastRunAt,
        dataFrom: Instant = validDataFrom,
        user: User = validUser,
        isCancelled: Boolean = false,
        doAddJob: Boolean = true,
    ): ScheduledReportEntity

    override fun createEntity(): ScheduledReportEntity {
        val user = userRepo().create(validUser)

        return createScheduledReportEntity(user = user)
    }

    abstract override fun updateEntity(entity: ScheduledReportEntity): ScheduledReportEntity

    @Transactional
    override fun assertEquality(expected: ScheduledReportEntity, actual: ScheduledReportEntity?) {
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.user.id, actual?.user?.id)
        assertEquals(expected.repoUri, actual?.repoUri)
        assertEquals(
            expected.nextRunAt?.truncatedTo(ChronoUnit.SECONDS),
            actual?.nextRunAt?.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(
            expected.lastRunAt?.truncatedTo(ChronoUnit.SECONDS),
            actual?.lastRunAt?.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(
            expected.dataFrom.truncatedTo(ChronoUnit.SECONDS),
            actual?.dataFrom?.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(expected.isCancelled, actual?.isCancelled)
        assertEquals(expected.cancellationReason, actual?.cancellationReason)
        assertEquals(expected.llmComplexity, actual?.llmComplexity)
        assertEquals(expected.llmMode, actual?.llmMode)
        assertEquals(expected.llmAnalyses, actual?.llmAnalyses)

        expected.jobs.forEachIndexed { index, job ->
            assertEquals(job.id, actual?.jobs?.get(index)?.id)
        }
    }

    @Test
    fun `method findByIdAndUserId returns correct scheduled report`(){
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected = repo.create(createScheduledReportEntity(user = user))
        val actual = repo.findByIdAndUserId(expected.id, user.id)

        assertEquality(expected, actual)
    }

    @Test
    fun `method findByUserId returns user scheduled reports`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)
        val user2 = userRepo.create(User(email = "some other email", username = "some other username"))

        val expected1 = createScheduledReportEntity(user = user)
        val expected2 = createScheduledReportEntity(user = user)
        val notExpected = createScheduledReportEntity(user = user2)

        val report1 = repo.create(expected1)
        val report2 = repo.create(expected2)
        repo.create(notExpected)

        val expected = listOf(report1, report2)
        val actual = repo.findByUserId(user.id)

        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { index, expectedReport ->
            assertEquality(expectedReport, actual[index])
        }
    }

    @Test
    fun `method findDue returns due reports`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val limit = Instant.now().plus(Duration.ofMinutes(10))

        val scheduledReportDue1 = createScheduledReportEntity(
            user = user,
            nextRunAt = limit.minus(Duration.ofMinutes(1)),
            doAddJob = false
        )
        val scheduledReportDue2 = createScheduledReportEntity(
            user = user,
            nextRunAt = limit.minus(Duration.ofMinutes(13)),
            doAddJob = false
        )
        val scheduledReportNotDue = createScheduledReportEntity(user = user, isCancelled = true, doAddJob = false)

        val report1 = repo.create(scheduledReportDue1)
        val report2 = repo.create(scheduledReportDue2)
        repo.create(scheduledReportNotDue)

        val expected = listOf(report1, report2).sortedBy { it.nextRunAt }
        val actual = repo.findDue().sortedBy{ it.nextRunAt }

        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { index, expectedReport ->
            assertEquality(expectedReport, actual[index])
        }
    }
}