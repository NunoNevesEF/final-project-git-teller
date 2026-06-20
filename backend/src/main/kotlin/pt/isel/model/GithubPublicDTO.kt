package pt.isel.model

import java.time.Instant


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
    val updatedAt: Instant,
    val gitAccountId: Int
)

/*data class BranchSummary(
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
    val languages: Map<String, Int>
)

data class CommitDetailsSummary(
    val sha: String,
    val message: String,
    val author: String,
    val date: Instant,
    val htmlUrl: String,
    val additions: Int,
    val deletions: Int,
    val totalChanges: Int,
    val files: List<CommitFileSummary>
)

data class CommitFileSummary(
    val filename: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val changes: Int,
    val patch: String?
)*/