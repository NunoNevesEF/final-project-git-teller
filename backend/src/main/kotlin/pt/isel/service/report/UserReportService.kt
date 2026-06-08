package pt.isel.service.report

import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.internal.JGitText
import org.springframework.stereotype.Service
import pt.isel.domain.report.GitCommunication
import pt.isel.entity.ReportEntity
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.UserReportDTO
import pt.isel.repository.interfaces.IUserReportRepository
import pt.isel.service.ServiceError
import pt.isel.service.account.UserService
import pt.isel.utils.*
import java.util.*

sealed class GitCommunicationServiceError : ServiceError
object UserReportNotFound : GitCommunicationServiceError()
object UserReportPDFNotGenerated : GitCommunicationServiceError()

sealed class GitOutcome: GitCommunicationServiceError(){
    abstract fun toStatus(): Int
}
object FailureRetry : GitOutcome() {
    override fun toStatus() = 504
}

class FailureDoNotRetry(val err: GitErrors) : GitOutcome() {
    override fun toStatus() = when(err){
        GitErrors.INVALID_REPO_URI -> 400
        GitErrors.REPO_NOT_FOUND -> 400
        GitErrors.AUTHENTICATION_ERROR -> 400
        GitErrors.UNKNOWN_ERROR -> 500
    }
}

enum class GitErrors {
    INVALID_REPO_URI,
    REPO_NOT_FOUND,
    AUTHENTICATION_ERROR,
    UNKNOWN_ERROR,
}

@Service
class UserReportService(
    private val userService: UserService,
    private val userReportRepo: IUserReportRepository,
    private val reportGenerationService: ReportPDFGenerationService
) {
    fun createReport(userId: Int, repoUri: String): Either<ServiceError, Int> {
        when (val userResult = userService.findById(userId)) {
            is Success -> {
                when (val analysisResult = createAnalysis(repoUri)) {
                    is Success -> {
                        val report = userReportRepo.create(
                            ReportEntity(
                                user = userResult.right, repoURI = repoUri, gitAnalysis = analysisResult.right
                            )
                        )
                        return success(report.id)
                    }
                    is Failure -> return analysisResult
                }
            }
            is Failure -> return userResult
        }
    }

    fun getUserReportsByUserId(userId: Int): List<UserReportDTO> = userReportRepo.findByUserId(userId).map { report ->
        UserReportDTO(id = report.id, createdAt = report.createdAt, repoURI = report.repoURI)
    }

    fun createAnalysis(repoUri: String): Either<GitOutcome, GitAnalysis> {
        try {
            val gitCommunication = GitCommunication.create(repoUri)
            return success(GitAnalysis.create(gitCommunication))
        } catch (e: Exception) {
            return when (e) {
                is InvalidRemoteException -> failure(FailureDoNotRetry(GitErrors.INVALID_REPO_URI))
                is TransportException -> failure(handleTransport(e))
                else -> failure(FailureDoNotRetry(GitErrors.UNKNOWN_ERROR))
            }
        }
    }

    fun getAnalysis(reportId: Int, userId: Int): Either<UserReportNotFound, GitAnalysis> =
        userReportRepo.findByIdAndUserId(reportId, userId).toEither { UserReportNotFound }.map { it.gitAnalysis }

    fun createReportPDF(reportId: Int?, images: List<String>): Either<UserReportNotFound, ByteArray> {
        val imagesBytes = images.map { Base64.getDecoder().decode(it) }
        val pdf = reportGenerationService.createPdf(imagesBytes)

        if (reportId != null) {
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

    private fun handleTransport(e: TransportException): GitOutcome {
        val msg = e.message.orEmpty()
        return when {
            rootCause(e) is NoRemoteRepositoryException -> FailureDoNotRetry(GitErrors.REPO_NOT_FOUND)
            msg == JGitText.get().authenticationNotSupported -> FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR)
            msg == JGitText.get().notAuthorized -> FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR)
            msg.contains("408") || msg.contains("504") -> FailureRetry //TIMEOUT
            else -> return FailureDoNotRetry(GitErrors.UNKNOWN_ERROR)
        }
    }

    private fun rootCause(t: Throwable): Throwable = t.cause?.let(::rootCause) ?: t
}