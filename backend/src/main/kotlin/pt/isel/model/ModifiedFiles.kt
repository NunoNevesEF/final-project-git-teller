package pt.isel.model

data class ModifiedFiles(
    val path: String,
    val changes: Int,
    val lastModified: Long,
    val extension: String
)