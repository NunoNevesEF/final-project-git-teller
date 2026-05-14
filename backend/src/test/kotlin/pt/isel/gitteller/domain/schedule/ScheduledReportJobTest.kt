package pt.isel.gitteller.domain.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.domain.schedule.Failure
import pt.isel.domain.schedule.Pending
import pt.isel.domain.schedule.Running
import pt.isel.domain.schedule.ScheduledJobReportPolicy
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.ScheduledReportJobState
import pt.isel.domain.schedule.Success
import java.time.Duration
import java.time.Instant
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScheduledReportJobTest {
    private val validId = 0
    private val validScheduleId = 0
    private val validRepoURI = "gitTest.com/user/test"
    private val validDataStart : Instant = Instant.now()
    private val validDataEnd : Instant = validDataStart.plus(Duration.ofDays(1))
    private val validState = Pending(validDataEnd)

    private val now = Instant.now()


    companion object {

        @JvmStatic
        fun incompleteStates(): Stream<ScheduledReportJobState> {
            val now = Instant.now()
            return Stream.of(Pending(scheduledRunAt = now), Running(startedAt = now, attempt = 1))
        }

        @JvmStatic
        fun completeStates(): Stream<ScheduledReportJobState>{
            val now = Instant.now()
            return Stream.of(
                Success(startedAt = now, attempt = 1),
                Failure(startedAt = now, attempt = 1, errorMsg = "someErrorMsg")
            )
        }
    }

    fun createScheduledReportJob(
        id: Int = validId, scheduleId: Int = validScheduleId, state : ScheduledReportJobState = validState,
        repoUri : String = validRepoURI, dataStart : Instant = validDataStart, dataEnd : Instant = validDataEnd
    ) = ScheduledReportJob(id, scheduleId, state, repoUri, dataStart, dataEnd)

    @Test
    fun `creation fails if id less than 0`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReportJob(id = -1) }
    }

    @Test
    fun `creation success if id is 0`(){
        assertDoesNotThrow { createScheduledReportJob(id = 0) }
    }

    @Test
    fun `creation fails if scheduledReportId less than 0`(){
        assertFailsWith<IllegalArgumentException> { createScheduledReportJob(scheduleId = -1) }
    }

    @Test
    fun `creation success if scheduledReportId is 0`(){
        assertDoesNotThrow { createScheduledReportJob(scheduleId = 0) }
    }

    @Test
    fun `creation fails if dataFrom greater than dataTo`(){
        assertFailsWith<IllegalArgumentException> {
            createScheduledReportJob(dataStart = now.plusSeconds(1), dataEnd = now)
        }
    }

    @Test
    fun `creation fails if dataFrom equals dataTo`(){
        assertFailsWith<IllegalArgumentException> {
            createScheduledReportJob(dataStart = now, dataEnd = now)
        }
    }

    @Test
    fun `creation success if dataFrom less than dataTo`(){
        assertDoesNotThrow { createScheduledReportJob(dataStart = now.minusSeconds(1), dataEnd = now) }
    }

    @Test
    fun `companion method creates job with id as 0 if none passed`(){
        val actual = ScheduledReportJob.create(
            scheduledReportId = validScheduleId, repoUri = validRepoURI,
            scheduledRunAt = validState.scheduledRunAt, dataFrom = validDataStart, dataTo = validDataEnd,
        ).id
        assertEquals(0, actual)
    }

    @Test
    fun `companion method creates job with dataTo as scheduledRunAt if none passed`(){
        val expected = validState.scheduledRunAt
        val testJob = ScheduledReportJob.create(
            id = validId, scheduledReportId = validScheduleId, repoUri = validRepoURI,
            scheduledRunAt = expected, dataFrom = validDataStart,
        )
        assertEquals(expected, testJob.dataTo)
    }

    @Test
    fun `state Pending method run returns Running state with correct parameters`(){
        val expectedAttempt = 1
        val before = Instant.now()

        val actual = Pending(before, expectedAttempt).run()

        val after = Instant.now()

        assertTrue(actual.startedAt in before..after)
        assertEquals(expectedAttempt, actual.attempt)
    }

    @Test
    fun `state Running method end returns Success state with correct parameters if success is true`(){
        val testJob = Running(attempt = 1)
        val before = Instant.now()
        val actual = testJob.end(true)
        val after = Instant.now()

        assertTrue(actual is Success)
        assertEquals(testJob.startedAt, actual.startedAt)
        assertTrue(actual.endedAt in before..after)
        assertEquals(testJob.attempt, actual.attempt)
    }

    @Test
    fun `state Running method end returns Failure state with correct parameters if success is false and max attempts exceeded`(){
        val expectedErrorMsg = "Retries Exceeded"
        val testJob = Running(attempt = ScheduledJobReportPolicy.MAX_ATTEMPTS + 1)
        val before = Instant.now()
        val actual = testJob.end(false, expectedErrorMsg)
        val after = Instant.now()

        assertTrue(actual is Failure)
        assertEquals(testJob.startedAt, actual.startedAt)
        assertTrue(actual.endedAt in before..after)
        assertEquals(testJob.attempt, actual.attempt)
        assertEquals(expectedErrorMsg, actual.errorMsg)
    }

    @Test
    fun `state Running method end returns Pending state with correct parameters if success is false and max attempts not exceeded`(){
        val testJob = Running(attempt = ScheduledJobReportPolicy.MAX_ATTEMPTS - 1)
        val actual = testJob.end(false)

        assertTrue(actual is Pending)
        assertTrue(testJob.startedAt < actual.scheduledRunAt)
        assertEquals(testJob.attempt + 1, actual.attempt)
    }

    @ParameterizedTest
    @MethodSource("incompleteStates")
    fun `incomplete states should return false`(state: ScheduledReportJobState) {
        assertFalse(state.isComplete)
    }

    @ParameterizedTest
    @MethodSource("completeStates")
    fun `complete states should return true`(state: ScheduledReportJobState) {
        assertTrue(state.isComplete)
    }
}