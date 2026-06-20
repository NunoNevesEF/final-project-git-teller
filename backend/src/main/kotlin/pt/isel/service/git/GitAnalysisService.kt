package pt.isel.service.git

import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.internal.JGitText
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Service
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.domain.report.GitCommunication
import pt.isel.model.report.GitAnalysis
import pt.isel.service.ServiceError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.LinkedAccountServiceError
import pt.isel.service.llmanalysis.CommitAnalysisService
import pt.isel.utils.Either
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.utils.failure
import pt.isel.utils.success

sealed class GitAnalysisServiceError() : ServiceError {
    abstract fun toStatus(): Int
}

object FailureRetry : GitAnalysisServiceError() {
    override fun toStatus() = 504
}

class FailureDoNotRetry(val err: GitErrors) : GitAnalysisServiceError() {
    override fun toStatus() = when (err) {
        GitErrors.INVALID_REPO_URI -> 400
        GitErrors.REPO_NOT_FOUND -> 400
        GitErrors.AUTHENTICATION_ERROR -> 400
        GitErrors.UNKNOWN_ERROR -> 500
    }
}

enum class GitErrors {
    INVALID_REPO_URI, REPO_NOT_FOUND, AUTHENTICATION_ERROR, UNKNOWN_ERROR,
}

@Service
class GitAnalysisService(
    private val llmAnalysisService: CommitAnalysisService,  // Add this dependency
) {
    fun createAnalysis(repoUri: String, token: String? = null): Either<ServiceError, GitAnalysis> {
        try {
            val gitCommunication = GitCommunication.create(repoUri, token)
            return success(GitAnalysis.create(gitCommunication))
        } catch (e: Exception) {
            return handleGitException(e)
        }
    }

    fun analyzeCommit(
        repoURI: String,
        flag: Boolean,
        byShas: CommitShasAnalysisRequest?,
        byDateRange: CommitDateRangeAnalysisRequest?,
        token: String? = null,
    ): Either<GitAnalysisServiceError, GitAnalysis> {

        if (flag && byShas == null && byDateRange == null) {
            return try {
                val gitAnalysis = GitAnalysis.create(GitCommunication.create(repoURI, token))
                val llmAnalysis = llmAnalysisService.analyzeGitOverview(gitAnalysis).llmAnalysis
                success(gitAnalysis.copy(llmAnalysis = llmAnalysis))
            } catch (e: Exception) {
                handleGitException(e)
            }
        } else if (flag && byShas != null && byDateRange == null) { //lista de shas
            return try {
                val llmAnalysis = llmAnalysisService.analyzeCommitsByShas(byShas).llmAnalysis
                success(GitAnalysis.create(GitCommunication.create(repoURI, token), llmAnalysis))
            } catch (e: Exception) {
                handleGitException(e)
            }
        } else if (flag && byShas == null && byDateRange != null) { //Datas
            return try {
                val llmAnalysis = llmAnalysisService.analyzeCommitsBetweenDates(byDateRange).llmAnalysis
                success(GitAnalysis.create(GitCommunication.create(repoURI, token), llmAnalysis))
            } catch (e: Exception) {
                handleGitException(e)
            }
        } else {
            failure(llmAnalysisService)
        }

        return failure(FailureDoNotRetry(GitErrors.UNKNOWN_ERROR))
    }

    private fun handleGitException(e: Exception) = when (e) {
        is InvalidRemoteException -> failure(FailureDoNotRetry(GitErrors.INVALID_REPO_URI))
        is TransportException -> failure(handleTransport(e))
        else -> failure(FailureDoNotRetry(GitErrors.UNKNOWN_ERROR))
    }

    private fun handleTransport(e: TransportException): GitAnalysisServiceError {
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