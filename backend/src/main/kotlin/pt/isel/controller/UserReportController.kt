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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.UserReportDTO
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/report")
class UserReportPublicController(
    private val userReportService: UserReportService
){
    @GetMapping("/generateReport")
    fun getReport(
        @RequestParam repoURI: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal?
    ): ResponseEntity<GitAnalysis> {
        val userId = userPrincipal?.getUserId()

        if(userId != null){
            return when (val reportId = userReportService.createReport(userId, repoURI)){
                is Success -> when(val analysisResult = userReportService.getAnalysis(reportId.right, userId)){
                    is Success -> ResponseEntity.ok(analysisResult.right)
                    is Failure -> ResponseEntity.notFound().build()
                }
                is Failure -> ResponseEntity.badRequest().build()
            }
        }

        return when(val analysisResult = userReportService.createAnalysis(repoURI)){
            is Success -> ResponseEntity.ok(analysisResult.right)
            is Failure -> ResponseEntity.status(analysisResult.left.toStatus()).build()
        }
    }

    @PostMapping("/generatePDF")
    fun getPDF(
        @RequestBody images: List<String>,
        @RequestParam reportId: Int?
    ): ResponseEntity<ByteArray> {
        return when (val pdf = userReportService.createReportPDF(reportId, images)){
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

    @GetMapping("/user-reports/analysis/{id}")
    fun getAnalysis(
        @PathVariable id: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<GitAnalysis> {
        return when(val analysis = userReportService.getAnalysis(id, principal.getUserId())){
            is Success -> ResponseEntity.ok(analysis.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/user-reports/download/{id}")
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