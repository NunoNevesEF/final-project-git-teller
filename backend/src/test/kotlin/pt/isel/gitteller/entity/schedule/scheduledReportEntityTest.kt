package pt.isel.gitteller.entity.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.domain.report.schedule.OneTimeScheduledReport
import pt.isel.domain.report.schedule.PeriodicScheduledReport
import pt.isel.domain.report.schedule.ScheduledReport
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.JobStateEmbeddable
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs

abstract class ScheduledReportEntityTest<
        ENTITY : ScheduledReportEntity
> {
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
        scheduledReport.nextRunAt!!,
        state = JobStateEmbeddable.pending(scheduledReport.nextRunAt!!)
    )

    abstract fun assertToDomain(original: ENTITY, result: ScheduledReport)

    abstract fun activate(original: ENTITY): ENTITY

    abstract fun deactivate(original: ENTITY): ENTITY


    @Test
    fun `method toDomain maps correctly`() {
        val entity = createScheduledReportEntity()

        val domain = entity.toDomain()

        assertToDomain(entity, domain)
    }

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
        val expectedState = JobStateEmbeddable.running(job.scheduledFor)

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
    fun `method isDue returns false if cancelled`() {
        val report = createScheduledReportEntity()

        report.isCancelled = true

        val result = report.isDue(report.nextRunAt!!.plusSeconds(1))

        assertFalse(result)
    }

    @Test
    fun `getLlmConfig rebuilds wrapper`() {
        val report = createScheduledReportEntity()

        report.llmComplexity = "HIGH"
        report.llmMode = "FULL"
        report.llmAnalyses = "SECURITY,STYLE"

        val cfg = report.getLlmConfig()

        assertNotNull(cfg)
        assertEquals("HIGH", cfg!!.byDetailedSettings!!.promptComplexity)
        assertEquals("FULL", cfg.byDetailedSettings.analysisMode)
        assertEquals(
            listOf("SECURITY", "STYLE"),
            cfg.byDetailedSettings.requestedAnalyses
        )
    }

    @Test
    fun `toDomain maps llm configuration`() {
        val entity = createScheduledReportEntity()

        entity.llmComplexity = "HIGH"
        entity.llmMode = "DIFF"
        entity.llmAnalyses = "DEFAULT,SECURITY"

        val domain = entity.toDomain()

        val cfg = domain.llmConfig!!

        assertEquals("HIGH", cfg.byDetailedSettings!!.promptComplexity)
        assertEquals("DIFF", cfg.byDetailedSettings.analysisMode)
        assertEquals(
            listOf("DEFAULT", "SECURITY"),
            cfg.byDetailedSettings.requestedAnalyses
        )
    }
}

class OneTimeScheduledReportEntityTest : ScheduledReportEntityTest<OneTimeScheduledReportEntity>() {
    override fun createScheduledReportEntity(
        id: Int, repoUri: String, nextRunAt: Instant, lastRunAt: Instant?, dataFrom: Instant, user: User
    ): OneTimeScheduledReportEntity {
        val scheduledReport = OneTimeScheduledReportEntity(id, repoUri, nextRunAt, lastRunAt, dataFrom)
        scheduledReport.user = user
        return scheduledReport
    }

    override fun assertToDomain(
        original: OneTimeScheduledReportEntity, result: ScheduledReport
    ) {
        assertIs<OneTimeScheduledReport>(result)
        assertEquals(original.user.id, result.userId)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.isCancelled, result.isCancelled)
        assertEquals(original.cancellationReason, result.cancellationReason)
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

class PeriodicScheduledReportEntityTest : ScheduledReportEntityTest<PeriodicScheduledReportEntity>() {
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
            id, repoUri, nextRunAt, lastRunAt, dataFrom, false, validTimezone,
            true, validTimezone, validCronExpression
        )
        scheduledReport.user = user
        return scheduledReport
    }

    override fun assertToDomain(
        original: PeriodicScheduledReportEntity, result: ScheduledReport
    ) {
        assertIs<PeriodicScheduledReport>(result)
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