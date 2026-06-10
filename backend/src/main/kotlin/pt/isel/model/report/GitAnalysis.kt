package pt.isel.model.report

import org.eclipse.jgit.revwalk.RevCommit
import pt.isel.domain.SearchInfo
import pt.isel.domain.report.GitCommunication
import pt.isel.model.ModifiedFiles
import java.time.Instant


data class GitAnalysis(
    val searchInfo : SearchInfo,
    val llmAnalysis: String = "",

    val commitsByUser: Map<String,List<CommitAnalysis>>,
    val mostModifiedFiles: List<ModifiedFiles>?,

    val firstCommitTime: Instant,
    val lastCommitTime: Instant
){
    companion object{
        fun create(gitCommunication: GitCommunication, llmAnalysis: String = ""): GitAnalysis {
            val (firstCommitTime, lastCommitTime) = gitCommunication.getFirstAndLastCommitDate()
            return GitAnalysis(
                searchInfo = gitCommunication.getSearchInfo(),
                llmAnalysis = llmAnalysis,
                firstCommitTime = firstCommitTime,
                lastCommitTime = lastCommitTime,
                commitsByUser = gitCommunication.commits.toCommitAnalysis(gitCommunication).byUser(),
                mostModifiedFiles = gitCommunication.getMostModifiedFiles()
            )
        }
        private fun List<RevCommit>.toCommitAnalysis(gitCommunication: GitCommunication) =
            map{ commit ->
                val (additions, deletions) = gitCommunication.getCommitChanges(commit)
                CommitAnalysis.create(commit, additions, deletions)
            }

        private fun List<CommitAnalysis>.byUser(): Map<String, List<CommitAnalysis>> =
            groupBy{ it.author }
    }
}

data class CommitAnalysis(
    val commitId: String,
    val name: String,
    val author: String,
    val parentCount: Int,
    val timestamp: Instant,
    val message: String,
    val additions: Int,
    val deletions: Int,
){
    companion object{
        fun create(commit: RevCommit, additions: Int, deletions: Int): CommitAnalysis =
            CommitAnalysis(
                commitId = commit.id.name,
                name = commit.name,
                author = commit.authorIdent.name,
                parentCount = commit.parentCount,
                timestamp = Instant.ofEpochSecond(commit.commitTime.toLong()),
                message = commit.firstMessageLine,
                additions = additions,
                deletions = deletions
            )
    }
}