package pt.isel.gitteller.domain.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledJobReportPolicy
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.entity.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/*abstract class ScheduledReportJobTest<T : ScheduledReportJob> {
    val now = Instant.now()

    val validId = 0
    val validScheduledReportId = 0
    val validDataFrom: Instant = now.minus(Duration.ofDays(1))
    val validScheduledFor: Instant = now
    val validRetryCount: Int = 0

    abstract fun createScheduledReportJob(
        id: Int = validId,
        scheduledReportId: Int = validScheduledReportId,
        dataFrom: Instant = validDataFrom,
        scheduledFor: Instant = validScheduledFor
    ): T

    abstract fun assertToEntity(original: T, result: ScheduledReportJobEntity)

    @Test
    fun `creation fails if id less than 0`() {
        assertFailsWith<IllegalArgumentException> { createScheduledReportJob(id = -1) }
    }

    @Test
    fun `creation success if id is 0`() {
        assertDoesNotThrow { createScheduledReportJob(id = 0) }
    }

    @Test
    fun `creation fails if scheduledReportId less than 0`() {
        assertFailsWith<IllegalArgumentException> { createScheduledReportJob(scheduledReportId = -1) }
    }

    @Test
    fun `creation success if scheduledReportId is 0`() {
        assertDoesNotThrow { createScheduledReportJob(scheduledReportId = 0) }
    }

    @Test
    fun `creation fails if dataFrom greater than dataTo`() {
        assertFailsWith<IllegalArgumentException> {
            createScheduledReportJob(dataFrom = now.plusSeconds(1), scheduledFor = now)
        }
    }

    @Test
    fun `creation fails if dataFrom equals dataTo`() {
        assertFailsWith<IllegalArgumentException> {
            createScheduledReportJob(dataFrom = now, scheduledFor = now)
        }
    }

    @Test
    fun `creation success if dataFrom less than dataTo`() {
        assertDoesNotThrow { createScheduledReportJob(dataFrom = now.minusSeconds(1), scheduledFor = now) }
    }

    @Test
    fun `method toEntity properly creates Entity`() {
        val original = createScheduledReportJob()
        val entity = original.toEntity()
        assertToEntity(original, entity)
    }
}

class PendingJobTest : ScheduledReportJobTest<PendingJob>() {
    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant
    ) = PendingJob(id, scheduledReportId, dataFrom, scheduledFor)

    override fun assertToEntity(
        original: PendingJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assertTrue(result.state is PendingJobEntity)
        assertEquals(original.runAt, (result.state as PendingJobEntity).runAt)
    }

    @Test
    fun `method run returns Running state with correct parameters`() {
        val testJob = PendingJob(dataFrom = validDataFrom, scheduledReportId = validScheduledReportId, scheduledFor = validScheduledFor)

        val before = Instant.now()
        val actual = testJob.run()
        val after = Instant.now()

        val expected = RunningJob(
            testJob.id, testJob.scheduledReportId, testJob.dataFrom, testJob.scheduledFor, testJob.retryCount
        ).copy(startedAt = actual.startedAt)

        assertTrue(actual.startedAt in before..after)
        assertEquals(expected, actual)
    }
}

class RunningJobTest : ScheduledReportJobTest<RunningJob>() {
    private fun createRunningJob(
        id: Int = validId,
        scheduledReportId: Int = validScheduledReportId,
        dataFrom: Instant = validDataFrom,
        scheduledFor: Instant = validScheduledFor,
        retryCount: Int = validRetryCount,
        startedAt: Instant = scheduledFor
    ) = RunningJob(id, scheduledReportId, dataFrom, scheduledFor, retryCount, startedAt)

    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant
    ) = createRunningJob(id, scheduledReportId, dataFrom, scheduledFor)

    override fun assertToEntity(
        original: RunningJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assertTrue(result.state is RunningJobEntity)
        assertEquals(original.startedAt, (result.state as RunningJobEntity).startedAt)
    }

    @Test
    fun `method end returns Success state with correct parameters if success is true`() {
        val testJob = createRunningJob()
        val before = Instant.now()
        val actual = testJob.end(true, allowRetry = true)
        val after = Instant.now()

        assertTrue(actual is SuccessfulJob)

        val expected = SuccessfulJob(
            testJob.id, testJob.scheduledReportId, testJob.dataFrom, testJob.scheduledFor, testJob.retryCount, testJob.startedAt
        ).copy(endedAt = actual.endedAt)

        assertTrue(actual.endedAt in before..after)
        assertEquals(expected, actual)
    }

    @Test
    fun `state Running method end returns Failure state with correct parameters if success is false and retry not allowed`() {
        val expectedErrorMsg = "some big error"
        val testJob = createRunningJob(retryCount = 0)
        val before = Instant.now()
        val actual = testJob.end(false, expectedErrorMsg, false)
        val after = Instant.now()

        assertTrue(actual is FailedJob)

        val expected = FailedJob(
            testJob.id,
            testJob.scheduledReportId,
            testJob.dataFrom,
            testJob.scheduledFor,
            testJob.retryCount,
            testJob.startedAt,
            errorMsg = expectedErrorMsg
        ).copy(endedAt = actual.endedAt)

        assertTrue(actual.endedAt in before..after)
        assertEquals(expected, actual)
    }

    @Test
    fun `state Running method end returns Failure state with correct parameters if success is false and max retries exceeded`() {
        val expectedErrorMsg = "exceeded retries"
        val testJob = createRunningJob(retryCount = ScheduledJobReportPolicy.MAX_RETRIES + 1)
        val before = Instant.now()
        val actual = testJob.end(false, expectedErrorMsg, true)
        val after = Instant.now()

        assertTrue(actual is FailedJob)

        val expected = FailedJob(
            testJob.id,
            testJob.scheduledReportId,
            testJob.dataFrom,
            testJob.scheduledFor,
            testJob.retryCount,
            testJob.startedAt,
            errorMsg = expectedErrorMsg
        ).copy(endedAt = actual.endedAt)

        assertTrue(actual.endedAt in before..after)
        assertEquals(expected, actual)
    }

    @Test
    fun `state Running method end returns Pending state with correct parameters if success is false and max retries not exceeded`() {
        val testJob = createRunningJob(retryCount = ScheduledJobReportPolicy.MAX_RETRIES - 1)
        val actual = testJob.end(false, allowRetry = true)

        assertTrue(actual is PendingJob)

        val expected = PendingJob(
            testJob.id, testJob.scheduledReportId, testJob.dataFrom, testJob.scheduledFor, testJob.retryCount + 1
        ).copy(runAt = actual.runAt)

        assertTrue(testJob.startedAt < actual.runAt)
        assertEquals(expected, actual)
    }
}

class SuccessfulJobTest : ScheduledReportJobTest<SuccessfulJob>() {
    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant
    ) = SuccessfulJob(
        id, scheduledReportId, dataFrom, scheduledFor, validRetryCount, validScheduledFor
    )

    override fun assertToEntity(
        original: SuccessfulJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertTrue(result.state is SuccessfulJobEntity)
        val state = result.state as SuccessfulJobEntity

        assertEquals(original.startedAt, state.startedAt)
        assertEquals(original.endedAt, state.endedAt)
    }
}

class FailedJobTest : ScheduledReportJobTest<FailedJob>() {
    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, scheduledFor: Instant
    ) = FailedJob(
        id, scheduledReportId, dataFrom, scheduledFor, validRetryCount, validScheduledFor, errorMsg = "some error"
    )

    override fun assertToEntity(
        original: FailedJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertTrue(result.state is FailedJobEntity)
        val state = result.state as FailedJobEntity

        assertEquals(original.startedAt, state.startedAt)
        assertEquals(original.endedAt, state.endedAt)
    }
}

class ScheduledReportJobFactoryTest {
    val now = Instant.now()

    val validScheduledReportId = 0
    val validDataFrom: Instant = now.minus(Duration.ofDays(1))
    val validScheduledFor: Instant = now

    @Test
    fun `companion method creates Pending job with scheduledFor as runAt`() {
        val expected = PendingJob(scheduledReportId = validScheduledReportId, dataFrom = validDataFrom, scheduledFor = validScheduledFor, runAt = validScheduledFor)
        val actual = ScheduledReportJob.create(
            scheduledReportId = validScheduledReportId, dataFrom = validDataFrom, scheduledFor = validScheduledFor,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `companion method creates job with id as 0`() {
        val actual = ScheduledReportJob.create(
            scheduledReportId = validScheduledReportId, dataFrom = validDataFrom, scheduledFor = validScheduledFor,
        ).id
        assertEquals(0, actual)
    }

    @Test
    fun `companion method creates job with retryCount as 0`() {
        val actual = ScheduledReportJob.create(
            scheduledReportId = validScheduledReportId, dataFrom = validDataFrom, scheduledFor = validScheduledFor
        ).retryCount
        assertEquals(0, actual)
    }
}*/