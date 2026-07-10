package pt.isel.gitteller.service.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pt.isel.model.report.GitAnalysis
import pt.isel.entity.account.User
import pt.isel.entity.report.Report
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.service.report.ReportService
import pt.isel.service.error.ReportNotFound
import pt.isel.utils.failure
import pt.isel.utils.success
import kotlin.collections.listOf
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {

    @Mock
    lateinit var repo: IReportRepository

    @InjectMocks
    lateinit var service: ReportService

    private val validReportId = 0

    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    private val validAnalysis = mock<GitAnalysis>()

    private val validPdf = "pdf".toByteArray()

    @Test
    fun `method createReport persists report`() {
        val report = Report(
            id = validReportId,
            user = validUser,
            gitAnalysis = validAnalysis
        )

        whenever(repo.create(any())).thenReturn(report)

        service.createReport(validAnalysis, validUser)

        verify(repo).create(any())
    }

    @Test
    fun `method getUserReportsByUserId returns reports`() {

        val reports = listOf(mock<Report>())

        whenever(repo.findByUserId(validUser.id))
            .thenReturn(reports)

        assertEquals(
            reports,
            service.getUserReportsByUserId(validUser.id)
        )
    }

    @Test
    fun `method getReportAnalysis returns analysis`() {

        val report = Report(
            id = validReportId,
            user = validUser,
            gitAnalysis = validAnalysis
        )

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(report)

        assertEquals(
            success(validAnalysis),
            service.getReportAnalysis(validReportId, validUser.id)
        )
    }

    @Test
    fun `method getReportAnalysis returns ReportNotFound`() {

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(null)

        assertEquals(
            failure(ReportNotFound),
            service.getReportAnalysis(validReportId, validUser.id)
        )
    }

    @Test
    fun `method getReportPDF returns pdf`() {

        val report = Report(
            id = validReportId,
            user = validUser,
            gitAnalysis = validAnalysis,
            pdf = validPdf
        )

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(report)

        assertEquals(
            success(validPdf),
            service.getReportPDF(validReportId, validUser.id)
        )
    }

    @Test
    fun `method getReportPDF returns ReportNotFound`() {

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(null)

        assertEquals(
            failure(ReportNotFound),
            service.getReportPDF(validReportId, validUser.id)
        )
    }

    @Test
    fun `method updateReportPdf updates repository`() {

        val report = Report(
            id = validReportId,
            user = validUser,
            gitAnalysis = validAnalysis
        )

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(report)

        val result = service.updateReportPdf(
            validReportId,
            validUser.id,
            validPdf
        )

        verify(repo).update(report.copy(pdf = validPdf))

        assertEquals(success(Unit), result)
    }

    @Test
    fun `method updateReportPdf returns ReportNotFound`() {

        whenever(repo.findByIdAndUserId(validReportId, validUser.id))
            .thenReturn(null)

        val result = service.updateReportPdf(
            validReportId,
            validUser.id,
            validPdf
        )

        assertEquals(
            failure(ReportNotFound),
            result
        )
    }
}
