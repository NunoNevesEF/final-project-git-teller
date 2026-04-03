package pt.isel.service

import jakarta.inject.Named
import pt.isel.domain.GitAnalysis
import pt.isel.repository.memory.GitCommunicationRepo

sealed class GitCommunicationServiceError
object RepoDoesNotExist: GitCommunicationServiceError()

@Named
class GitCommunicationService(private val gitCommunicationRepo: GitCommunicationRepo){
    fun getRepoAnalysis(repoURI: String): Either<RepoDoesNotExist, GitAnalysis> {
        return try{
            success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI)))
        } catch(_ : Exception){ //TODO: REPLACE WITH EXPLICIT HANDLING FOR NON-EXISTING REPO
            failure(RepoDoesNotExist)
        }

    }
}