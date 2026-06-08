package pt.isel.gitteller.service

import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.internal.JGitText
import org.eclipse.jgit.transport.URIish
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pt.isel.domain.report.GitCommunication
import pt.isel.entity.ReportEntity
import pt.isel.entity.User
import pt.isel.model.report.GitAnalysis
import pt.isel.repository.interfaces.IUserReportRepository
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.service.report.ReportPDFGenerationService
import pt.isel.service.report.UserReportNotFound
import pt.isel.service.report.UserReportPDFNotGenerated
import pt.isel.service.report.UserReportService
import pt.isel.utils.failure
import pt.isel.utils.isFailure
import pt.isel.utils.isSuccess
import pt.isel.utils.leftOrNull
import pt.isel.utils.rightOrNull
import pt.isel.utils.success
import java.time.Instant
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertTrue

/*@ExtendWith(MockitoExtension::class)
class UserReportServiceTest {

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var userReportRepo: IUserReportRepository

    @Mock
    private lateinit var reportGenerationService: ReportPDFGenerationService

    @Spy
    @InjectMocks
    private lateinit var service: UserReportService

    private val validUserId = 1
    private val validReportId = 10
    private val validRepoUri = "https://github.com/test/repo.git"

    @Mock
    private lateinit var mockUser: User

    @Mock
    private lateinit var mockGitAnalysis: GitAnalysis

    @Mock
    private lateinit var mockReportEntity: ReportEntity

    @Mock
    private lateinit var mockGitCommunication: GitCommunication

    @Test
    fun `method createReport returns report id`() {
        whenever(userService.findById(validUserId)).thenReturn(success(mockUser))
        doReturn(success(mockGitAnalysis))
            .whenever(service)
            .generateAnalysis(validRepoUri)
        whenever(userReportRepo.create(any())).thenReturn(mockReportEntity)

        whenever(mockReportEntity.id).thenReturn(validReportId)

        val result = service.generateAnalysis(validUserId, validRepoUri)

        assertTrue(result.isSuccess())
        assertEquals(validReportId, result.rightOrNull())
    }

    @Test
    fun `method createReport returns User service error`() {
        whenever(userService.findById(validUserId)).thenReturn(failure(UserNotFound))

        val result = service.generateAnalysis(validUserId, validRepoUri)

        assertTrue(result.isFailure())
        assertEquals(UserNotFound, result.leftOrNull())
    }

    @Test
    fun `method createReport returns createGitAnalysis method error`() {
        whenever(userService.findById(validUserId)).thenReturn(success(mockUser))
        doReturn(failure(FailureRetry))
            .whenever(service)
            .generateAnalysis(validRepoUri)

        val result = service.generateAnalysis(validUserId, validRepoUri)

        assertTrue(result.isFailure())
        assertEquals(FailureRetry, result.leftOrNull())
    }

    @Test
    fun `getUserReportsByUserId returns mapped DTOs`() {
        val createdAt = Instant.now()

        whenever(mockReportEntity.id).thenReturn(validReportId)
        whenever(mockReportEntity.createdAt).thenReturn(createdAt)
        whenever(mockReportEntity.repoURI).thenReturn(validRepoUri)

        whenever(userReportRepo.findByUserId(validUserId))
            .thenReturn(listOf(mockReportEntity))

        val result = service.getUserReportsByUserId(validUserId)

        assertEquals(1, result.size)
        assertEquals(validReportId, result[0].id)
        assertEquals(validRepoUri, result[0].repoURI)
        assertEquals(createdAt, result[0].createdAt)
    }

    @Test
    fun `method createAnalysis returns analysis`() {
        mockGitCommunication().use {
            mockGitAnalysis().use {
                val result = service.generateAnalysis(validRepoUri)

                assertTrue(result.isSuccess())
                assertEquals(mockGitAnalysis, result.rightOrNull())
            }
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Invalid Repo Uri on InvalidRemoteException`() {
        mockGitCommunicationThrow(InvalidRemoteException("some error")).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.REPO_NOT_FOUND), result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Unknown Error on non handled error`() {
        mockGitCommunicationThrow(IllegalArgumentException("some error")).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.UNKNOWN_ERROR), result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Repo Not Found on TransportException caused by NoRemoteRepositoryException`() {
        mockGitCommunicationThrow(
            TransportException(
                "some error",
                NoRemoteRepositoryException(
                    URIish(validRepoUri),
                    "repo not found"
                )
            )
        ).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.REPO_NOT_FOUND), result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Authentication Error on TransportException with msg authenticationNotSupported`() {
        mockGitCommunicationThrow(TransportException(JGitText.get().authenticationNotSupported)).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR), result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Authentication Error on TransportException with msg notAuthorized`() {
        mockGitCommunicationThrow(TransportException(JGitText.get().notAuthorized)).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR), result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureRetry on TransportException with msg containing 408`() {
        mockGitCommunicationThrow(TransportException("status 408")).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureRetry, result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureRetry on TransportException with msg containing 504`() {
        mockGitCommunicationThrow(TransportException("status 504")).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureRetry, result.leftOrNull())
        }
    }

    @Test
    fun `method createAnalysis returns FailureDoNotRetry with Unknown Error on TransportException with not handled cause`() {
        mockGitCommunicationThrow(TransportException("whatever")).use {
            val result = service.generateAnalysis(validRepoUri)

            assertTrue(result.isFailure())
            assertEquals(FailureDoNotRetry(GitErrors.UNKNOWN_ERROR), result.leftOrNull())
        }
    }

    @Test
    fun `getAnalysis returns analysis when report exists`() {
        whenever(mockReportEntity.gitAnalysis).thenReturn(mockGitAnalysis)

        whenever(userReportRepo.findByIdAndUserId(validReportId, validUserId))
            .thenReturn(mockReportEntity)

        val result = service.getAnalysis(validReportId, validUserId)

        assertTrue(result.isSuccess())
        assertEquals(mockGitAnalysis, result.rightOrNull())
    }

    @Test
    fun `getAnalysis returns UserReportNotFound when report does not exist`() {
        whenever(userReportRepo.findByIdAndUserId(validReportId, validUserId))
            .thenReturn(null)

        val result = service.getAnalysis(validReportId, validUserId)

        assertTrue(result.isFailure())
        assertEquals(UserReportNotFound, result.leftOrNull())
    }

    @Test
    fun `createReportPDF returns pdf without persisting when reportId is null`() {
        val image = Base64.getEncoder().encodeToString("image".toByteArray())
        val pdfBytes = "pdf".toByteArray()

        whenever(reportGenerationService.createPdf(any()))
            .thenReturn(pdfBytes)

        val result = service.generatePDF(null, listOf(image))

        assertTrue(result.isSuccess())
        assertArrayEquals(pdfBytes, result.rightOrNull())

        verify(userReportRepo, never()).findById(any())
        verify(userReportRepo, never()).update(any())
    }

    @Test
    fun `createReportPDF persists pdf when report exists`() {
        val image = Base64.getEncoder().encodeToString("image".toByteArray())
        val pdfBytes = "pdf".toByteArray()

        whenever(reportGenerationService.createPdf(any()))
            .thenReturn(pdfBytes)

        whenever(userReportRepo.findById(validReportId))
            .thenReturn(mockReportEntity)

        val result = service.generatePDF(validReportId, listOf(image))

        assertTrue(result.isSuccess())
        assertArrayEquals(pdfBytes, result.rightOrNull())

        verify(userReportRepo).update(mockReportEntity)
        verify(mockReportEntity).pdf = pdfBytes
    }

    @Test
    fun `createReportPDF returns UserReportNotFound when report does not exist`() {
        val image = Base64.getEncoder().encodeToString("image".toByteArray())
        val pdfBytes = "pdf".toByteArray()

        whenever(reportGenerationService.createPdf(any()))
            .thenReturn(pdfBytes)

        whenever(userReportRepo.findById(validReportId))
            .thenReturn(null)

        val result = service.generatePDF(validReportId, listOf(image))

        assertTrue(result.isFailure())
        assertEquals(UserReportNotFound, result.leftOrNull())
    }

    @Test
    fun `getReportPDF returns pdf when present`() {
        val pdfBytes = "pdf".toByteArray()

        whenever(mockReportEntity.pdf).thenReturn(pdfBytes)

        whenever(userReportRepo.findByIdAndUserId(validReportId, validUserId))
            .thenReturn(mockReportEntity)

        val result = service.getReportPDF(validReportId, validUserId)

        assertTrue(result.isSuccess())
        assertArrayEquals(pdfBytes, result.rightOrNull())
    }

    @Test
    fun `getReportPDF returns UserReportNotFound when report does not exist`() {
        whenever(userReportRepo.findByIdAndUserId(validReportId, validUserId))
            .thenReturn(null)

        val result = service.getReportPDF(validReportId, validUserId)

        assertTrue(result.isFailure())
        assertEquals(UserReportNotFound, result.leftOrNull())
    }

    @Test
    fun `getReportPDF returns UserReportPDFNotGenerated when pdf is null`() {
        whenever(mockReportEntity.pdf).thenReturn(null)

        whenever(userReportRepo.findByIdAndUserId(validReportId, validUserId))
            .thenReturn(mockReportEntity)

        val result = service.getReportPDF(validReportId, validUserId)

        assertTrue(result.isFailure())
        assertEquals(UserReportPDFNotGenerated, result.leftOrNull())
    }

    private fun mockGitCommunication(): MockedStatic<GitCommunication.Companion> {
        val gitMock = Mockito.mockStatic(GitCommunication.Companion::class.java)

        gitMock.`when`<GitCommunication> {
            GitCommunication.create(validRepoUri)
        }.thenReturn(mockGitCommunication)

        return gitMock
    }

    private fun mockGitCommunicationThrow(
        e: Exception
    ): MockedStatic<GitCommunication.Companion> {

        val gitMock = Mockito.mockStatic(GitCommunication.Companion::class.java)

        gitMock.`when`<GitCommunication> {
            GitCommunication.create(validRepoUri)
        }.thenThrow(e)

        return gitMock
    }

    private fun mockGitAnalysis(): MockedStatic<GitAnalysis.Companion> {
        val analysisMock = Mockito.mockStatic(GitAnalysis.Companion::class.java)

        analysisMock.`when`<GitAnalysis> {
            GitAnalysis.create(mockGitCommunication)
        }.thenReturn(mockGitAnalysis)

        return analysisMock
    }

    //TODO: FIX THIS. Unable to mock GitCommunication properly.
}*/

//TODO: TESTS NEED TO BE REDONE AFTER MERGING CHANGED SOME OBJECTS