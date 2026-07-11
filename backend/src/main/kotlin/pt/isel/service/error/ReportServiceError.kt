package pt.isel.service.error

import org.springframework.http.HttpStatus

/**
 * `ReportServiceError`
 *
 * Represents errors that can occur while executing report-related service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 */
sealed class ReportServiceError : ServiceError

/** No report with the requested identifier exists. */
object ReportNotFound : ReportServiceError()

/** The PDF associated with the requested report does not exist. */
object ReportPDFNotFound: ReportServiceError()

/**
 * Maps a [ReportServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun ReportServiceError.toHttpStatus(): HttpStatus = when (this) {
        ReportNotFound -> HttpStatus.NOT_FOUND
        ReportPDFNotFound -> HttpStatus.NOT_FOUND
}