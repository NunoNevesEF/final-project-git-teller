package pt.isel.model.scheduledReport

data class DueScheduledJobItem(
    val reportId: Int,
    val userId: Int,
    val repoUri: String,
    val gitAccountId: Int?
)