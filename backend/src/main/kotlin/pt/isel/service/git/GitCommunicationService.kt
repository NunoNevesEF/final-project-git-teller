package pt.isel.service.git

import jakarta.inject.Named
import pt.isel.domain.GitAnalysis
import pt.isel.repository.memory.GitCommunicationRepo

sealed class GitCommunicationServiceError
object RepoDoesNotExist: GitCommunicationServiceError()

@Named
class GitCommunicationService(private val gitCommunicationRepo: GitCommunicationRepo){
    fun getRepoAnalysis(repoURI: String): pt.isel.service.Either<RepoDoesNotExist, GitAnalysis> {
        return try{
            _root_ide_package_.pt.isel.service.success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI)))
        } catch(_ : Exception){ //TODO: REPLACE WITH EXPLICIT HANDLING FOR NON-EXISTING REPO
            _root_ide_package_.pt.isel.service.failure(RepoDoesNotExist)
        }

    }
}