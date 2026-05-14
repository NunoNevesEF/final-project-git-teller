package pt.isel.gitteller.repo.memory.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import pt.isel.domain.schedule.Pending
import pt.isel.domain.schedule.ScheduledJobReportPolicy
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.repository.memory.schedule.ScheduledReportJobRepoMem
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class ScheduledReportJobRepoMemTest{
    private lateinit var repo: ScheduledReportJobRepoMem
    private val validId = 0
    private val validScheduleId = 0
    private val validRepoUri = "someUri"
    private val validDataFrom: Instant = Instant.now()
    private val validScheduledRunAt : Instant = Instant.now().plus(Duration.ofDays(1))

    private val pending = Pending(validScheduledRunAt)
    private val running = pending.run()
    private val success = running.end(true)
    private val failed = running.copy(attempt = ScheduledJobReportPolicy.MAX_ATTEMPTS + 1).end(false)

    private fun newScheduledReportJob(
        id: Int = validId, scheduledReportId: Int = validScheduleId, repoUri: String = validRepoUri,
        scheduledRunAt: Instant = validScheduledRunAt, dataFrom: Instant = validDataFrom
    ) = ScheduledReportJob.create(
        id, scheduledReportId, repoUri, scheduledRunAt, dataFrom
    )

    @BeforeEach
    fun setup(){ repo = ScheduledReportJobRepoMem() }

    @Test
    fun `method create returns ScheduledReportJob with repo assigned id`(){
        val testScheduledJob = newScheduledReportJob()
        val expectedId = repo.currId()

        val actual = repo.create(testScheduledJob)
        val expected = testScheduledJob.copy(id = expectedId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method create assignedId increments after call`(){
        val oldId = repo.currId()
        val created = repo.create(newScheduledReportJob())
        val newId = repo.currId()

        assertEquals(oldId, created.id)
        assertEquals(oldId+1, newId)
    }

    @Test
    fun `method read returns LinkedAccount by id`(){
        val expected = repo.create(newScheduledReportJob())
        val actual = repo.read(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readIncompleteJobs returns all jobs with state Pending or Running`(){
        val job = newScheduledReportJob()
        val pendingJob = repo.create(job.copy(state = pending))
        val runningJob = repo.create(job.copy(state = running))
        repo.create(job.copy(state = success))
        repo.create(job.copy(state = failed))

        val expected = listOf(pendingJob, runningJob)
        val actual = repo.readIncompleteJobs()
        assertEquals(expected, actual)
    }

    @Test
    fun `method update returns the updated job`(){
        val testScheduledJob = repo.create(newScheduledReportJob())
        val expected = testScheduledJob.copy(state = success)
        val actual = repo.update(expected)

        assertEquals(expected, actual)
    }

    @Test
    fun `method update returns Null if job not found`(){
        val actual = repo.update(newScheduledReportJob())
        assertNull(actual)
    }

    @Test
    fun `method delete returns deleted job`(){
        val expected = repo.create(newScheduledReportJob())
        val actual = repo.delete(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method delete returns Null if job not found`(){
        val actual = repo.delete(newScheduledReportJob().id)
        assertNull(actual)
    }
}