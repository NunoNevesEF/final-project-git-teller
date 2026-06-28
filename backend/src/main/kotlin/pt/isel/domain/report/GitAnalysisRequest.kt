package pt.isel.domain.report

import pt.isel.domain.DateInterval

data class GitAnalysisRequest(
    val repoURI: String,
    val llmRequest: AnalysisRequestWrapper?,   // opcional llm
    val dateFilter: DateInterval?, // opcional data
    val gitAccountId: Int? = null              // required para ver repos privados
)