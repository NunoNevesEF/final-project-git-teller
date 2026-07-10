package pt.isel.controller.report

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
import pt.isel.entity.account.User
import pt.isel.model.report.GitAnalysis
import pt.isel.controller.report.dto.ReportListItemDTO
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.service.error.toHttpStatus
import pt.isel.service.report.ReportOrchestrator
import pt.isel.service.report.ReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.entity.report.Report

/**
 *  `ReportController`
 *
 * Exposes report related presentation-layer endpoints of the application.
 * */
@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/report")
class ReportController(
    private val reportOrchestrator: ReportOrchestrator,
    private val reportService: ReportService,
){
    /**
     * Stores a new [Report] for the given [User].
     *
     * Returns:
     * - `201 Created` if success
     * - `404 Not Found` if the user generating the report wasn't found
     *
     * @param gitAnalysis The analysis and it's request parameters that created the report.
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     *
     * @return An empty [ResponseEntity] with the appropriate HTTP status.
     *
     * @throws IllegalStateException If the password encoder unexpectedly returns `null`.
     */
    @PostMapping("/create")
    fun createReport(
        @RequestBody gitAnalysis: GitAnalysis,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Int> {
        return when(val result = reportOrchestrator.createReport(gitAnalysis, principal.getUserId())){
            is Success -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
    /**
     * Lists the [Report]s for the given [User].
     *
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     *
     * @return A [ResponseEntity] containing the list of [ReportListItemDTO] and status `200 Ok`
     */
    @GetMapping("/user-reports")
    fun getUserReports(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ReportListItemDTO>> {
        val reports = reportService.getUserReportsByUserId(principal.getUserId())
        return ResponseEntity.ok(reports.map{ ReportListItemDTO(it) })
    }
    /**
     * Gets the [GitAnalysis]s for the given [Report].
     *
     * Returns:
     * - `200 Ok` with the [GitAnalysis] on success.
     * - `404 Not found` if the [Report] matching [reportId] was not found.
     *
     * @param reportId The unique identifier for the [Report].
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     *
     * @return A [ResponseEntity] containing the found [GitAnalysis] on success, or an empty response with the appropriate HTTP status on failure.
     */
    @GetMapping("/user-reports/{reportId}/analysis")
    fun getAnalysis(
        @PathVariable reportId: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<GitAnalysis> {
        return when(val result = reportService.getReportAnalysis(reportId, principal.getUserId())){
            is Success -> ResponseEntity.ok(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
    /**
     * Gets the [GitAnalysis]s for the given [Report].
     *
     * Returns:
     * - `200 Ok` with the pdf on success.
     * - `404 Not found` if the [Report] matching [reportId] was not found.
     *
     * @param reportId The unique identifier for the [Report].
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     *
     * @return A [ResponseEntity] containing the found or generated pdf on success, or an empty response with the appropriate HTTP status on failure.
     */
    @GetMapping("/user-reports/{reportId}/download")
    fun downloadPdf(
        @PathVariable reportId: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {
        return when(val result = reportOrchestrator.getOrGenerateReportPDF(reportId, principal.getUserId())) {
            is Success ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-$reportId.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
}