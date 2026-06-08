package pt.isel.gitteller.entity.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.entity.schedule.FailedJobStateEmbeddable
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PendingJobStateEmbeddable
import pt.isel.entity.schedule.RunningJobStateEmbeddable
import pt.isel.entity.schedule.ScheduledReportJobEntity
import pt.isel.entity.schedule.ScheduledReportJobStateEmbeddable
import pt.isel.entity.schedule.SuccessfulJobStateEmbeddable
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

abstract class ScheduledReportJobEntityTest<DOMAIN : ScheduledReportJob, STATE : ScheduledReportJobStateEmbeddable> {
    protected val now = Instant.now()

    protected val validId = 1
    protected val validDataFrom = now.minus(Duration.ofDays(1))
    protected val validScheduledFor = now
    protected val validRetryCount = 0

    abstract fun createState(): STATE

    abstract fun assertToDomain(
        original: ScheduledReportJobEntity, result: DOMAIN
    )

    @Test
    fun `method updateState properly updates state`() {
        val job = ScheduledReportJobEntity(
            validId, validDataFrom, validScheduledFor, validRetryCount, createState()
        )

        val newState = PendingJobStateEmbeddable(runAt = job.scheduledFor.plus(Duration.ofMinutes(15)))

        val updatedJob = job.updateState(newState)

        assertSame(job, updatedJob)
        assertSame(newState, job.state)
    }

    @Test
    fun `method toDomain properly converts entity`() {
        val entity = ScheduledReportJobEntity(
            validId, validDataFrom, validScheduledFor, validRetryCount, createState()
        )
        entity.scheduledReport = OneTimeScheduledReportEntity()

        val result = entity.toDomain()

        assertToDomain(entity, result as DOMAIN)
    }
}

class PendingJobEntityTest : ScheduledReportJobEntityTest<PendingJob, PendingJobStateEmbeddable>() {
    override fun createState() = PendingJobStateEmbeddable(validScheduledFor)

    override fun assertToDomain(
        original: ScheduledReportJobEntity, result: PendingJob
    ) {
        val state = original.state as PendingJobStateEmbeddable

        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assertEquals(state.runAt, result.runAt)
    }
}

class RunningJobEntityTest : ScheduledReportJobEntityTest<RunningJob, RunningJobStateEmbeddable>() {
    override fun createState() = RunningJobStateEmbeddable(now)

    override fun assertToDomain(
        original: ScheduledReportJobEntity, result: RunningJob
    ) {
        val state = original.state as RunningJobStateEmbeddable

        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)
        assertEquals(state.startedAt, result.startedAt)
    }
}

class SuccessfulJobEntityTest : ScheduledReportJobEntityTest<SuccessfulJob, SuccessfulJobStateEmbeddable>() {

    override fun createState() = SuccessfulJobStateEmbeddable(
        now, now.plusSeconds(10)
    )

    override fun assertToDomain(
        original: ScheduledReportJobEntity, result: SuccessfulJob
    ) {
        val state = original.state as SuccessfulJobStateEmbeddable

        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertEquals(state.startedAt, result.startedAt)
        assertEquals(state.endedAt, result.endedAt)
    }
}

class FailedJobEntityTest : ScheduledReportJobEntityTest<FailedJob, FailedJobStateEmbeddable>() {

    override fun createState() = FailedJobStateEmbeddable(
        now, now.plusSeconds(10), "some error"
    )

    override fun assertToDomain(
        original: ScheduledReportJobEntity, result: FailedJob
    ) {
        val state = original.state as FailedJobStateEmbeddable

        assertEquals(original.id, result.id)
        assertEquals(original.dataFrom, result.dataFrom)
        assertEquals(original.scheduledFor, result.scheduledFor)
        assertEquals(original.retryCount, result.retryCount)

        assertEquals(state.startedAt, result.startedAt)
        assertEquals(state.endedAt, result.endedAt)
        assertEquals(state.errorMsg, result.errorMsg)
    }
}