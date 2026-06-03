package pt.isel.service.git

import org.springframework.stereotype.Service
import pt.isel.domain.GitAnalysis
import pt.isel.repository.memory.GitCommunicationRepo
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success

sealed class GitCommunicationServiceError
object RepoDoesNotExist: GitCommunicationServiceError()

@Service
class GitCommunicationService(private val gitCommunicationRepo: GitCommunicationRepo){
    fun getRepoAnalysis(repoURI: String): Either<RepoDoesNotExist, GitAnalysis> {
        return try{
            success(GitAnalysis.create(gitCommunicationRepo.getOrCreate(repoURI)))
        } catch(_ : Exception){ //TODO: REPLACE WITH EXPLICIT HANDLING FOR NON-EXISTING REPO
            failure(RepoDoesNotExist)
        }

    }
}