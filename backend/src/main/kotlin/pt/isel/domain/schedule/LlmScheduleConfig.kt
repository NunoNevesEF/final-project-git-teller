package pt.isel.domain.schedule

data class LlmScheduleConfig(
    val promptComplexity: String = "MEDIUM",
    val analysisMode: String = "DIFF",
    val requestedAnalyses: List<String> = listOf("DEFAULT"),
    val overviewOnly: Boolean = false
)
