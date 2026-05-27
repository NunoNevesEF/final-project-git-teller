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
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.utils.CronInput
import pt.isel.utils.YearlyMode
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class ScheduledReportTest<T: ScheduledReport> {
    val validId = 0
    val validUserId = 0
    val validRepoURI = "gitTest.com/user/test"
    val validDataStart = Instant.now()
    val validNextRun = validDataStart.plus(Duration.ofDays(1))

    abstract fun createScheduledReport(
        id: Int = validId,
        userId: Int = validUserId,
        repoUri: String = validRepoURI,
        nextRun: Instant? = validNextRun,
        lastRun: Instant? = null,
    ): T

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
    fun `method createJob properly creates job`(){
        val testSchedule = createScheduledReport()

        val actual = testSchedule.createJob()
        val expected = ScheduledReportJob(
            id = actual.id,
            scheduledReportId = testSchedule.id,
            state = PendingJob(runAt = testSchedule.nextRun!!, retryCount = 1),
            repoUri = testSchedule.repoUri,
            dataFrom = testSchedule.dataStart,
            dataTo = testSchedule.nextRun!!
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `creation fails if dataStart after nextRun`(){
        assertFailsWith<IllegalArgumentException> {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRun = validDataStart.minusSeconds(100), lastRun = null, dataStart = validDataStart
            )
        }
    }

    @Test
    fun `creation fails if dataStart equals nextRun`(){
        assertFailsWith<IllegalArgumentException> {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRun = validDataStart, lastRun = null, dataStart = validDataStart
            )
        }
    }

    @Test
    fun `creation succeeds if dataStart after nextRun`(){
        assertDoesNotThrow {
            OneTimeScheduledReport(
                id = validId, userId = validUserId, repoUri = validRepoURI,
                nextRun = validDataStart.plusSeconds(100), lastRun = null, dataStart = validDataStart
            )
        }
    }
}

class OneTimeScheduledReportTest: ScheduledReportTest<OneTimeScheduledReport>(){
    override fun createScheduledReport(
        id: Int, userId: Int, repoUri: String, nextRun: Instant?, lastRun: Instant?
    ): OneTimeScheduledReport = OneTimeScheduledReport(
        id = id, userId = userId, repoUri = repoUri, nextRun = nextRun, lastRun = lastRun, dataStart = validDataStart
    )

    @Test
    fun `creation fails if both nextRun and lastRun are null`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReport(nextRun = null, lastRun = null) }
    }

    @Test
    fun `creation fails if both nextRun and lastRun are not null`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReport(nextRun = validNextRun, lastRun = validNextRun) }
    }

    @Test
    fun `creation succeeds if nextRun is not null and last run is null`(){ //Before run scenario
        assertDoesNotThrow { createScheduledReport(nextRun = validNextRun, lastRun = null) }
    }

    @Test
    fun `creation succeeds if nextRun is null and lastRun are not null`(){ //After run scenario
        assertDoesNotThrow  { createScheduledReport(nextRun = null, lastRun = validNextRun) }
    }

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = OneTimeScheduledReport.create(
            userId = validUserId, repoURI = validRepoURI,
            nextRun = validNextRun, dataStart = validDataStart
        )
        val expected = createScheduledReport(id = 0)
        assertEquals(expected, actual)
    }

    @Test
    fun `companion method create defaults dataStart to now if not passed`(){
        val before = Instant.now()
        val actual = OneTimeScheduledReport.create(
            id = validId, userId = validUserId, repoURI = validRepoURI,
            nextRun = validNextRun,
        )
        val after = Instant.now()
        assertTrue(actual.dataStart in before..after)
    }

    @Test
    fun `method completeCurrentExecution returns object with nextRun as null and lastRun as passed exec time`(){
        val testSchedule = createScheduledReport()
        val expectExecTime = testSchedule.nextRun!!.plusSeconds(15)
        val actual = testSchedule.completeCurrentExecution(expectExecTime)
        assertNull(actual.nextRun)
        assertEquals(expectExecTime, actual.lastRun)
    }

    @Test
    fun `method scheduledReportCopy returns object with updated id`(){
        val testSchedule = createScheduledReport()
        val newId = Integer.MAX_VALUE
        val actual = testSchedule.scheduledReportCopy(id = newId)
        val expected = testSchedule.copy(id = newId)
        assertEquals(expected, actual)
    }

    @Test
    fun `method createJob fails if job already completed`(){ //nextRun == null
        val testSchedule = createScheduledReport(nextRun = null, lastRun = validNextRun)
        assertFailsWith<IllegalArgumentException> { testSchedule.createJob() }
    }
}

class PeriodicScheduledReportTest: ScheduledReportTest<PeriodicScheduledReport>(){
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
        nextRun = nextRun!!, lastRun = lastRun, dataStart = validDataStart,
        cronExpression = validCronExpression, timeZone = validTimezone,
    )

    @Test
    fun `companion method create defaults id to 0 if not passed`(){
        val actual = PeriodicScheduledReport.create(
            userId = validUserId, repoURI = validRepoURI,
            timeZone = validTimezone, cronInput = validCronInput
        )
        val expected = createScheduledReport(id = 0).copy(nextRun = actual.nextRun, dataStart = actual.dataStart)
        assertEquals(expected, actual)
    }

    @Test
    fun `method completeCurrentExecution returns object with calculated nextRun passed execTime as lastRun and dataStart as old nextRun`(){
        val testSchedule = createScheduledReport()
        val expectExecTime = testSchedule.nextRun.plusSeconds(15)
        val actual = testSchedule.completeCurrentExecution(expectExecTime)

        assert(testSchedule.nextRun < actual.nextRun)
        assertEquals(expectExecTime, actual.lastRun)
        assertEquals(testSchedule.nextRun, actual.dataStart)
    }

    @Test
    fun `method scheduledReportCopy returns object with updated id`(){
        val testSchedule = createScheduledReport()
        val newId = Integer.MAX_VALUE
        val actual = testSchedule.scheduledReportCopy(id = newId)
        val expected = testSchedule.copy(id = newId)
        assertEquals(expected, actual)
    }


}