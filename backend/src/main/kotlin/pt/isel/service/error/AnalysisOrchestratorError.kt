package pt.isel.service.error

import org.springframework.http.HttpStatus

/**
 *  `AnalysisOrchestratorErrors`
 *
 * Represents errors that can occur while executing the analysis-orchestrator operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 * */
sealed class AnalysisOrchestratorError: ServiceError
/**Expected the id of a linked account, but it wasn't found**/
object MissingGitLinkedAccountId: AnalysisOrchestratorError()

/**
 * Maps a [AnalysisOrchestratorError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun AnalysisOrchestratorError.toHttpStatus() = when (this) {
    is MissingGitLinkedAccountId -> HttpStatus.BAD_REQUEST
}