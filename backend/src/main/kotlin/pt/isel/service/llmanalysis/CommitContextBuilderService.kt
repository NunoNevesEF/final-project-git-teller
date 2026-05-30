package pt.isel.service.llmanalysis

import org.eclipse.jgit.revwalk.RevCommit
import org.springframework.stereotype.Service
import pt.isel.domain.CommitAnalysisContext
import pt.isel.domain.CommitFileChangeDto
import java.time.Instant

@Service
class CommitContextBuilderService {

    fun buildCommitContext(
        repoURI: String,
        commit: RevCommit,
        parent: RevCommit?,
        files: List<CommitFileChangeDto>
    ): CommitAnalysisContext {
        return CommitAnalysisContext(
            repoURI = repoURI,
            commitSha = commit.id.name,
            parentSha = parent?.id?.name,
            author = commit.authorIdent?.name ?: "Unknown",
            timestamp = Instant.ofEpochSecond(commit.commitTime.toLong()),
            message = commit.shortMessage,
            filesChanged = files.size,
            totalInsertions = files.sumOf { it.insertions },
            totalDeletions = files.sumOf { it.deletions },
            files = files
        )
    }
}