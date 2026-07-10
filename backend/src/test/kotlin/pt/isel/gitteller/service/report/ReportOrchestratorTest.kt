package pt.isel.gitteller.service.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pt.isel.domain.report.GitAnalysis
import pt.isel.entity.account.User
import pt.isel.service.account.UserService
import pt.isel.service.error.UserNotFound
import pt.isel.service.report.PDFGenerationService
import pt.isel.service.report.ReportOrchestrator
import pt.isel.service.report.ReportService
import pt.isel.service.error.ReportNotFound
import pt.isel.service.error.ReportPDFNotFound
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ReportOrchestratorTest {

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var reportService: ReportService

    @Mock
    lateinit var pdfGenerationService: PDFGenerationService

    @InjectMocks
    lateinit var orchestrator: ReportOrchestrator

    private val validReportId = 0

    private val validUser = User(
        id = 0, email = "test@email.com", username = "test"
    )

    private val validAnalysis = mock<GitAnalysis>()

    private val validPDF = "pdf".toByteArray()

    @Test
    fun `method createReport creates report on success`() {

        whenever(userService.findById(validUser.id)).thenReturn(success(validUser))

        whenever(reportService.createReport(validAnalysis, validUser))
            .thenReturn(validReportId)

        val result = orchestrator.createReport(
            validAnalysis, validUser.id
        )

        verify(reportService).createReport(validAnalysis, validUser)

        assertEquals(success(validReportId), result)
    }

    @Test
    fun `method createReport returns UserNotFound from userService`() {

        whenever(userService.findById(validUser.id)).thenReturn(failure(UserNotFound))

        val result = orchestrator.createReport(validAnalysis, validUser.id)

        assertEquals(failure(UserNotFound), result)

        verify(reportService, never()).createReport(any(), any())
    }

    @Test
    fun `method getOrGenerateReportPDF returns stored pdf`() {

        whenever(reportService.getReportPDF(validReportId, validUser.id)).thenReturn(success(validPDF))

        val result = orchestrator.getOrGenerateReportPDF(
            validReportId, validUser.id
        )

        assertEquals(success(validPDF), result)

        verify(pdfGenerationService, never()).createPdf(any())
    }

    @Test
    fun `method getOrGenerateReportPDF returns ReportNotFound from reportService`() {

        whenever(reportService.getReportPDF(validReportId, validUser.id)).thenReturn(failure(ReportNotFound))

        val result = orchestrator.getOrGenerateReportPDF(
            validReportId, validUser.id
        )

        assertEquals(
            failure(ReportNotFound), result
        )
    }

    @Test
    fun `method getOrGenerateReportPDF generates new pdf if none stored`() {

        whenever(reportService.getReportPDF(validReportId, validUser.id)).thenReturn(failure(ReportPDFNotFound))

        whenever(reportService.getReportAnalysis(validReportId, validUser.id)).thenReturn(success(validAnalysis))

        whenever(pdfGenerationService.createPdf(validAnalysis)).thenReturn(validPDF)

        val result = orchestrator.getOrGenerateReportPDF(
            validReportId, validUser.id
        )

        verify(reportService).updateReportPdf(
            validReportId, validUser.id, validPDF
        )

        assertEquals(success(validPDF), result)
    }

    @Test
    fun `method getOrGenerateReportPDF returns ReportNotFound when deleted during getting stored analysis`() {

        whenever(reportService.getReportPDF(validReportId, validUser.id)).thenReturn(failure(ReportPDFNotFound))

        whenever(reportService.getReportAnalysis(validReportId, validUser.id)).thenReturn(failure(ReportNotFound))

        val result = orchestrator.getOrGenerateReportPDF(
            validReportId, validUser.id
        )

        assertEquals(
            failure(ReportNotFound), result
        )
    }

    @Test
    fun `method getOrGenerateReportPDF returns ReportNotFound when deleted during storing pdf`() {

        whenever(reportService.getReportPDF(validReportId, validUser.id)).thenReturn(failure(ReportPDFNotFound))

        whenever(reportService.getReportAnalysis(validReportId, validUser.id)).thenReturn(success(validAnalysis))

        whenever(pdfGenerationService.createPdf(validAnalysis)).thenReturn(validPDF)

        whenever(
            reportService.updateReportPdf(
                validReportId, validUser.id, validPDF
            )
        ).thenReturn(failure(ReportNotFound))

        val result = orchestrator.getOrGenerateReportPDF(
            validReportId, validUser.id
        )

        assertEquals(
            failure(ReportNotFound), result
        )
    }
}