package pt.isel.gitteller.domain.report.schedule

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.domain.report.schedule.BlankRepoUriException
import pt.isel.domain.report.schedule.ScheduleInvalidDateRangeException
import pt.isel.domain.report.schedule.MalformedRepoUriException
import pt.isel.domain.report.schedule.OneTimeScheduledReport
import pt.isel.domain.report.schedule.PendingJob
import pt.isel.domain.report.schedule.PeriodicScheduledReport
import pt.isel.domain.report.schedule.ScheduledReport
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.utils.CronInput
import pt.isel.utils.YearlyMode
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

abstract class ScheduledReportTest{
    val validId = 0

    val validUserId = 0
    val validEmail = "test@email.com"
    val validUsername = "test"

    val validRepoURI = "https://gitTest.com/user/test"
    val validDataFrom = Instant.now()!!
    val validNextRun = validDataFrom.plus(Duration.ofDays(1))!!

    val validUser = User(validId, validEmail, validUsername)

    abstract fun createScheduledReport(
        id: Int = validId,
        userId: Int = validUserId,
        repoUri: String = validRepoURI,
        nextRun: Instant? = validNextRun,
        lastRun: Instant? = null,
    ): ScheduledReport

    abstract fun assertAdvanceScheduled(original: ScheduledReport, result: ScheduledReport)
    abstract fun assertToEntity(original: ScheduledReport, result: ScheduledReportEntity)

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails repoUri blank`(repoUri: String) {
        assertFailsWith<BlankRepoUriException> { createScheduledReport(repoUri = repoUri) }
    }

    @Test
    fun `creation fails malformed repo uri`() {
        assertFailsWith<MalformedRepoUriException> {
            createScheduledReport(repoUri = "not a uri")
        }
    }

    @Test
    fun `creation succeeds repoUri not blank`(){ assertDoesNotThrow { createScheduledReport(repoUri = "SomeRepoUri") } }

    @Test
    fun `creation fails if dataFrom after nextRun`(){
        assertFailsWith<ScheduleInvalidDateRangeException> {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRunAt = validDataFrom.minusSeconds(100), lastRunAt = null, dataFrom = validDataFrom
            )
        }
    }

    @Test
    fun `creation fails if dataFrom equals nextRun`(){
        assertFailsWith<ScheduleInvalidDateRangeException> {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRunAt = validDataFrom, lastRunAt = null, dataFrom = validDataFrom
            )
        }
    }

    @Test
    fun `creation succeeds if dataFrom before nextRun`(){
        assertDoesNotThrow {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRunAt = validDataFrom.plusSeconds(100), lastRunAt = null, dataFrom = validDataFrom
            )
        }
    }

    @Test
    fun `method createJob properly creates Pending job`(){
        val testSchedule = createScheduledReport()

        val actual = testSchedule.createJob()
        val expected = PendingJob(
            id = actual.id,
            scheduledReportId = testSchedule.id,
            dataFrom = testSchedule.dataFrom,
            dataTo = testSchedule.nextRunAt!!,
            scheduledFor = testSchedule.nextRunAt!!,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `method createJob fails if schedule is cancelled`() {
        val report = createScheduledReport().cancel("failure")

        assertFailsWith<ScheduleInvalidDateRangeException> {
            report.createJob()
        }
    }

    @Test
    fun `method cancel marks report as cancelled`() {
        val cancelled = createScheduledReport().cancel("boom")

        assertTrue(cancelled.isCancelled)
        assertEquals("boom", cancelled.cancellationReason)
    }

    @Test
    fun `method advanceSchedule properly creates next state`(){
        val original = createScheduledReport()
        val result = original.advanceSchedule()
        assertAdvanceScheduled(original, result)
    }

    @Test
    fun `method recordExecution properly updates lastRunAt`(){
        val execTime = Instant.now().plus(Duration.ofDays(1))
        val actual = createScheduledReport().recordExecution(execTime)
        assertEquals(execTime, actual.lastRunAt)
    }

    @Test
    fun `method toEntity properly creates Entity`(){
        val original = createScheduledReport()
        val result = original.toEntity(validUser, null)
        assertToEntity(original, result)
    }
}

class OneTimeScheduledReportTest: ScheduledReportTest(){
    override fun createScheduledReport(
        id: Int, userId: Int, repoUri: String, nextRun: Instant?, lastRun: Instant?
    ): OneTimeScheduledReport = OneTimeScheduledReport(
        id = id, userId = userId, repoUri = repoUri, nextRunAt = nextRun, lastRunAt = lastRun, dataFrom = validDataFrom
    )

    override fun assertAdvanceScheduled(original: ScheduledReport, result: ScheduledReport) {
        assertIs<OneTimeScheduledReport>(original)
        assertIs<OneTimeScheduledReport>(result)
        assertNull(result.nextRunAt)
    }

    override fun assertToEntity(original: ScheduledReport, result: ScheduledReportEntity) {
        assertIs<OneTimeScheduledReport>(original)
        assertIs<OneTimeScheduledReportEntity>(result)
        assertEquals(original.userId, result.user.id)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.llmConfig?.byDetailedSettings?.promptComplexity, result.llmComplexity)
        assertEquals(original.llmConfig?.byDetailedSettings?.analysisMode, result.llmMode)
        assertEquals(original.llmConfig?.byDetailedSettings?.requestedAnalyses?.joinToString(","), result.llmAnalyses)
        assertEquals(original.isCancelled, result.isCancelled)
        assertEquals(original.cancellationReason, result.cancellationReason)
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = OneTimeScheduledReport.create(
            userId = validUserId, repoURI = validRepoURI,
            nextRun = validNextRun, dataStart = validDataFrom
        )
        val expected = createScheduledReport(id = 0)
        assertEquals(expected, actual)
    }

    @Test
    fun `companion method create defaults dataFrom to now if not passed`(){
        val before = Instant.now()
        val actual = OneTimeScheduledReport.create(
            id = validId, userId = validUserId, repoURI = validRepoURI,
            nextRun = validNextRun,
        )
        val after = Instant.now()
        assertTrue(actual.dataFrom in before..after)
    }

    @Test
    fun `method createJob fails if nextRunAt is null`() {
        val report = createScheduledReport(nextRun = null)

        assertFailsWith<ScheduleInvalidDateRangeException> {
            report.createJob()
        }
    }
}

class PeriodicScheduledReportTest: ScheduledReportTest(){
    val validMinute = 0; val validHour = 0
    val validMonth = 1; val validDOM = 1
    val validFreqMode = YearlyMode(validDOM, validMonth)

    val validTimezone = "UTC"
    val validCronInput = CronInput(minute = validMinute, hour = validHour, validFreqMode)
    val validCronExpression = "0 $validMinute $validHour $validDOM $validMonth *"

    override fun createScheduledReport(
        id: Int, userId: Int, repoUri: String, nextRun: Instant?, lastRun: Instant?
    ): PeriodicScheduledReport = PeriodicScheduledReport(
        id = id, userId = userId, repoUri = repoUri,
        nextRunAt = nextRun!!, lastRunAt = lastRun, dataFrom = validDataFrom,
        cronExpression = validCronExpression, timeZone = validTimezone,
    )

    override fun assertAdvanceScheduled(original: ScheduledReport, result: ScheduledReport) {
        assertIs<PeriodicScheduledReport>(original)
        assertIs<PeriodicScheduledReport>(result)
        assert(original.nextRunAt <= result.nextRunAt)
        assertEquals(original.nextRunAt, result.dataFrom)
    }

    override fun assertToEntity(original: ScheduledReport, result: ScheduledReportEntity) {
        assertEquals(original.userId, result.user.id)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.llmConfig?.byDetailedSettings?.promptComplexity, result.llmComplexity)
        assertEquals(original.llmConfig?.byDetailedSettings?.analysisMode, result.llmMode)
        assertEquals(original.llmConfig?.byDetailedSettings?.requestedAnalyses?.joinToString(","), result.llmAnalyses)
        assertEquals(original.isCancelled, result.isCancelled)
        assertEquals(original.cancellationReason, result.cancellationReason)

        assertIs<PeriodicScheduledReport>(original)
        assertIs<PeriodicScheduledReportEntity>(result)

        assertEquals(original.active, result.active)
        assertEquals(original.timeZone, result.timeZone)
        assertEquals(original.cronExpression, result.cronExpression)
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = PeriodicScheduledReport.create(
            userId = validUserId, repoURI = validRepoURI,
            timeZone = validTimezone, cronInput = validCronInput
        )
        val expected = createScheduledReport(id = 0).copy(nextRunAt = actual.nextRunAt, dataFrom = actual.dataFrom)
        assertEquals(expected, actual)
    }
}