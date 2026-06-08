package pt.isel.model.report

import java.time.Instant

data class UserReportDTO(
    val id: Int,
    val createdAt: Instant,
    val repoURI: String,
)

sealed class ReportGenerationContext {
    data class User(val userId: Int) : ReportGenerationContext()
    object Guest : ReportGenerationContext()
}

sealed class ReportGenerationResult(val analysis: GitAnalysis) {
    class GenerationResultGuest(analysis: GitAnalysis) : ReportGenerationResult(analysis)
    class GenerationResultUser(analysis: GitAnalysis, val reportId: Int) : ReportGenerationResult(analysis)
}

sealed class PDFGenerationContext(val analysis: GitAnalysis) {
    class User(val reportId: Int?, val userId: Int, analysis: GitAnalysis) : PDFGenerationContext(analysis)
    class Guest(analysis: GitAnalysis) : PDFGenerationContext(analysis)
}