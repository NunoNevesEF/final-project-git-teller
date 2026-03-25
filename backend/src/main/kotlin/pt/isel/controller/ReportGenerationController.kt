package pt.isel.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.GitAnalysis
import pt.isel.service.ReportGenerationService

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/report")
class ReportGenerationController(private val reportGenerationService: ReportGenerationService) {
    @PostMapping("/create")
    fun getGitAnalysis(
        @RequestBody gitAnalysis: GitAnalysis,
    ): ResponseEntity<ByteArray> {
        val pdf = reportGenerationService.createPdf(gitAnalysis)
        // Probably should not receive gitAnalysis, migrate when graph development is over
        // if request is from an authenticated user, we should eventually store the report according to user
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }
}