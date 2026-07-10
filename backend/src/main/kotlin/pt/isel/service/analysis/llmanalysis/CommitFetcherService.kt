package pt.isel.service.analysis.llmanalysis

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.isel.domain.DateInterval
import pt.isel.domain.report.GitCommunication

//TODO: DOCUMENT

@Service
class CommitFetcherService {

    fun readCommitAndParent(repository: Repository, commitSha: String): Pair<RevCommit, RevCommit?> {
        val objectId = repository.resolve("${commitSha}^{commit}") ?: repository.resolve(commitSha)
        if (objectId == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Commit não encontrado: $commitSha")
        }

        return RevWalk(repository).use { walk ->
            val commit = walk.parseCommit(objectId)
            val parent = commit.parents.firstOrNull()?.let(walk::parseCommit)
            commit to parent
        }
    }

    fun getCommitsByShas(gitCommunication: GitCommunication, shas: List<String>): List<RevCommit> {
        return gitCommunication.getCommitsByShas(shas)
            .sortedBy { it.commitTime }
    }

    fun getCommitsBetweenDatesOrAll(
        gitCommunication: GitCommunication,
        dateInterval: DateInterval?,
        maxCommits: Int
    ): List<RevCommit> {
        return gitCommunication.getCommits(dateInterval).take(maxCommits.coerceIn(1, 200)).sortedBy { it.commitTime }
    }

    fun resolveParent(repository: Repository, commit: RevCommit): RevCommit? {
        val parentId = commit.parents.firstOrNull() ?: return null
        return RevWalk(repository).use { walk -> walk.parseCommit(parentId) }
    }
}