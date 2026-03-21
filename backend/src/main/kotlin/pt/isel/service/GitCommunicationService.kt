package pt.isel.service

import jakarta.inject.Named
import pt.isel.domain.GitAnalysis
import pt.isel.repository.GitCommunicationRepo

sealed class GitCommunicationError
object RepoDoesNotExist: GitCommunicationError()

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