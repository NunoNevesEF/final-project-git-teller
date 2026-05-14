package pt.isel.gitteller.repo.memory.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.Pending
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.repository.memory.schedule.ScheduledReportJobRepoMem
import pt.isel.repository.memory.schedule.ScheduledReportRepoMem
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ScheduledReportRepoMemTest {
    @Mock
    private lateinit var jobRepo: ScheduledReportJobRepoMem

    private lateinit var repo: ScheduledReportRepoMem

    @BeforeEach
    fun setup(){ repo = ScheduledReportRepoMem(jobRepo) }

    private val validId = 0
    private val validUserId = 0
    private val validJobId = 0
    private val validRepoUri = "someUri"
    private val validDataStart: Instant = Instant.now()
    private val validNextRun : Instant = Instant.now().plus(Duration.ofDays(1))

    private val active = Pending(validNextRun).run()
    private val complete = active.end(true)

    private fun newScheduledReport(
        id: Int = validId, userId: Int = validUserId,
        repoUri: String = validRepoUri, nextRun: Instant = validNextRun, dataStart: Instant = validDataStart
    ) = OneTimeScheduledReport.create(
        id, userId, repoUri, nextRun, dataStart
    )

    private fun newScheduledReportJob(
        id: Int = validJobId, scheduledReportId: Int = validId, repoUri: String = validRepoUri,
        scheduledRunAt: Instant = validNextRun, dataFrom: Instant = validDataStart
    ) = ScheduledReportJob.create(
        id, scheduledReportId, repoUri, scheduledRunAt, dataFrom
    )

    @Test
    fun `method create returns ScheduledReportJob with repo assigned id`(){
        val testScheduledJob = newScheduledReport()
        val expectedId = repo.currId()

        val actual = repo.create(testScheduledJob)
        val expected = testScheduledJob.copy(id = expectedId)

        assertEquals(expected, actual)
    }

    @Test
    fun `method create assignedId increments after call`(){
        val oldId = repo.currId()
        val created = repo.create(newScheduledReport())
        val newId = repo.currId()

        assertEquals(oldId, created.id)
        assertEquals(oldId+1, newId)
    }

    @Test
    fun `method read returns LinkedAccount by id`(){
        val expected = repo.create(newScheduledReport())
        val actual = repo.read(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readScheduledReportsByUser returns user schedules`(){
        val schedule1 = repo.create(newScheduledReport(userId = validUserId))
        val schedule2 = repo.create(newScheduledReport(userId = validUserId))
        repo.create(newScheduledReport(userId = validUserId + 1))

        val expected = listOf(schedule1, schedule2)
        val actual = repo.readScheduledReportsByUser(validUserId)
        assertEquals(expected, actual)
    }

    @Test
    fun `method readPending returns schedules to be run in the next 15 minutes without active job`(){
        val pendingSoon = repo.create(
            newScheduledReport(nextRun = Instant.now().plus(Duration.ofMinutes(15)))
        )
        repo.create( //Not pending soon
            newScheduledReport(nextRun = Instant.now().plus(Duration.ofMinutes(30)))
        )
        val withActiveJob = repo.create(
            newScheduledReport(nextRun = Instant.now().plus(Duration.ofMinutes(10)))
        )
        val withCompletedJob = repo.create(
            newScheduledReport(nextRun = Instant.now().plus(Duration.ofMinutes(5)))
        )
        val activeJob = newScheduledReportJob(scheduledReportId = withActiveJob.id).copy(state = active)
        newScheduledReportJob(scheduledReportId = withCompletedJob.id).copy(state = complete) //completed job

        whenever(jobRepo.readIncompleteJobs()).thenReturn(listOf(activeJob))
        val expected = listOf(pendingSoon, withCompletedJob)
        val actual = repo.readPending()
        assertEquals(expected.sortedBy { it.id }, actual.sortedBy { it.id })
    }

    @Test
    fun `method update returns the updated job and persists it`(){
        val testSchedule = repo.create(newScheduledReport())
        val expected = testSchedule.completeCurrentExecution(Instant.now())
        val actual = repo.update(expected)

        assertEquals(expected, actual)
        assertEquals(expected, repo.read(expected.id))
    }

    @Test
    fun `method update returns Null if job not found`(){
        val actual = repo.update(newScheduledReport())
        assertNull(actual)
    }

    @Test
    fun `method delete returns deleted job and persists deletion`(){
        val expected = repo.create(newScheduledReport())
        val actual = repo.delete(expected.id)
        assertEquals(expected, actual)
        assertNull(repo.read(expected.id))
    }

    @Test
    fun `method delete returns Null if job not found`(){
        val actual = repo.delete(newScheduledReportJob().id)
        assertNull(actual)
    }
}