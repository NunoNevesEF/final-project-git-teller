package pt.isel.domain

data class SearchInfo(
    val repositoryUrl: String,
    val repositoryName: String,
    val repositoryOwner: String,
    val platform: String,
)