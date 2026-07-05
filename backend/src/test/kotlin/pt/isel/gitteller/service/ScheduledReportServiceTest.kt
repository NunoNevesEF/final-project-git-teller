package pt.isel.gitteller.service

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.RunningJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.entity.User
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportJobEntity
import pt.isel.model.scheduledReport.CreateOneTimeScheduledReportDTO
import pt.isel.repository.interfaces.IScheduledReportRepository
import pt.isel.service.InvalidScheduledReportDomainArguments
import pt.isel.service.ScheduledReportNotFoundException
import pt.isel.service.ScheduledReportService
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ScheduledReportServiceTest(){
    @Mock
    private lateinit var scheduledReportRepo: IScheduledReportRepository

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var service: ScheduledReportService

    private val validUserId = 0
    private val validScheduledReportId = 0
    private val validJobId = 0

    @Mock
    private lateinit var mockUser: User

    @Mock
    private lateinit var mockScheduledReport: OneTimeScheduledReport

    @Mock
    private lateinit var mockScheduledReportEntity: OneTimeScheduledReportEntity

    @Mock
    private lateinit var createScheduledReportDto: CreateOneTimeScheduledReportDTO

    @Mock
    private lateinit var mockPendingJob: PendingJob

    @Mock
    private lateinit var mockRunningJob: RunningJob

    @Mock
    private lateinit var mockCompleteJob: SuccessfulJob

    @Mock
    private lateinit var mockPendingJobEntity: ScheduledReportJobEntity


    @Test
    fun `method createScheduledReport returns created ScheduledReport`(){
        whenever(userService.findById(validUserId)).thenReturn(success(mockUser))

        whenever(createScheduledReportDto.toDomain(validUserId)).thenReturn(mockScheduledReport)

        //whenever(mockScheduledReport.toEntity(mockUser)).thenReturn(mockScheduledReportEntity)

        whenever(scheduledReportRepo.create(mockScheduledReportEntity)).thenReturn(mockScheduledReportEntity)

        whenever(mockScheduledReportEntity.toDomain()).thenReturn(mockScheduledReport)

        val result = service.createScheduledReport(createScheduledReportDto, validUserId)

        assertTrue(result.isSuccess())
        assertEquals(mockScheduledReport.id, result.rightOrNull())
    }

    @Test
    fun `method createScheduledReport returns UserNotFound when user does not exist`() {
        whenever(userService.findById(validUserId)).thenReturn(failure(UserNotFound))

        val result = service.createScheduledReport(createScheduledReportDto, validUserId)

        assertTrue(result.isFailure())
        assertEquals(UserNotFound, result.leftOrNull())
    }

    @Test
    fun `method createScheduledReport returns InvalidScheduledReportDomainArguments when dto conversion fails`() {
        val exceptionMessage = "invalid argument"

        whenever(userService.findById(validUserId)).thenReturn(success(mockUser))

        whenever(createScheduledReportDto.toDomain(any()))
            .thenThrow(IllegalArgumentException(exceptionMessage))

        val result = service.createScheduledReport(createScheduledReportDto, validUserId)

        assertTrue(result.isFailure())

        val error = result.leftOrNull()
        assertTrue(error is InvalidScheduledReportDomainArguments)
        assertEquals(exceptionMessage, error.msg)
    }

    @Test
    fun `method createScheduledReportJob returns pending job when successful`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(mockScheduledReportEntity)

        whenever(mockScheduledReportEntity.toDomain()).thenReturn(mockScheduledReport)

        whenever(mockScheduledReport.createJob()).thenReturn(mockPendingJob)

        whenever(mockPendingJob.toEntity()).thenReturn(mockPendingJobEntity)

        whenever(scheduledReportRepo.update(mockScheduledReportEntity))
            .thenReturn(mockScheduledReportEntity)

        val result = service.createScheduledReportJob(validScheduledReportId)

        assertEquals(mockPendingJob, result)

        verify(mockScheduledReportEntity).addJob(mockPendingJobEntity)
        verify(scheduledReportRepo).update(mockScheduledReportEntity)
    }

    @Test
    fun `method createScheduledReportJob throws ScheduledReportNotFoundException when schedule does not exist`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(null)

        assertThrows<ScheduledReportNotFoundException> { service.createScheduledReportJob(validScheduledReportId) }
    }

    /*@Test
    fun `method cancelReport updates schedule`(){
        val error = "some error"

        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(mockScheduledReportEntity)
        whenever(scheduledReportRepo.update(mockScheduledReportEntity)).thenReturn(mockScheduledReportEntity)

        service.cancelReport(validScheduledReportId, error)

        verify(mockScheduledReportEntity).cancel(error)
        verify(scheduledReportRepo).update(mockScheduledReportEntity)
    }*/

    @Test
    fun `cancelReport throws ScheduledReportNotFoundException when schedule does not exist`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(null)

        assertThrows<ScheduledReportNotFoundException> { service.cancelReport(validScheduledReportId, "some error") }
    }

    /*@Test
    fun `method listDueJobs returns ids from repository`() {
        val dueScheduledReportId1 = 1
        val dueScheduledReportId2 = 2

        val repoUri1 = "repo1"
        val repoUri2 = "repo2"

        val userId1 = 1
        val userId2 = 2

        val user1 = mock<User>()
        val user2 = mock<User>()

        whenever(user1.id).thenReturn(userId1)
        whenever(user2.id).thenReturn(userId2)

        val dueScheduledReport1 = mock<OneTimeScheduledReportEntity>()
        val dueScheduledReport2 = mock<PeriodicScheduledReportEntity>()

        whenever(dueScheduledReport1.id).thenReturn(dueScheduledReportId1)
        whenever(dueScheduledReport1.repoUri).thenReturn(repoUri1)
        whenever(dueScheduledReport1.user).thenReturn(user1)

        whenever(dueScheduledReport2.id).thenReturn(dueScheduledReportId2)
        whenever(dueScheduledReport2.repoUri).thenReturn(repoUri2)
        whenever(dueScheduledReport2.user).thenReturn(user2)

        whenever(scheduledReportRepo.findDue())
            .thenReturn(listOf(dueScheduledReport1, dueScheduledReport2))

        val result = service.listDueJobs()

        assertEquals(
            listOf(
                Triple(dueScheduledReportId1, repoUri1, userId1),
                Triple(dueScheduledReportId2, repoUri2, userId2)
            ),
            result
        )
    }*/

    /*@Test
    fun `method calculateNextScheduledReport returns updated scheduled report`(){
        val original = mockScheduledReport
        val updated = mock<OneTimeScheduledReport>()

        val originalEntity = mockScheduledReportEntity
        val updatedEntity = mock<OneTimeScheduledReportEntity>()

        whenever(original.advanceSchedule()).thenReturn(updated)

        whenever(scheduledReportRepo.findById(validScheduledReportId))
            .thenReturn(originalEntity)

        whenever(originalEntity.toDomain()).thenReturn(original)

        whenever(updated.toEntity(originalEntity.user)).thenReturn(updatedEntity)

        whenever(scheduledReportRepo.update(updatedEntity)).thenReturn(updatedEntity)

        val result = service.calculateNextReport(validScheduledReportId)

        assertEquals(updated, result)
        assertEquals(originalEntity.jobs, updatedEntity.jobs)

        verify(original).advanceSchedule()
        verify(scheduledReportRepo).update(updatedEntity)
    }*/

    @Test
    fun `method calculateNextScheduledReport throws ScheduledReportNotFoundException when schedule not found`(){
        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(null)

        assertThrows<ScheduledReportNotFoundException>{ service.calculateNextReport(validScheduledReportId) }
    }

    /*@Test
    fun `method updateReportLastRun returns updated scheduled report`(){
        whenever(mockCompleteJob.scheduledReportId).thenReturn(validScheduledReportId)

        val original = mockScheduledReport
        val updated = mock<OneTimeScheduledReport>()

        val originalEntity = mockScheduledReportEntity
        val updatedEntity = mock<OneTimeScheduledReportEntity>()

        whenever(original.recordExecution(mockCompleteJob.startedAt)).thenReturn(updated)

        whenever(scheduledReportRepo.findById(validScheduledReportId))
            .thenReturn(originalEntity)

        whenever(originalEntity.toDomain()).thenReturn(original)

        whenever(updated.toEntity(originalEntity.user)).thenReturn(updatedEntity)

        whenever(scheduledReportRepo.update(updatedEntity)).thenReturn(updatedEntity)

        val result = service.updateReportLastRun(mockCompleteJob.scheduledReportId, mockCompleteJob.startedAt)

        assertEquals(updated, result)
        assertEquals(originalEntity.jobs, updatedEntity.jobs)

        verify(original).recordExecution(mockCompleteJob.startedAt)
        verify(scheduledReportRepo).update(updatedEntity)
    }*/

    @Test
    fun `method updateReportLastRun throws ScheduledReportNotFoundException when schedule not found`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId))
            .thenReturn(null)

        assertThrows<ScheduledReportNotFoundException>{ service.updateReportLastRun(mockCompleteJob.scheduledReportId, mockCompleteJob.startedAt) }
    }

    @Test
    fun `runJob updates job and returns running job`() {
        val original = mockPendingJob
        val updated = mockRunningJob

        val originalEntity = mockScheduledReportEntity

        whenever(original.scheduledReportId).thenReturn(validScheduledReportId)
        whenever(original.id).thenReturn(validJobId)

        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(originalEntity)

        whenever(original.run()).thenReturn(updated)

        whenever(originalEntity.updateJob(eq(validJobId), any())).thenReturn(mock())

        whenever(scheduledReportRepo.update(originalEntity)).thenReturn(originalEntity)

        val result = service.runJob(mockPendingJob)

        assertEquals(updated, result)

        verify(original).run()
        verify(originalEntity).updateJob(eq(validJobId), any())
        verify(scheduledReportRepo).update(originalEntity)
    }

    @Test
    fun `runJob throws ScheduledReportNotFoundException when schedule not found`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(null)

        assertThrows<ScheduledReportNotFoundException>{ service.runJob(mockPendingJob) }
    }

    @Test
    fun `endJob updates job and returns completed job`() {
        val original = mockRunningJob
        val updated = mockCompleteJob

        val originalEntity = mockScheduledReportEntity

        whenever(original.scheduledReportId).thenReturn(validScheduledReportId)
        whenever(original.id).thenReturn(validJobId)

        whenever(scheduledReportRepo.findById(validScheduledReportId)).thenReturn(originalEntity)

        whenever(original.end(any(), any(), any())).thenReturn(updated)

        whenever(originalEntity.updateJob(eq(validJobId), any())).thenReturn(mock())

        whenever(scheduledReportRepo.update(originalEntity)).thenReturn(originalEntity)

        val result = service.endJob(mockRunningJob, true)

        assertEquals(updated, result)

        verify(original).end(true, allowRetry = true)
        verify(originalEntity).updateJob(eq(validJobId), any())
        verify(scheduledReportRepo).update(originalEntity)
    }

    @Test
    fun `endJob throws ScheduledReportNotFoundException when schedule not found`() {
        whenever(scheduledReportRepo.findById(validScheduledReportId))
            .thenReturn(null)

        assertThrows<ScheduledReportNotFoundException>{ service.endJob(mockRunningJob, true) }
    }
}