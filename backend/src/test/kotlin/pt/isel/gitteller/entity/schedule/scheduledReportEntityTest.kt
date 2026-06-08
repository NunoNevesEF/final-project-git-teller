package pt.isel.gitteller.entity.schedule

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PendingJobStateEmbeddable
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.schedule.RunningJobStateEmbeddable
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

abstract class ScheduledReportEntityTest<DOMAIN : ScheduledReport<DOMAIN, ENTITY>, ENTITY : ScheduledReportEntity<ENTITY, DOMAIN>> {
    val now = Instant.now()

    val validId = 0
    val validRepoURI = "gitTest.com/user/test"
    val validNextRunAt = now
    val validLastRunAt = null
    val validDataFrom = now.minus(Duration.ofDays(1))

    val validUser = User(validId, "test@email.com", "test")

    abstract fun createScheduledReportEntity(
        id: Int = validId,
        repoUri: String = validRepoURI,
        nextRunAt: Instant = validNextRunAt,
        lastRunAt: Instant? = validLastRunAt,
        dataFrom: Instant = validDataFrom,
        user: User = validUser,
    ): ENTITY

    fun createJob(scheduledReport: ENTITY) = ScheduledReportJobEntity(
        validId,
        scheduledReport.dataFrom,
        scheduledReport.nextRunAt!!,
        state = PendingJobStateEmbeddable(scheduledReport.nextRunAt!!)
    )

    abstract fun assertToDomain(original: ENTITY, result: DOMAIN)

    abstract fun activate(original: ENTITY): ENTITY

    abstract fun deactivate(original: ENTITY): ENTITY

    @Test
    fun `method addJob properly adds job and implements relation`() {
        val scheduledReport = createScheduledReportEntity()
        val job = createJob(scheduledReport)
        scheduledReport.addJob(job)

        assertEquals(1, scheduledReport.jobs.size)
        assertSame(job, scheduledReport.jobs.first { it.id == job.id })
        assertSame(scheduledReport, job.scheduledReport)
    }

    @Test
    fun `method updateJob properly updates job`() {
        val scheduledReport = createScheduledReportEntity()
        val job = createJob(scheduledReport)

        val expectedRetryCount = job.retryCount + 1
        val expectedState = RunningJobStateEmbeddable(job.scheduledFor)

        scheduledReport.addJob(job)

        val updatedJob = scheduledReport.updateJob(job.id) {
            it.retryCount++
            it.state = expectedState
            it
        }

        assertNotNull(updatedJob)
        assertEquals(expectedRetryCount, updatedJob!!.retryCount)
        assertEquals(expectedState, updatedJob.state)
        assertSame(updatedJob, scheduledReport.jobs.first { it.id == updatedJob.id })
    }

    @Test
    fun `method updateJob returns null if job id not found`() {
        val report = createScheduledReportEntity()
        val result = report.updateJob(Integer.MAX_VALUE) { it }
        assertNull(result)
    }

    @Test
    fun `method isDue returns true if requirements match`() {
        val report = createScheduledReportEntity()

        activate(report)

        val result = report.isDue(report.nextRunAt!!.plus(Duration.ofMinutes(5)))

        assertTrue(result)
    }

    @Test
    fun `method isDue returns false if schedule isNotActive`() {
        val report = createScheduledReportEntity()
        val limit = report.nextRunAt!!.plus(Duration.ofMinutes(5))

        deactivate(report)

        val result = report.isDue(limit)

        assertFalse(result)
    }

    @Test
    fun `method isDue returns false if job is already scheduled`() {
        val report = createScheduledReportEntity()
        report.addJob(createJob(report))

        activate(report)

        val result = report.isDue(report.nextRunAt!!.plus(Duration.ofMinutes(5)))

        assertFalse(result)
    }

    @Test
    fun `method isDue returns false if nextRun is before limit`() {
        val report = createScheduledReportEntity()

        activate(report)

        val result = report.isDue(report.nextRunAt!!.minus(Duration.ofMinutes(5)))

        assertFalse(result)
    }

    @Test
    fun `method cancel updates entity with isCancelled as true and errorMsg`(){
        val report = createScheduledReportEntity()
        val expectedError = "some error"
        report.cancel(expectedError)
        assertEquals(expectedError, report.cancellationReason)
        assertEquals(true, report.isCancelled)
    }
}

class OneTimeScheduledReportEntityTest :
    ScheduledReportEntityTest<OneTimeScheduledReport, OneTimeScheduledReportEntity>() {
    override fun createScheduledReportEntity(
        id: Int, repoUri: String, nextRunAt: Instant, lastRunAt: Instant?, dataFrom: Instant, user: User
    ): OneTimeScheduledReportEntity {
        val scheduledReport = OneTimeScheduledReportEntity(id, repoUri, nextRunAt, lastRunAt, dataFrom)
        scheduledReport.user = user
        return scheduledReport
    }

    override fun assertToDomain(
        original: OneTimeScheduledReportEntity, result: OneTimeScheduledReport
    ) {
        assertEquals(original.user.id, result.userId)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
    }

    override fun activate(original: OneTimeScheduledReportEntity): OneTimeScheduledReportEntity {
        original.nextRunAt = now
        return original
    }

    override fun deactivate(original: OneTimeScheduledReportEntity): OneTimeScheduledReportEntity {
        original.nextRunAt = null
        return original
    }

}

class PeriodicScheduledReportEntityTest :
    ScheduledReportEntityTest<PeriodicScheduledReport, PeriodicScheduledReportEntity>() {
    val validMinute = 0;
    val validHour = 0
    val validMonth = 1;
    val validDOM = 1

    val validTimezone = "UTC"
    val validCronExpression = "0 $validMinute $validHour $validDOM $validMonth *"

    override fun createScheduledReportEntity(
        id: Int, repoUri: String, nextRunAt: Instant, lastRunAt: Instant?, dataFrom: Instant, user: User
    ): PeriodicScheduledReportEntity {
        val scheduledReport = PeriodicScheduledReportEntity(
            id, repoUri, nextRunAt, lastRunAt, dataFrom, true, validTimezone, validCronExpression
        )
        scheduledReport.user = user
        return scheduledReport
    }

    override fun assertToDomain(
        original: PeriodicScheduledReportEntity, result: PeriodicScheduledReport
    ) {
        assertEquals(original.user.id, result.userId)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.active, result.active)
        assertEquals(original.timeZone, result.timeZone)
        assertEquals(original.cronExpression, result.cronExpression)
    }

    override fun activate(original: PeriodicScheduledReportEntity): PeriodicScheduledReportEntity {
        original.active = true
        return original
    }

    override fun deactivate(original: PeriodicScheduledReportEntity): PeriodicScheduledReportEntity {
        original.active = false
        return original
    }
}