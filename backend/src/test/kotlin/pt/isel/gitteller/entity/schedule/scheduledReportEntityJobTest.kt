package pt.isel.gitteller.entity.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.domain.report.schedule.FailedJob
import pt.isel.domain.report.schedule.PendingJob
import pt.isel.domain.report.schedule.RunningJob
import pt.isel.domain.report.schedule.ScheduledReportJob
import pt.isel.domain.report.schedule.SuccessfulJob
import pt.isel.entity.report.schedule.JobStateEmbeddable
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class ScheduledReportJobEntityTest {
    protected val now = Instant.now()

    protected val validId = 1
    protected val validDataFrom = now.minus(Duration.ofDays(1))
    protected val validDataTo = now
    protected val validScheduledFor = now
    protected val validRetryCount = 0

    private companion object {
        protected val now = Instant.now()

        protected val validId = 1
        protected val validDataFrom = now.minus(Duration.ofDays(1))
        protected val validDataTo = now
        protected val validScheduledFor = now
        protected val validRetryCount = 0

        @JvmStatic
        fun jobStates() = listOf(
            Arguments.of(
                JobStateEmbeddable.pending(now),
                PendingJob::class.java,
                { state: JobStateEmbeddable, job: ScheduledReportJob ->
                    job as PendingJob
                    assertEquals(state.runAt, job.runAt)
                }
            ),
            Arguments.of(
                JobStateEmbeddable.running(now),
                RunningJob::class.java,
                { state: JobStateEmbeddable, job: ScheduledReportJob ->
                    job as RunningJob
                    assertEquals(state.startedAt, job.startedAt)
                }
            ),
            Arguments.of(
                JobStateEmbeddable.successful(now, now.plusSeconds(10)),
                SuccessfulJob::class.java,
                { state: JobStateEmbeddable, job: ScheduledReportJob ->
                    job as SuccessfulJob
                    assertEquals(state.startedAt, job.startedAt)
                    assertEquals(state.endedAt, job.endedAt)
                }
            ),
            Arguments.of(
                JobStateEmbeddable.failed(now, now.plusSeconds(10), "some error"),
                FailedJob::class.java,
                { state: JobStateEmbeddable, job: ScheduledReportJob ->
                    job as FailedJob
                    assertEquals(state.startedAt, job.startedAt)
                    assertEquals(state.endedAt, job.endedAt)
                    assertEquals(state.errorMsg, job.errorMsg)
                }
            )
        )
    }

    @ParameterizedTest
    @MethodSource("jobStates")
    fun `method toDomain properly converts entity`(
        state: JobStateEmbeddable,
        expectedType: Class<out ScheduledReportJob>,
        extraAssertions: (JobStateEmbeddable, ScheduledReportJob) -> Unit
    ) {
        val entity = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            validRetryCount,
            state
        )

        entity.scheduledReport = OneTimeScheduledReportEntity(id = 123)

        val result = entity.toDomain()

        assertTrue(expectedType.isInstance(result))

        assertEquals(entity.id, result.id)
        assertEquals(entity.scheduledReport.id, result.scheduledReportId)
        assertEquals(entity.dataFrom, result.dataFrom)
        assertEquals(entity.dataTo, result.dataTo)
        assertEquals(entity.scheduledFor, result.scheduledFor)
        assertEquals(entity.retryCount, result.retryCount)

        extraAssertions(state, result)
    }

    @ParameterizedTest
    @MethodSource("jobStates")
    fun `method updateState properly updates state`(
        state: JobStateEmbeddable,
    ) {
        val job = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            validRetryCount,
            JobStateEmbeddable.pending(validScheduledFor)
        )

        val updated = job.updateState(state)

        assertSame(job, updated)
        assertSame(state, job.state)
    }

    @Test
    fun `method isQueued returns true for pending`() {
        val job = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            state = JobStateEmbeddable.pending(validScheduledFor)
        )

        assertTrue(job.isQueued())
    }

    @Test
    fun `method isQueued returns true for running`() {
        val job = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            state = JobStateEmbeddable.running(now)
        )

        assertTrue(job.isQueued())
    }

    @Test
    fun `isQueued returns false for successful`() {
        val job = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            state = JobStateEmbeddable.successful(now, now.plusSeconds(1))
        )

        assertFalse(job.isQueued())
    }

    @Test
    fun `isQueued returns false for failed`() {
        val job = ScheduledReportJobEntity(
            validId,
            validDataFrom,
            validDataTo,
            validScheduledFor,
            state = JobStateEmbeddable.failed(now, now.plusSeconds(1), "boom")
        )

        assertFalse(job.isQueued())
    }
}