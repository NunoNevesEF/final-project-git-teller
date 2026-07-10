package pt.isel.domain.report

//TODO: DOCUMENT

data class ModifiedFiles(
    val path: String,
    val changes: Int,
    val lastModified: Long,
    val extension: String
)