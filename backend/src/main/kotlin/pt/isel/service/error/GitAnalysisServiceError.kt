package pt.isel.service.error

import org.springframework.http.HttpStatus

/**
 *  `GitAnalysisServiceError`
 *
 * Represents errors that can occur while executing git-analysis-related service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 * */
sealed class GitAnalysisServiceError() : ServiceError
/**Invalid Repository Format**/
object InvalidRepoUri: GitAnalysisServiceError()
/**Repository not found or it's private.**/
object RepoNotFoundOrPrivate: GitAnalysisServiceError()
/**Due to JGIT behavior, this exception is only thrown when a repo does not exist and credentials were not passed as such is a 404 not found.**/
object CouldNotDetermineAuthentication : GitAnalysisServiceError()
/**Attempt to connect to repository timed out**/
object GitConnectionTimeout: GitAnalysisServiceError()
/**Invalid analysis filters. Ex: No data for given range.**/
object InvalidFilter : GitAnalysisServiceError()
/**An error of unknown origin**/
object UnknownGitAnalysisError: GitAnalysisServiceError()

/**
 * Maps a [GitAnalysisServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun GitAnalysisServiceError.toHttpStatus(): HttpStatus = when (this) {
    CouldNotDetermineAuthentication -> HttpStatus.NOT_FOUND
    InvalidFilter -> HttpStatus.BAD_REQUEST
    InvalidRepoUri -> HttpStatus.BAD_REQUEST
    RepoNotFoundOrPrivate -> HttpStatus.NOT_FOUND
    UnknownGitAnalysisError -> HttpStatus.INTERNAL_SERVER_ERROR
    GitConnectionTimeout -> HttpStatus.GATEWAY_TIMEOUT
}