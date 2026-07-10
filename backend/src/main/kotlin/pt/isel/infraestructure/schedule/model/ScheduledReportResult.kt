package pt.isel.infraestructure.schedule.model

import pt.isel.service.error.CouldNotDetermineAuthentication
import pt.isel.service.error.GitAnalysisServiceError
import pt.isel.service.error.GitConnectionTimeout
import pt.isel.service.error.InvalidFilter
import pt.isel.service.error.InvalidRepoUri
import pt.isel.service.error.RepoNotFoundOrPrivate
import pt.isel.service.error.UnknownGitAnalysisError

//TODO: DOCUMENT

sealed class ScheduledReportResult()
data class FailureDoRetry(val errorMsg: String) : ScheduledReportResult()
data class FailureDoNotRetry(val errorMsg: String): ScheduledReportResult()

fun GitAnalysisServiceError.toScheduledReportResult(): ScheduledReportResult = when (this) {
    CouldNotDetermineAuthentication -> FailureDoNotRetry("Authentication failed.")
    InvalidFilter -> FailureDoNotRetry("No results for given filters.")
    InvalidRepoUri -> FailureDoNotRetry("Invalid repository uri is invalid")
    RepoNotFoundOrPrivate -> FailureDoNotRetry("Repo not found or private repository is invalid")
    UnknownGitAnalysisError -> FailureDoNotRetry("An unexpected error occurred.")
    GitConnectionTimeout -> FailureDoRetry("Connection timed out.")
}