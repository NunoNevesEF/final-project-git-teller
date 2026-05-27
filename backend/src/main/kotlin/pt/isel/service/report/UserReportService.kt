package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.domain.report.GitCommunication
import pt.isel.model.report.RepoAnalysis
import pt.isel.entity.ReportEntity
import pt.isel.model.report.UserReportDTO
import pt.isel.repository.interfaces.IUserReportRepository
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.map
import pt.isel.utils.success
import pt.isel.utils.toEither
import java.util.Base64
import kotlin.collections.map

sealed class GitCommunicationServiceError
object UserReportNotFound : GitCommunicationServiceError()
object UserReportPDFNotGenerated : GitCommunicationServiceError()
object UserNotFound : GitCommunicationServiceError()
object RepoDoesNotExist : GitCommunicationServiceError()

@Service
class UserReportService(
    private val userRepo: IUserRepository,
    private val userReportRepo: IUserReportRepository,
    private val reportGenerationService: ReportPDFGenerationService
) {
    fun createReport(userId: Int?, repoURI: String): Either<GitCommunicationServiceError, RepoAnalysis> {
        try {
            val gitCommunication = GitCommunication.create(repoURI)
            val repoAnalysis = RepoAnalysis.create(gitCommunication)

            if (userId != null) {
                val user = userRepo.findById(userId) ?: return failure(UserNotFound)
                userReportRepo.create(
                    ReportEntity(user = user, repoURI = repoURI, repoAnalysis = repoAnalysis)
                )
            }

            return success(repoAnalysis)
        } catch (_: Exception) { //TODO: REPLACE WITH EXPLICIT HANDLING FOR NON-EXISTING REPO
            return failure(RepoDoesNotExist)
        }
    }

    fun getUserReportsByUserId(userId: Int): List<UserReportDTO> =
        userReportRepo.findByUserId(userId).map { report ->
            UserReportDTO(id = report.id, createdAt = report.createdAt, repoURI = report.repoURI)
        }

    fun getAnalysis(reportId: Int, userId: Int): Either<UserReportNotFound, RepoAnalysis> =
        userReportRepo.findByIdAndUserId(reportId, userId)
            .toEither { UserReportNotFound }
            .map{ it.repoAnalysis }

    fun createReportPDF(reportId: Int?, images: List<String>): Either<UserReportNotFound, ByteArray> {
        val imagesBytes = images.map { Base64.getDecoder().decode(it) }
        val pdf = reportGenerationService.createPdf(imagesBytes)

        if(reportId != null) {
            val userReport = userReportRepo.findById(reportId) ?: return failure(UserReportNotFound)
            userReport.pdf = pdf
            userReportRepo.update(userReport)
        }

        return success(pdf)
    }

    fun getReportPDF(reportId: Int, userId: Int): Either<GitCommunicationServiceError, ByteArray> {
        val userReport = userReportRepo.findByIdAndUserId(reportId, userId) ?: return failure(UserReportNotFound)
        return userReport.pdf.toEither { UserReportPDFNotGenerated }
    }
}