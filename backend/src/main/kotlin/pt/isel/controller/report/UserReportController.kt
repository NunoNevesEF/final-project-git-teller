package pt.isel.controller.report

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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.PDFGenerationContext
import pt.isel.model.report.ReportGenerationContext
import pt.isel.model.report.UserReportDTO
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.account.UserNotFound
import pt.isel.service.git.GitAnalysisService
import pt.isel.service.git.GitOutcome
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/report")
class UserReportPublicController(
    private val userReportService: UserReportService,
){
    @GetMapping("/generateReport")
    fun generateReportNoLLM(
        @RequestParam repoURI: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<GitAnalysis> {
        val context = when(val userId = userPrincipal?.getUserId()){
            null -> ReportGenerationContext.Guest
            else -> ReportGenerationContext.User(userId)
        }

        return when (val analysisResult = userReportService.generateAnalysis(context, repoURI)) {
            is Success -> ResponseEntity.ok(analysisResult.right.analysis)
            is Failure -> when(analysisResult.left){
                is UserNotFound -> ResponseEntity.notFound().build()
                is GitOutcome -> ResponseEntity.status(analysisResult.left.toStatus()).build()
                else -> ResponseEntity.badRequest().build()
            }
        }
    }

    //TODO: GENERATE REPORT WITH LLM.

    @PostMapping("/generatePDF")
    fun generatePdf(
        @RequestBody gitAnalysis: GitAnalysis,
        @RequestParam reportId: Int?,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<ByteArray> {
        val context = when(val userId = userPrincipal?.getUserId()){
            null -> PDFGenerationContext.Guest(gitAnalysis)
            else -> PDFGenerationContext.User(reportId, userId, gitAnalysis)
        }

        return when (val pdf = userReportService.generatePDF(context)){
            is Success -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/report")
class UserReportPrivateController(
    private val userReportService: UserReportService
) {
    @GetMapping("/user-reports")
    fun getUserReports(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<UserReportDTO>> {
        val reports = userReportService.getUserReportsByUserId(principal.getUserId())
        return ResponseEntity.ok(reports)
    }

    @GetMapping("/user-reports/{id}/analysis")
    fun getAnalysis(
        @PathVariable id: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<GitAnalysis> {
        return when(val analysis = userReportService.getAnalysis(id, principal.getUserId())){
            is Success -> ResponseEntity.ok(analysis.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/user-reports/{id}/download")
    fun downloadReport(
        @PathVariable id: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {
        return when(val pdf = userReportService.getReportPDF(id, principal.getUserId())) {
            is Success ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-$id.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}