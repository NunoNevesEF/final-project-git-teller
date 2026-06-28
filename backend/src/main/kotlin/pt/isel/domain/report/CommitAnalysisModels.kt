package pt.isel.domain.report

import java.time.Instant

data class CommitFileChangeDto(
    val oldPath: String?,
    val newPath: String?,
    val changeType: String,
    val insertions: Int,
    val deletions: Int,
    val patch: String,
)

data class CommitAnalysisContext(
    val repoURI: String,
    val commitSha: String,
    val parentSha: String?,
    val author: String,
    val timestamp: Instant,
    val message: String,
    val filesChanged: Int,
    val totalInsertions: Int,
    val totalDeletions: Int,
    val files: List<CommitFileChangeDto>,
    )

data class CommitAnalysisResponse(
    val context: CommitAnalysisContext,
    val llmAnalysis: String,
)

data class BatchCommitAnalysisContext(
    val repoURI: String,
    val fromDate: Instant?,
    val toDate: Instant?,
    val commitCount: Int,
    val totalInsertions: Int,
    val totalDeletions: Int,
    val commits: List<CommitAnalysisContext>,
)

data class BatchCommitAnalysisResponse(
    val context: BatchCommitAnalysisContext,
    val llmAnalysis: String,
)


data class CommitAnalysisRequest(
    val repoURI: String,
    val commitSha: String,
    val maxCharsPerFile: Int = 3000,
    val promptComplexity: String = "MEDIUM",
    val analysisMode: String? = "DIFF",
    val requestedAnalyses: List<String> = listOf("DEFAULT")
)

data class CommitShasAnalysisRequest(
    val commitShas: List<String>,
    val maxCharsPerFile: Int = 5000,
    val promptComplexity: String = "MEDIUM",
    val analysisMode: String? = "DIFF",
    val requestedAnalyses: List<String> = listOf("DEFAULT")
)

data class CommitDetailedSettingsAnalysisRequest(
    val maxCommits: Int = 20,
    val maxCharsPerFile: Int = 5000,
    val promptComplexity: String = "MEDIUM",
    val analysisMode: String? = "DIFF",
    val requestedAnalyses: List<String> = listOf("DEFAULT")
)

