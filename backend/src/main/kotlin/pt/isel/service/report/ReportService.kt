package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.entity.account.User
import pt.isel.entity.report.Report
import pt.isel.model.report.GitAnalysis
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.service.error.ReportNotFound
import pt.isel.service.error.ReportPDFNotFound
import pt.isel.service.error.ReportServiceError
import pt.isel.utils.*

/**
 *  `ReportService`
 *
 * The service layer report-related operations
 *
 * @property [reportRepo] The report repository used to communicate with the persistence layer.
 * */
@Service
class ReportService(
    private val reportRepo: IReportRepository,
) {
    /**Creates a new [Report] owned by given user and generated from analysis
     *
     * @param [gitAnalysis] The analysis that generated the report.
     * @param [user] The [User] who generated the report.
     * @return [Int] the identifier of the created report.
     * */
    fun createReport(gitAnalysis: GitAnalysis, user: User): Int{
        return reportRepo.create(Report(user = user, gitAnalysis = gitAnalysis)).id
    }
    /**@param [userId] The user who owns the report's unique identifier.
     * @return A list of the reports owned by user matching identifier.
     * */
    fun getUserReportsByUserId(userId: Int): List<Report> =
        reportRepo.findByUserId(userId)
    /**@param [reportId] The report's unique identifier
     * @param [userId] The user who owns the report's unique identifier.
     * @return
     * - The [GitAnalysis] that generated the report on success.
     * - [ReportNotFound] if no matching [Report] was found.
     * */
    fun getReportAnalysis(reportId: Int, userId: Int): Either<ReportNotFound, GitAnalysis> =
        reportRepo.findByIdAndUserId(reportId, userId).toEither { ReportNotFound }.map { it.gitAnalysis }
    /**@param [reportId] The report's unique identifier
     * @param [userId] The user who owns the report's unique identifier.
     * @return
     * - The pdf file generated from the report on success.
     * - [ReportNotFound] if no matching [Report] was found.
     * */
    fun getReportPDF(reportId: Int, userId: Int): Either<ReportServiceError, ByteArray> {
        val report = reportRepo.findByIdAndUserId(reportId, userId) ?: return failure(ReportNotFound)
        val pdf = report.pdf ?: return failure(ReportPDFNotFound)
        return success(pdf)
    }
    /**@param [reportId] The report's unique identifier
     * @param [userId] The user who owns the report's unique identifier.
     * @param [pdf] The pdf of the report to persist.
     * @return
     * - [ReportNotFound] if no matching [Report] was found.
     * */
    fun updateReportPdf(reportId: Int, userId: Int, pdf: ByteArray): Either<ReportNotFound, Unit> {
        val report = reportRepo.findByIdAndUserId(reportId, userId) ?: return failure(ReportNotFound)
        val updatedReport = report.copy(pdf = pdf)
        reportRepo.update(updatedReport)
        return success(Unit)
    }
}