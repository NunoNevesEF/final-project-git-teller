package pt.isel.service.analysis

import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.internal.JGitText
import org.springframework.stereotype.Service
import pt.isel.domain.DateInterval
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.report.GitCommunication
import pt.isel.model.report.GitAnalysis
import pt.isel.model.report.InvalidFiltersException
import pt.isel.service.error.CouldNotDetermineAuthentication
import pt.isel.service.error.GitAnalysisServiceError
import pt.isel.service.error.GitConnectionTimeout
import pt.isel.service.error.InvalidFilter
import pt.isel.service.error.InvalidRepoUri
import pt.isel.service.error.RepoNotFoundOrPrivate
import pt.isel.service.error.UnknownGitAnalysisError
import pt.isel.service.llmanalysis.CommitAnalysisService
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success

//TODO: DOCUMENT METHODS

/**
 *  `GitAnalysisService`
 *
 * The service layer git-analysis-related operations
 *
 * @property [llmAnalysisService] the service that deals with llm communication to analyse git analysis output or analysis repository.
 * */
@Service
class GitAnalysisService(private val llmAnalysisService: CommitAnalysisService) {

    fun analyze(
        request: GitAnalysisRequest,
        token: String?,
    ): Either<GitAnalysisServiceError, GitAnalysis> {
        return try {
            val git = GitCommunication.create(request.repoURI, token)
            try {
                val gitAnalysis = GitAnalysis.create(git, request.dateFilter)
                val enriched = enrichWithLLM(git, gitAnalysis, request)
                success(enriched)
            } finally {
                git.delete()
            }
        } catch (e: Exception) {
            handleGitException(e)
        }
    }

    fun enrichWithLLM(git: GitCommunication, gitAnalysis: GitAnalysis, gitAnalysisRequest: GitAnalysisRequest): GitAnalysis {
        if (gitAnalysisRequest.llmRequest == null) return gitAnalysis
        val request = gitAnalysisRequest.llmRequest

        val llm = when {
            request.byShas != null ->
                llmAnalysisService.analyzeCommitsByShas(request.byShas, git)

            request.byDetailedSettings != null -> {
                llmAnalysisService.analyzeCommitsDetailedSettings(request.byDetailedSettings, git, gitAnalysisRequest.dateFilter)
            }

            else ->
                llmAnalysisService.analyzeGitOverview(gitAnalysis)
        }

        return gitAnalysis.copy(llmAnalysis = llm.llmAnalysis)
    }

    private fun handleGitException(e: Exception) = when (e) {
        is InvalidFiltersException -> failure(InvalidFilter)
        is IllegalArgumentException -> failure(InvalidRepoUri)
        is InvalidRemoteException -> failure(InvalidRepoUri)
        is TransportException -> failure(handleTransport(e))
        else -> failure(UnknownGitAnalysisError)
    }

    private fun handleTransport(e: TransportException): GitAnalysisServiceError {
        val msg = e.message.orEmpty()
        return when {
            rootCause(e) is NoRemoteRepositoryException -> RepoNotFoundOrPrivate
            msg.contains(JGitText.get().authenticationNotSupported) -> CouldNotDetermineAuthentication
            msg.contains(JGitText.get().noCredentialsProvider) -> RepoNotFoundOrPrivate
            msg.contains(JGitText.get().notAuthorized) -> CouldNotDetermineAuthentication
            msg.contains("408") || msg.contains("504") -> GitConnectionTimeout
            else -> return UnknownGitAnalysisError
        }
    }

    private fun rootCause(t: Throwable): Throwable = t.cause?.let(::rootCause) ?: t
}