package pt.isel.controller.report

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.report.GitAnalysis
import pt.isel.service.report.ReportPDFGenerationService

@CrossOrigin(origins = ["https://frontend-production-fc0c.up.railway.app"])
@RestController
@RequestMapping("/api/public/pdf")
class PdfGenerationController(
    private val pdfGenerationService: ReportPDFGenerationService
){
    @PostMapping("/generate")
    fun generatePdf(
        @RequestBody gitAnalysis: GitAnalysis,
    ): ResponseEntity<ByteArray>{
        val pdf = pdfGenerationService.createPdf(gitAnalysis)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }
}
