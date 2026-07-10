package pt.isel.domain.report

import org.eclipse.jgit.revwalk.RevCommit
import pt.isel.domain.DateInterval
import java.time.Instant

//TODO: DOCUMENT

class InvalidFiltersException : RuntimeException()

data class GitAnalysis(
    val searchInfo : SearchInfo,
    val llmAnalysis: String = "",

    val commitsByUser: Map<String,List<CommitAnalysis>>,
    val mostModifiedFiles: List<ModifiedFiles>?,

    val firstCommitTime: Instant,
    val lastCommitTime: Instant
){
    companion object{
        fun create(
            gitCommunication: GitCommunication,
            dateInterval: DateInterval? = null,
            llmAnalysis: String = ""
        ): GitAnalysis {

            val commits = gitCommunication.getCommits(dateInterval)

            if (commits.isEmpty()) {
                throw InvalidFiltersException()
            }

            val (first, last) = commits.first() to commits.last()

            return GitAnalysis(
                searchInfo = gitCommunication.getSearchInfo(),
                llmAnalysis = llmAnalysis,
                firstCommitTime = Instant.ofEpochSecond(first.commitTime.toLong()),
                lastCommitTime = Instant.ofEpochSecond(last.commitTime.toLong()),
                commitsByUser = commits.toCommitAnalysis(gitCommunication).byUser(),
                mostModifiedFiles = gitCommunication.getMostModifiedFiles(commits)
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