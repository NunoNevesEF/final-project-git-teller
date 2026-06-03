package pt.isel.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.CommitDTO
import pt.isel.domain.GitAnalysis
import pt.isel.domain.UserReportDto
import pt.isel.service.ReportGenerationService
import pt.isel.service.UserReportService
import java.util.Base64
import pt.isel.security.principal.UserPrincipal
import java.time.Instant

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/report")
class ReportGenerationController(
        private val reportGenerationService: ReportGenerationService,
        private val userReportService: UserReportService
) {
    @PostMapping("/create")
    fun getGitAnalysis(
        @RequestBody gitAnalysis: GitAnalysis,
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<ByteArray> {
        val pdf = reportGenerationService.createPdf(gitAnalysis)

        principal?.let {
            userReportService.create(it.getUserId(), pdf)
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }

    @GetMapping("/user-reports")
    fun getUserReports(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<UserReportDto>> {

        val reports = userReportService.getByUserId(principal.getUserId())

        return ResponseEntity.ok(reports)
    }

    @GetMapping("/user-reports/{id}/download")
    fun downloadReport(
        @PathVariable id: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {

        val pdf = userReportService.getReportPdf(id, principal.getUserId())

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-$id.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }


//    fun getMockAnalysis(): GitAnalysis {
//        return GitAnalysis(
//            commitsByUser = mapOf(
//                "48308-Nuno Neves" to listOf(
//                    CommitDTO(
//                        id = "123",
//                        name = "123",
//                        author = "48308-Nuno Neves",
//                        parentCount = 1,
//                        timestamp = Instant.now(),
//                        message = "KAN-32",
//                        additions = 285,
//                        deletions = 57
//                    )
//                )
//            ),
//            mostModifiedFiles = listOf(),
//            firstCommitTime = Instant.now(),
//            lastCommitTime = Instant.now()
//        )
//    }
}