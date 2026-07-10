package pt.isel.gitteller.controller.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.controller.report.dto.ReportListItemDTO
import pt.isel.domain.report.CommitAnalysis
import pt.isel.domain.report.GitAnalysis
import pt.isel.domain.report.SearchInfo
import pt.isel.entity.account.User
import pt.isel.entity.report.Report
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ReportDTOTest {
    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    val validCommitAnalysis = CommitAnalysis(
        "test", "test", "test", 1, Instant.now(), "test", 1, 1
    )

    val validGitAnalysis = GitAnalysis(
        SearchInfo("test", "test", "test", "test"),
        "llm_test",
        mapOf(validUser.username!! to listOf(validCommitAnalysis)),
        null,
        Instant.now().minus(Duration.ofDays(1)),
        Instant.now()
    )

    private val validReport = Report(0, validUser, Instant.now(), validGitAnalysis)

    @Test
    fun `Report correctly maps to ReportListItemDTO`(){
        val expected = ReportListItemDTO(validReport.id, validReport.createdAt, validGitAnalysis.searchInfo.repositoryUrl)
        val actual = ReportListItemDTO(validReport)

        assertEquals(expected, actual)
    }
}