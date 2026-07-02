package pt.isel.service.git

import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.internal.JGitText
import org.springframework.stereotype.Service
import pt.isel.domain.DateInterval
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.report.GitCommunication
import pt.isel.model.report.GitAnalysis
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.ServiceError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.llmanalysis.CommitAnalysisService
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.utils.Success


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
class GitAnalysisService(private val llmAnalysisService: CommitAnalysisService) {

    fun analyze(
        request: GitAnalysisRequest,
        token: String?,
    ): Either<GitAnalysisServiceError, GitAnalysis> {
        return try {
            val git = buildGitAnalysis(request.repoURI,token, request.dateFilter)
            val enriched = enrichWithLLM(git, request)
            success(enriched)
        } catch (e: Exception) {
            handleGitException(e)
        }
    }

    fun buildGitAnalysis(repoURI: String, token: String?, dateInterval: DateInterval?): GitAnalysis {
        val gitComm = GitCommunication.create(repoURI, token)
        return GitAnalysis.create(gitComm, dateInterval)
    }

    fun enrichWithLLM(gitAnalysis: GitAnalysis, gitAnalysisRequest: GitAnalysisRequest): GitAnalysis {
        if (gitAnalysisRequest.llmRequest == null) return gitAnalysis
        val request = gitAnalysisRequest.llmRequest

        val llm = when {
            request.byShas != null ->
                llmAnalysisService.analyzeCommitsByShas(request.byShas, gitAnalysisRequest.repoURI)

            request.byDetailedSettings != null -> {
                llmAnalysisService.analyzeCommitsDetailedSettings(request.byDetailedSettings, gitAnalysisRequest.repoURI,gitAnalysisRequest.dateFilter)
            }

            else ->
                llmAnalysisService.analyzeGitOverview(gitAnalysis)
        }

        return gitAnalysis.copy(llmAnalysis = llm.llmAnalysis)
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
            msg.contains(JGitText.get().authenticationNotSupported) -> FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR)
            msg.contains(JGitText.get().noCredentialsProvider) -> FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR)
            msg.contains(JGitText.get().notAuthorized) -> FailureDoNotRetry(GitErrors.AUTHENTICATION_ERROR)
            msg.contains("408") || msg.contains("504") -> FailureRetry //TIMEOUT
            else -> return FailureDoNotRetry(GitErrors.UNKNOWN_ERROR)
        }
    }

    private fun rootCause(t: Throwable): Throwable = t.cause?.let(::rootCause) ?: t
}