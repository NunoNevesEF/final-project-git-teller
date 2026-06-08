package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.entity.ReportEntity
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.PDFGenerationContext
import pt.isel.model.report.ReportGenerationContext
import pt.isel.model.report.ReportGenerationResult
import pt.isel.model.report.UserReportDTO
import pt.isel.repository.interfaces.IUserReportRepository
import pt.isel.service.ServiceError
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.service.git.GitAnalysisService
import pt.isel.utils.*

sealed class GitCommunicationServiceError : ServiceError
object UserReportNotFound : GitCommunicationServiceError()
object UserReportNotPassed: GitCommunicationServiceError()
object UserReportPDFNotGenerated : GitCommunicationServiceError()

@Service
class UserReportService(
    private val userService: UserService,
    private val userReportRepo: IUserReportRepository,
    private val gitAnalysisService: GitAnalysisService,
    private val reportGenerationService: ReportPDFGenerationService
) {
    private fun createReport(gitAnalysis: GitAnalysis, userId: Int, repoUri: String): Either<UserNotFound, ReportGenerationResult> =
        when(val userResult = userService.findById(userId)){
            is Success -> {
                val report = userReportRepo.create(
                    ReportEntity(user = userResult.right, repoURI = repoUri, gitAnalysis = gitAnalysis)
                )
                success(ReportGenerationResult.GenerationResultUser(gitAnalysis, report.id))
            }
            is Failure -> userResult
        }

    fun getUserReportsByUserId(userId: Int): List<UserReportDTO> = userReportRepo.findByUserId(userId).map { report ->
        UserReportDTO(id = report.id, createdAt = report.createdAt, repoURI = report.repoURI)
    }

    private fun insertPDFOnReport(pdf: ByteArray, userId: Int, reportId: Int): Either<ServiceError, ByteArray> {
        when (val userResult = userService.findById(userId)) {
            is Success -> {
                val userReport = userReportRepo.findById(reportId) ?: return failure(UserReportNotFound)
                userReport.pdf = pdf
                userReportRepo.update(userReport)
                return success(pdf)
            }
            is Failure -> return userResult
        }
    }

    fun generateAnalysis(context: ReportGenerationContext, repoUri: String): Either<ServiceError, ReportGenerationResult> {
        val analysisResult = gitAnalysisService.createAnalysis(repoUri)
        if (analysisResult is Failure) return analysisResult

        val analysis = analysisResult.rightOrNull()!!

        return when (context) {
            is ReportGenerationContext.User -> createReport(analysis, context.userId, repoUri)
            is ReportGenerationContext.Guest -> success(ReportGenerationResult.GenerationResultGuest(analysis))
        }
    }

    fun getAnalysis(reportId: Int, userId: Int): Either<UserReportNotFound, GitAnalysis> =
        userReportRepo.findByIdAndUserId(reportId, userId).toEither { UserReportNotFound }.map { it.gitAnalysis }

    fun generatePDF(context: PDFGenerationContext): Either<ServiceError, ByteArray> {
        val pdf = reportGenerationService.createPdf(context.analysis)

        return when (context) {
            is PDFGenerationContext.User ->
                if(context.reportId == null) failure(UserReportNotPassed)
                else insertPDFOnReport(pdf, context.userId, context.reportId)
            is PDFGenerationContext.Guest -> success(pdf)
        }
    }

    fun getReportPDF(reportId: Int, userId: Int): Either<GitCommunicationServiceError, ByteArray> {
        val userReport = userReportRepo.findByIdAndUserId(reportId, userId) ?: return failure(UserReportNotFound)
        return userReport.pdf.toEither { UserReportPDFNotGenerated }
    }
}