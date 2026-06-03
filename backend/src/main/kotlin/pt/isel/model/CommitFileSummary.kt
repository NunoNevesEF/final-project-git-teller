package pt.isel.model

data class CommitFileSummary(
    val path: String,
    val changeType: String,
    val insertions: Int,
    val deletions: Int,
    val isRename: Boolean = false,
    val category: String? = null,
    val hotspotRank: Int? = null
)