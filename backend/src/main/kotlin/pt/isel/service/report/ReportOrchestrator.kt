package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.entity.account.User
import pt.isel.entity.report.Report
import pt.isel.domain.report.GitAnalysis
import pt.isel.service.account.UserService
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.ReportNotFound
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.onFailure
import pt.isel.utils.onSuccess
import pt.isel.utils.rightOrNull
import pt.isel.utils.success

/**
 *  `ReportOrchestrator`
 *
 * The service layer orchestrator responsible for sequenced actions necessary for [ReportService]
 *
 * @property [userService] The service layer user-related operations.
 * @property [reportService] The service layer report-related operations.
 * @property [pdfGenerationService] The service layer pdf generator from a [GitAnalysis]
 * */
@Service
class ReportOrchestrator(
    private val userService: UserService,
    private val reportService: ReportService,
    private val pdfGenerationService: PDFGenerationService
){
    /**Creates a new [Report] owned by given user and generated from analysis
     *
     * @param [gitAnalysis] The analysis that generated the report.
     * @param [userId] The identifier of the [User] who generated the report.
     * @return
     * - [Int] the identifier of the created report on success.
     * - [UserNotFound] if no matching [User] found.
     * */
    fun createReport(gitAnalysis: GitAnalysis, userId: Int): Either<UserNotFound, Int> {
        val userResult = userService.findById(userId).onFailure { return(failure(it)) }
        val user = userResult.rightOrNull()!!

        return success(reportService.createReport(gitAnalysis, user))
    }
    /**@param [reportId] The report's unique identifier
     * @param [userId] The user who owns the report's unique identifier.
     * @return
     * - The [ReportNotFound] if no matching [Report] was found at any call to the persistence.
     * */
    fun getOrGenerateReportPDF(reportId: Int, userId: Int): Either<ReportNotFound, ByteArray> {
        reportService.getReportPDF(reportId, userId)
            .onSuccess { pdf -> return(success(pdf)) } //PDF is already generated
            .onFailure { if(it is ReportNotFound) return failure(it) } //No report found

        val getAnalysisResult = reportService.getReportAnalysis(reportId, userId)
            .onFailure { return(failure(it)) }  //If report was deleted mid-way due to race-condition
        val analysis = getAnalysisResult.rightOrNull()!!

        val pdf = pdfGenerationService.createPdf(analysis)  //Generates and persists a new pdf
        reportService.updateReportPdf(reportId, userId, pdf)
            .onFailure { return(failure(it)) } //If report was deleted mid-way due to race-condition
        return success(pdf)
    }
}