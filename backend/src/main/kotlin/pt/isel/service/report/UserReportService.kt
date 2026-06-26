package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.entity.ReportEntity
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.UserReportDTO
import pt.isel.repository.interfaces.IUserReportRepository
import pt.isel.service.ServiceError
import pt.isel.service.account.UserNotFound
import pt.isel.service.account.UserService
import pt.isel.utils.*

sealed class GitCommunicationServiceError : ServiceError
object UserReportNotFound : GitCommunicationServiceError()

@Service
class UserReportService(
    private val userService: UserService,
    private val userReportRepo: IUserReportRepository,
    private val reportGenerationService: ReportPDFGenerationService
) {
    fun createReport(gitAnalysis: GitAnalysis, repoUri: String, userId: Int): Either<UserNotFound, Int> =
        when(val userResult = userService.findById(userId)){
            is Success -> {
                val report = userReportRepo.create(
                    ReportEntity(user = userResult.right, repoURI = repoUri, gitAnalysis = gitAnalysis)
                )
                success(report.id)
            }
            is Failure -> userResult
        }

    fun getUserReportsByUserId(userId: Int): List<UserReportDTO> = userReportRepo.findByUserId(userId).map { report ->
        UserReportDTO(id = report.id, createdAt = report.createdAt, repoUri = report.repoURI)
    }

    fun getAnalysis(reportId: Int, userId: Int): Either<UserReportNotFound, GitAnalysis> =
        userReportRepo.findByIdAndUserId(reportId, userId).toEither { UserReportNotFound }.map { it.gitAnalysis }

    fun getReportPDF(reportId: Int, userId: Int): Either<GitCommunicationServiceError, ByteArray> {
        val pdf = userReportRepo.findByIdAndUserId(reportId, userId)?.pdf ?: return generatePDF(reportId, userId)
        return success(pdf)
    }

    private fun generatePDF(reportId: Int, userId: Int): Either<GitCommunicationServiceError, ByteArray> {
        val userReport = userReportRepo.findByIdAndUserId(reportId, userId) ?: return failure(UserReportNotFound)
        val pdf = reportGenerationService.createPdf(userReport.gitAnalysis)
        userReport.pdf = pdf
        userReportRepo.update(userReport)
        return success(pdf)
    }
}