package pt.isel.domain.report

import pt.isel.domain.DateInterval

/**
 * `GitAnalysisRequest`
 *
 * Represents the request parameters provided by a user when requesting a analysis
 *
 * @property [repoURI] the target repository.
 * @property [llmRequest] optional request parameters if user requests repository analysis by LLMs. Null if no request.
 * @property [dateFilter] optional request parameter for the timeframe the analysis should check. Null if no date filter.
 * @property [gitAccountId] optional request parameter indicating which of the user's git provider linked accounts should be used when cloning the repo as to access private repos. Null if no account.
 */
data class GitAnalysisRequest(
    val repoURI: String,
    val llmRequest: AnalysisRequestWrapper?,
    val dateFilter: DateInterval?,
    val gitAccountId: Int? = null
)