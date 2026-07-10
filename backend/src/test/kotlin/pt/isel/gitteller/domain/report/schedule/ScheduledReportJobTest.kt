package pt.isel.gitteller.domain.report.schedule

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.domain.report.schedule.FailedJob
import pt.isel.domain.report.schedule.JobInvalidDateRangeException
import pt.isel.domain.report.schedule.PendingJob
import pt.isel.domain.report.schedule.RunningJob
import pt.isel.domain.report.schedule.ScheduledJobReportPolicy
import pt.isel.domain.report.schedule.ScheduledReportJob
import pt.isel.domain.report.schedule.SuccessfulJob
import pt.isel.entity.report.model.JobStatus
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

abstract class ScheduledReportJobTest<T : ScheduledReportJob> {
    val now = Instant.now()

    val validId = 0
    val validScheduledReportId = 0
    val validDataFrom: Instant = now.minus(Duration.ofDays(1))
    val validDataTo = now
    val validScheduledFor: Instant = now
    val validRetryCount: Int = 0

    abstract fun createScheduledReportJob(
        id: Int = validId,
        scheduledReportId: Int = validScheduledReportId,
        dataFrom: Instant = validDataFrom,
        dataTo: Instant = validDataTo,
        scheduledFor: Instant = validScheduledFor,
        retryCount: Int = validRetryCount,
    ): T

    abstract fun assertToEntity(original: T, result: ScheduledReportJobEntity)

    @Test
    fun `creation fails if dataFrom greater than dataTo`() {
        assertFailsWith<JobInvalidDateRangeException> {
            createScheduledReportJob(dataFrom = validDataTo.plusSeconds(1), dataTo = validDataTo)
        }
    }

    @Test
    fun `creation fails if dataFrom equals dataTo`() {
        assertFailsWith<JobInvalidDateRangeException> {
            createScheduledReportJob(dataFrom = validDataTo, scheduledFor = validDataTo)
        }
    }

    @Test
    fun `creation success if dataFrom less than dataTo`() {
        assertDoesNotThrow { createScheduledReportJob(dataFrom = validDataTo.minusSeconds(1), scheduledFor = validDataTo) }
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
        id: Int, scheduledReportId: Int, dataFrom: Instant, dataTo: Instant, scheduledFor: Instant, retryCount: Int
    ) = PendingJob(id, scheduledReportId, dataFrom, dataTo, scheduledFor)

    override fun assertToEntity(
        original: PendingJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assert(result.state.status == JobStatus.PENDING)
        assertEquals(original.runAt, result.state.runAt)
    }

    @Test
    fun `method run returns Running state with correct parameters`() {
        val testJob = createScheduledReportJob()

        val before = Instant.now()
        val actual = testJob.run()
        val after = Instant.now()

        val expected = RunningJob(
            testJob.id,
            testJob.scheduledReportId,
            testJob.dataFrom,
            testJob.dataTo,
            testJob.scheduledFor,
            testJob.retryCount,
            actual.startedAt
        )

        assertTrue(actual.startedAt in before..after)
        assertEquals(expected, actual)
    }
}

class RunningJobTest : ScheduledReportJobTest<RunningJob>() {
    private fun createRunningJob(
        id: Int = validId,
        scheduledReportId: Int = validScheduledReportId,
        dataFrom: Instant = validDataFrom,
        dataTo: Instant = validDataTo,
        scheduledFor: Instant = validScheduledFor,
        retryCount: Int = validRetryCount,
        startedAt: Instant = scheduledFor
    ) = RunningJob(id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount, startedAt)

    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, dataTo: Instant, scheduledFor: Instant, retryCount: Int
    ) = createRunningJob(id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount)

    override fun assertToEntity(
        original: RunningJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assertTrue(result.state.status == JobStatus.RUNNING)
        assertEquals(original.startedAt, result.state.startedAt)
    }

    @Test
    fun `method success properly creates Successful job`() {
        val original = createRunningJob()

        val before = Instant.now()
        val actual = original.success()
        val after = Instant.now()

        assertTrue(actual.endedAt in before..after)

        val expected = SuccessfulJob(
            id = original.id,
            scheduledReportId = original.scheduledReportId,
            dataFrom = original.dataFrom,
            dataTo = original.dataTo,
            scheduledFor = original.scheduledFor,
            retryCount = original.retryCount,
            startedAt = original.startedAt,
            endedAt = actual.endedAt,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `method failure properly creates Failed job`() {
        val original = createRunningJob()

        val before = Instant.now()
        val actual = original.failure("boom")
        val after = Instant.now()

        assertTrue(actual.endedAt in before..after)

        val expected = FailedJob(
            id = original.id,
            scheduledReportId = original.scheduledReportId,
            dataFrom = original.dataFrom,
            dataTo = original.dataTo,
            scheduledFor = original.scheduledFor,
            retryCount = original.retryCount,
            startedAt = original.startedAt,
            endedAt = actual.endedAt,
            errorMsg = "boom",
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `method failureOrRetry properly schedules retry`() {
        val original = createRunningJob(retryCount = 0)

        val actual = original.failureOrRetry()

        assertIs<PendingJob>(actual)

        assertEquals(original.retryCount + 1, actual.retryCount)
        assertEquals(original.dataFrom, actual.dataFrom)
        assertEquals(original.dataTo, actual.dataTo)
        assertEquals(original.scheduledFor, actual.scheduledFor)

        assertTrue(actual.runAt > original.startedAt)
    }

    @Test
    fun `method failureOrRetry properly fails after max retries`() {
        val original = createRunningJob(
            retryCount = ScheduledJobReportPolicy.MAX_RETRIES
        )

        val actual = original.failureOrRetry()

        assertIs<FailedJob>(actual)
        assertEquals("exceeded retries", actual.errorMsg)
    }
}

class SuccessfulJobTest : ScheduledReportJobTest<SuccessfulJob>() {
    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, dataTo: Instant, scheduledFor: Instant, retryCount: Int
    ) = SuccessfulJob(
        id, scheduledReportId, dataFrom, dataTo, scheduledFor, retryCount, validScheduledFor,
    )

    override fun assertToEntity(
        original: SuccessfulJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertTrue(result.state.status == JobStatus.SUCCESS)

        assertEquals(original.startedAt, result.state.startedAt)
        assertEquals(original.endedAt, result.state.endedAt)
    }
}

class FailedJobTest : ScheduledReportJobTest<FailedJob>() {
    override fun createScheduledReportJob(
        id: Int, scheduledReportId: Int, dataFrom: Instant, dataTo: Instant, scheduledFor: Instant, retryCount: Int
    ) = FailedJob(
        id, scheduledReportId, dataFrom, dataTo, scheduledFor, validRetryCount, validScheduledFor, errorMsg = "some error"
    )

    override fun assertToEntity(
        original: FailedJob, result: ScheduledReportJobEntity
    ) {
        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertTrue(result.state.status == JobStatus.FAILURE)

        assertEquals(original.startedAt, result.state.startedAt)
        assertEquals(original.endedAt, result.state.endedAt)
    }
}

class ScheduledReportJobFactoryTest {
    val now = Instant.now()

    val validScheduledReportId = 0
    val validDataFrom: Instant = now.minus(Duration.ofDays(1))
    val validScheduledFor: Instant = now

    @Test
    fun `companion method creates Pending job with scheduledFor as runAt`() {
        val actual = ScheduledReportJob.create(
            scheduledReportId = validScheduledReportId,
            dataFrom = validDataFrom,
            scheduledFor = validScheduledFor,
        )

        val expected = PendingJob(
            scheduledReportId = validScheduledReportId,
            dataFrom = validDataFrom,
            dataTo = validScheduledFor,
            scheduledFor = validScheduledFor,
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
}