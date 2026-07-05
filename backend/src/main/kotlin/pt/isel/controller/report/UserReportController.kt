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
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.report.CreateUserReportDTO
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.UserReportDTO
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import java.net.URI

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/report")
class UserReportController(
    private val userReportService: UserReportService,
){
    @PostMapping("/create")
    fun createReport(
        @RequestBody dto: CreateUserReportDTO,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Int> {
        return when(val reportResult = userReportService.createReport(dto.gitAnalysis, userPrincipal.getUserId())){
            is Success -> ResponseEntity.created(URI("/private/report/${reportResult.right}")).body(reportResult.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }

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
    fun downloadPdf(
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