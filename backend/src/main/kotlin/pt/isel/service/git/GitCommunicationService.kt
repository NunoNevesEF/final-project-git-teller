package pt.isel.service.git

import org.springframework.stereotype.Service
import pt.isel.controller.AnalysisRequestWrapper
import pt.isel.domain.BatchCommitAnalysisContext
import pt.isel.domain.BatchCommitAnalysisResponse
import pt.isel.domain.CommitAnalysisResponse
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.domain.GitAnalysis
import pt.isel.repository.memory.GitCommunicationRepo
import pt.isel.service.llmanalysis.CommitAnalysisService
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success

sealed class GitCommunicationServiceError
object RepoDoesNotExist: GitCommunicationServiceError()
object BadAnalysisParamters: GitCommunicationServiceError()

@Service
class GitCommunicationService(
    private val gitCommunicationRepo: GitCommunicationRepo,
    private val llmAnalysisService: CommitAnalysisService  // Add this dependency
) {

    fun getRepoAnalysis(repoURI: String): Either<RepoDoesNotExist, GitAnalysis> {
        return try{
            success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI)))
        } catch(_ : Exception){
            failure(RepoDoesNotExist)
        }
    }


    fun analyzeCommit(
        repoURI: String,
        flag: Boolean,
        byShas: CommitShasAnalysisRequest?,
        byDateRange: CommitDateRangeAnalysisRequest?,
    ): Either<GitCommunicationServiceError, GitAnalysis> {

        if (flag && byShas == null && byDateRange == null) {
            return try {
                val gitAnalysis = GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI))
                val llmAnalysis = llmAnalysisService.analyzeGitOverview(gitAnalysis).llmAnalysis
                success(gitAnalysis.copy(llmAnalysis = llmAnalysis))
            } catch (_ : Exception) {
                failure(RepoDoesNotExist)
            }
        }
        else if (flag && byShas != null && byDateRange == null){ //lista de shas
            return try {
                val llmAnalysis = llmAnalysisService.analyzeCommitsByShas(byShas).llmAnalysis
                success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI), llmAnalysis))
            } catch (_ : Exception) {
                failure(RepoDoesNotExist)
            }
        }
        else if (flag && byShas == null && byDateRange != null){ //Datas
            return try {
                val llmAnalysis = llmAnalysisService.analyzeCommitsBetweenDates(byDateRange).llmAnalysis
                success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI), llmAnalysis))
            } catch (_ : Exception) {
                failure(RepoDoesNotExist)
            }
        }
        else{
            failure(llmAnalysisService)
        }


        return failure(RepoDoesNotExist)
    }
}