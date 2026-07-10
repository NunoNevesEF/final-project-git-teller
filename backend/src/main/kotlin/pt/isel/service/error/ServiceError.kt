package pt.isel.service.error

import org.springframework.http.HttpStatus

sealed interface ServiceError

/**
 * Maps a [ServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun ServiceError.toHttpStatus(): HttpStatus = when(this){
    is UserServiceError -> this.toHttpStatus()
    is LinkedAccountServiceError -> this.toHttpStatus()
    is AuthServiceError -> this.toHttpStatus()
    is GitAnalysisServiceError -> this.toHttpStatus()
    is ReportServiceError -> this.toHttpStatus()
    is AnalysisOrchestratorError -> this.toHttpStatus()
    is ScheduledReportServiceError -> this.toHttpStatus()
}
