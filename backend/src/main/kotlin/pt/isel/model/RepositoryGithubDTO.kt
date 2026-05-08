package pt.isel.model

import java.time.Instant

// DTOs for service responses (what we expose)
data class RepositorySummary(
    val id: Long,
    val name: String,
    val fullName: String,
    val htmlUrl: String,
    val description: String?,
    val private: Boolean,
    val language: String?,
    val starsCount: Int,
    val forksCount: Int,
    val updatedAt: Instant
)

data class BranchSummary(
    val name: String,
    val protected: Boolean,
    val lastCommitSha: String
)

data class CommitSummary(
    val sha: String,
    val message: String,
    val author: String,
    val timestamp: Instant,
    val htmlUrl: String
)

data class LanguagesSummary(
    val languages: Map<String, Int>  // e.g. {"Kotlin": 50000, "Java": 30000}
)

// DTOs for GitHub API responses (internal - parse GitHub's response format)
private data class GitHubEmailDTO(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)

private data class GitHubRepositoryDTO(
    val id: Long,
    val name: String,
    val full_name: String,
    val html_url: String,
    val description: String?,
    val private: Boolean,
    val language: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val updated_at: String
) {
    fun toSummary(): RepositorySummary = RepositorySummary(
        id = id,
        name = name,
        fullName = full_name,
        htmlUrl = html_url,
        description = description,
        private = private,
        language = language,
        starsCount = stargazers_count,
        forksCount = forks_count,
        updatedAt = Instant.parse(updated_at)
    )
}

private data class GitHubBranchDTO(
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

private data class GitHubCommitReferenceDTO(
    val sha: String
)

private data class GitHubCommitDTO(
    val sha: String,
    val commit: GitHubCommitDataDTO,
    val html_url: String,
    val author: GitHubUserDTO?
) {
    fun toSummary(): CommitSummary = CommitSummary(
        sha = sha,
        message = commit.message,
        author = author?.login ?: "Unknown",
        timestamp = Instant.parse(commit.author.date),
        htmlUrl = html_url
    )
}

private data class GitHubCommitDataDTO(
    val message: String,
    val author: GitHubAuthorDTO
)

private data class GitHubAuthorDTO(
    val name: String,
    val date: String
)

private data class GitHubUserDTO(
    val login: String
)