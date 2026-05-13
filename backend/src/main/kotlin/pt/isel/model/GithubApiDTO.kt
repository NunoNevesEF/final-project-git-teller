package pt.isel.model


import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant


internal data class GitHubEmailDTO(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)

internal data class GitHubRepositoryDTO(
    val id: Long,
    val name: String,
    @JsonProperty("full_name") val fullName: String,
    @JsonProperty("html_url") val htmlUrl: String,
    val description: String?,
    @JsonProperty("private") val isPrivate: Boolean,
    val language: String?,
    @JsonProperty("stargazers_count") val stargazersCount: Int,
    @JsonProperty("forks_count") val forksCount: Int,
    @JsonProperty("updated_at") val updatedAt: String
) {
    fun toSummary(): RepositorySummary = RepositorySummary(
        id = id,
        name = name,
        fullName = fullName,
        htmlUrl = htmlUrl,
        description = description,
        private = isPrivate,
        language = language,
        starsCount = stargazersCount,
        forksCount = forksCount,
        updatedAt = Instant.parse(updatedAt)
    )
}

/*internal data class GitHubBranchDTO(
    val name: String,
    val protected: Boolean,
    val commit: GitHubCommitReferenceDTO
) {
    fun toSummary(): BranchSummary = BranchSummary(
        name = name,
        protected = protected,
        lastCommitSha = commit.sha
    )
}

internal data class GitHubCommitReferenceDTO(val sha: String)

internal data class GitHubCommitDTO(
    val sha: String,
    val commit: GitHubCommitDataDTO,
    @JsonProperty("html_url") val htmlUrl: String,
    val author: GitHubUserDTO?
) {
    fun toSummary(): CommitSummary = CommitSummary(
        sha = sha,
        message = commit.message,
        author = author?.login ?: "Unknown",
        timestamp = Instant.parse(commit.author.date),
        htmlUrl = htmlUrl
    )
}

internal data class GitHubCommitDataDTO(val message: String, val author: GitHubAuthorDTO)
internal data class GitHubAuthorDTO(val name: String, val date: String)
internal data class GitHubUserDTO(val login: String)

internal data class GitHubCommitDetailsDTO(
    val sha: String,
    val commit: GitHubCommitDataDTO,
    @JsonProperty("html_url") val htmlUrl: String,
    val author: GitHubUserDTO?,
    val stats: GitHubCommitStatsDTO?,
    val files: List<GitHubCommitFileDTO>?
) {
    fun toSummary(): CommitDetailsSummary = CommitDetailsSummary(
        sha = sha,
        message = commit.message,
        author = author?.login ?: commit.author.name,
        date = Instant.parse(commit.author.date),
        htmlUrl = htmlUrl,
        additions = stats?.additions ?: 0,
        deletions = stats?.deletions ?: 0,
        totalChanges = stats?.total ?: 0,
        files = files?.map { it.toSummary() } ?: emptyList()
    )
}

internal data class GitHubCommitStatsDTO(val additions: Int, val deletions: Int, val total: Int)

internal data class GitHubCommitFileDTO(
    val filename: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val changes: Int,
    val patch: String?
) {
    fun toSummary(): CommitFileSummary = CommitFileSummary(
        filename = filename,
        status = status,
        additions = additions,
        deletions = deletions,
        changes = changes,
        patch = patch
    )
}*/