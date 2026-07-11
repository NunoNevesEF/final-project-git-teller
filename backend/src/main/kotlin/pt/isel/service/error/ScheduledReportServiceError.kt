package pt.isel.service.error

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import pt.isel.domain.report.schedule.BlankRepoUriException
import pt.isel.domain.report.schedule.InvalidJobCreationException
import pt.isel.domain.report.schedule.MalformedRepoUriException
import pt.isel.domain.report.schedule.ScheduleInvalidDateRangeException
import pt.isel.domain.report.schedule.ScheduledReportDomainException
import pt.isel.entity.report.model.JobStatus

/**
 * `ScheduledReportServiceError`
 *
 * Represents errors that can occur while executing scheduled report service operations.
 *
 * These errors are part of the service layer and can be translated into
 * HTTP status codes by the presentation layer.
 */
sealed class ScheduledReportServiceError: ServiceError

/** The supplied scheduled report arguments are invalid. */
class InvalidScheduledReportDomainArguments(val msg: String) : ScheduledReportServiceError()

/** No scheduled report with the requested identifier exists. */
object ScheduledReportNotFound: ScheduledReportServiceError()

/** The requested report has an unexpected or unsupported type. */
object UnexpectedReportType: ScheduledReportServiceError()

/**
 * Maps a [ScheduledReportDomainException] to the corresponding
 * [ScheduledReportServiceError].
 *
 * This function is intended for the service layer to translate
 * domain-layer errors into service-layer errors.
 *
 * @return the corresponding [ScheduledReportServiceError].
 */
fun ScheduledReportDomainException.toScheduledReportServiceError(): ScheduledReportServiceError = when (this) {
    is BlankRepoUriException -> InvalidScheduledReportDomainArguments("BlankRepoUriException")
    is MalformedRepoUriException -> InvalidScheduledReportDomainArguments("MalformedRepoUriException")
    is ScheduleInvalidDateRangeException -> InvalidScheduledReportDomainArguments("InvalidDateRangeException")
    is InvalidJobCreationException -> TODO("Intentionally left unmapped. This exception should be propagated.")
}

/**
 * Maps a [ScheduledReportServiceError] to the corresponding HTTP status code.
 *
 * This function is intended for the presentation layer to translate
 * service-layer errors into HTTP responses.
 *
 * @return the HTTP status code corresponding to this error.
 */
fun ScheduledReportServiceError.toHttpStatus(): HttpStatus = when(this){
    is InvalidScheduledReportDomainArguments -> HttpStatus.BAD_REQUEST
    is ScheduledReportNotFound -> HttpStatus.NOT_FOUND
    is UnexpectedReportType -> HttpStatus.BAD_REQUEST
}