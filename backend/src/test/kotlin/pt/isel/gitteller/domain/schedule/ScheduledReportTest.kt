package pt.isel.gitteller.domain.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.utils.CronInput
import pt.isel.utils.YearlyMode
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class ScheduledReportTest<
        DOMAIN: ScheduledReport<DOMAIN, ENTITY>,
        ENTITY: ScheduledReportEntity<ENTITY, DOMAIN>
>{
    val validId = 0

    val validUserId = 0
    val validEmail = "test@email.com"
    val validUserName = "test"

    val validRepoURI = "gitTest.com/user/test"
    val validDataFrom = Instant.now()!!
    val validNextRun = validDataFrom.plus(Duration.ofDays(1))!!

    val validUser = User(validId, validEmail, validUserName)

    abstract fun createScheduledReport(
        id: Int = validId,
        userId: Int = validUserId,
        repoUri: String = validRepoURI,
        nextRun: Instant? = validNextRun,
        lastRun: Instant? = null,
    ): DOMAIN

    abstract fun assertAdvanceScheduled(original: DOMAIN, result: DOMAIN)
    abstract fun assertToEntity(original: DOMAIN, result: ENTITY)

    @Test
    fun `creation fails if id less than 0`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReport(id = -1) }
    }

    @Test
    fun `creation succeeds if id is 0`(){ assertDoesNotThrow { createScheduledReport(id = 0) } }

    @Test
    fun `creation fails userId less than 0`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReport(userId = -1) }
    }

    @Test
    fun `creation succeeds userId is 0`(){ assertDoesNotThrow { createScheduledReport(userId = 0) } }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `creation fails repoUri blank`(repoUri: String) {
        assertFailsWith<IllegalArgumentException> { createScheduledReport(repoUri = repoUri) }
    }

    @Test
    fun `creation succeeds repoUri not blank`(){ assertDoesNotThrow { createScheduledReport(repoUri = "SomeRepoUri") } }

    @Test
    fun `creation fails if dataFrom after nextRun`(){
        assertFailsWith<IllegalArgumentException> {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRunAt = validDataFrom.minusSeconds(100), lastRunAt = null, dataFrom = validDataFrom
            )
        }
    }

    @Test
    fun `creation fails if dataFrom equals nextRun`(){
        assertFailsWith<IllegalArgumentException> {
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
            scheduledFor = testSchedule.nextRunAt!!,
        )
        assertEquals(expected, actual)
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
        val result = original.toEntity(validUser)
        assertToEntity(original, result)
    }
}

class OneTimeScheduledReportTest: ScheduledReportTest<OneTimeScheduledReport, OneTimeScheduledReportEntity>(){
    override fun createScheduledReport(
        id: Int, userId: Int, repoUri: String, nextRun: Instant?, lastRun: Instant?
    ): OneTimeScheduledReport = OneTimeScheduledReport(
        id = id, userId = userId, repoUri = repoUri, nextRunAt = nextRun, lastRunAt = lastRun, dataFrom = validDataFrom
    )

    override fun assertAdvanceScheduled(original: OneTimeScheduledReport, result: OneTimeScheduledReport) {
        assertNull(result.nextRunAt)
    }

    override fun assertToEntity(original: OneTimeScheduledReport, result: OneTimeScheduledReportEntity) {
        assertEquals(original.userId, result.user.id)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
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
            /*id = validId, */userId = validUserId, repoURI = validRepoURI,
            nextRun = validNextRun,
        )
        val after = Instant.now()
        assertTrue(actual.dataFrom in before..after)
    }
}

class PeriodicScheduledReportTest: ScheduledReportTest<PeriodicScheduledReport, PeriodicScheduledReportEntity>(){
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

    override fun assertAdvanceScheduled(original: PeriodicScheduledReport, result: PeriodicScheduledReport) {
        assert(original.nextRunAt <= result.nextRunAt)
        assertEquals(original.nextRunAt, result.dataFrom)
    }

    override fun assertToEntity(original: PeriodicScheduledReport, result: PeriodicScheduledReportEntity) {
        assertEquals(original.userId, result.user.id)
        assertEquals(original.repoUri, result.repoUri)
        assertEquals(original.nextRunAt, result.nextRunAt)
        assertEquals(original.lastRunAt, result.lastRunAt)
        assertEquals(original.dataFrom, result.dataFrom)
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