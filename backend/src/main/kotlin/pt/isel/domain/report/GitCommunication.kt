package pt.isel.domain.report

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import pt.isel.model.report.ModifiedFiles
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import pt.isel.domain.DateInterval

data class GitCommunication(val git: Git, val repoURI: String) {
    val branches by lazy { getAllBranches() }

    companion object {
        fun create(repoURI: String, token: String?): GitCommunication {
            val git = getOrCloneGitRepo(repoURI, token)
            return GitCommunication(git, repoURI)
        }

        /**Temporary function for testing JGIT behavior, get a repo from gitRepos directory**/
        private fun getGitRepo(repoPathFile: File): Git {
            return Git.open(repoPathFile)
        }

        /**Temporary function for testing JGIT behavior, clones a repo to gitRepos directory**/
        private fun cloneGitRepo(repoURI: String, repoPathFile: File, token: String?): Git {
            val clone = Git.cloneRepository()
                .setURI(repoURI)
                .setDirectory(repoPathFile)
                .setBare(true)

            token?.let {
                clone.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider("oauth2", token)
                )
            }

            return clone.call()
        }

        /**Temporary function for testing JGIT behavior, tries to get a repo from gitRepos directory, if it doesn't exist then it clones**/
        private fun getOrCloneGitRepo(repoURI: String, token: String?): Git {
            val repoPathFile = getRepoFile(getRepoPath(repoURI))

            return try {
                getGitRepo(repoPathFile)
            } catch (e: RepositoryNotFoundException) {
                cloneGitRepo(repoURI, repoPathFile, token)
            }
        }

        /**Temporary function for testing JGIT behavior, creates directory path from git repo URI**/
        private fun getRepoPath(repoURI: String): String {
            val splitPath = repoURI.split("/").drop(2)
            val userDirectory = splitPath[1]
            val serviceDirectory = splitPath[0].removeSuffix(".com")
            val repoDirectory = splitPath[2]

            return "$reposStorageLocation/$userDirectory/$serviceDirectory/$repoDirectory"
        }

        /**Temporary function for testing JGIT behavior, creates file from directory path**/
        private fun getRepoFile(repoPath: String): File {
            return File(repoPath)
        }

        fun openExisting(repoURI: String): GitCommunication {
            val repoPath = getRepoPath(repoURI)
            val repoPathFile = File(repoPath)

            if (!repoPathFile.exists() /*|| !File(repoPathFile, ".git").exists()*/) {
                throw RepositoryNotFoundException("Local repository not found: $repoPath")
            }

            return GitCommunication(Git.open(repoPathFile), repoURI)
        }
    }

    fun getAllCommits(): List<RevCommit> {
        return git.log().all().call().toList()
    }

    fun logCommit(revCommit: RevCommit) {
        val footerLines = revCommit.footerLines
        println(
            "==== Commit: ====\n" + "id: ${revCommit.id.name}\n" + "name: ${revCommit.name}\n" + "author: ${revCommit.authorIdent.name}\n" + "commiter: ${revCommit.committerIdent.name}\n" + "time: ${
                Instant.ofEpochSecond(
                    revCommit.commitTime.toLong()
                )
            }\n" + "nº parents: ${revCommit.parentCount}\n" + "full message: ${revCommit.fullMessage}\n" + "short message: ${revCommit.shortMessage}\n" + "first line msg: ${revCommit.firstMessageLine}\n" + "Footer Lines: ${footerLines.size}\n"
        )
    }

    private fun getAllBranches(): List<Ref> {
        return git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
    }

    fun logBranch(branch: Ref) {
        println(
            "==== Branch ====\n" + "id: ${branch.objectId.name}\n" + "name: ${branch.name}"
        )
    }

    fun getCommitsByBranch(): MutableMap<String, List<RevCommit>> {
        val branchCommits = mutableMapOf<String, List<RevCommit>>()
        val mainBranch = git.repository.exactRef("HEAD")
        for (branch in branches) {
            val log = git.log().add(branch.objectId)
            if (branch.objectId != mainBranch.objectId) {
                log.not(mainBranch?.objectId)
            }
            val commits = log.call().toList()
            branchCommits[branch.name] = commits
        }
        return branchCommits
    }

    fun getSearchInfo(): SearchInfo {
        val uri = java.net.URI(repoURI)

        val segments = uri.path.trim('/').split('/')

        require(segments.size >= 2) {
            "Invalid repository URL: $repoURI"
        }

        val owner = segments[0]
        val repository = segments[1]

        val platform = when (uri.host.lowercase()) {
            "github.com" -> "github"
            "gitlab.com" -> "gitlab"
            else -> uri.host
        }

        return SearchInfo(
            repositoryUrl = repoURI,
            repositoryName = repository,
            repositoryOwner = owner,
            platform = platform.replaceFirstChar { it.uppercase() })
    }

    fun getCommitChanges(commit: RevCommit): Pair<Int, Int> {
        if (commit.parentCount == 0) {
            return Pair(0, 0)
        }

        val parent = commit.getParent(0)

        val diffs = git.diff().setOldTree(getTreeParser(parent)).setNewTree(getTreeParser(commit)).call()

        var additions = 0
        var deletions = 0

        val diffFormatter = DiffFormatter(ByteArrayOutputStream())
        diffFormatter.setRepository(git.repository)

        for (diff in diffs) {

            val edits = diffFormatter.toFileHeader(diff).toEditList()

            for (edit in edits) {

                additions += edit.endB - edit.beginB
                deletions += edit.endA - edit.beginA
            }
        }
        return Pair(additions, deletions)
    }

    fun getMostModifiedFiles(commits: List<RevCommit>): List<ModifiedFiles> {
        val fileStats = mutableMapOf<String, Pair<Int, Long>>()

        for (commit in commits) {
            if (commit.parentCount == 0) continue
            val parent = commit.getParent(0)

            val diffs = git.diff().setOldTree(getTreeParser(parent)).setNewTree(getTreeParser(commit)).call()

            val commitTime = commit.commitTime.toLong()
            for (diff in diffs) {
                val path = if (diff.newPath != "/dev/null") diff.newPath
                else diff.oldPath
                if (path == "/dev/null") continue
                val (currentChanges, currentLast) = fileStats[path] ?: (0 to 0L)
                val updatedChanges = currentChanges + 1
                val updatedLast = maxOf(currentLast, commitTime)

                fileStats[path] = updatedChanges to updatedLast
            }
        }

        return fileStats.entries.sortedByDescending { it.value.first }.take(10).map { (path, data) ->
                ModifiedFiles(
                    path = path,
                    changes = data.first,
                    lastModified = data.second,
                    extension = path.substringAfterLast('.', "")
                )
            }
    }

    fun getFirstAndLastCommitDate(commits: List<RevCommit>): Pair<Instant, Instant> {
        val lastCommitTime = Instant.ofEpochSecond(commits.first().commitTime.toLong())
        val firstCommitTime = Instant.ofEpochSecond(commits.last().commitTime.toLong())
        return Pair(firstCommitTime, lastCommitTime)
    }

    /**Temporary function for testing JGIT behavior, returns a list of diffs**/
    fun getGitDiff(oldCommit: RevCommit, newCommit: RevCommit): List<DiffEntry> {
        return git.diff().setOldTree(getTreeParser(oldCommit)).setNewTree(getTreeParser(newCommit)).call()
    }

    /**Temporary function for testing JGIT behavior, creates a readable complete diff (akin to git terminal command)**/
    fun formatDiff(diffEntries: List<DiffEntry>): List<String> {
        return diffEntries.map { formatDiffEntry(it) }
    }

    /**Temporary function for testing JGIT behavior, creates a readable diff entry (akin to git terminal command)**/
    fun formatDiffEntry(diffEntry: DiffEntry): String {
        val out = ByteArrayOutputStream()
        val df = DiffFormatter(out)
        df.setRepository(git.repository)
        df.format(diffEntry)
        return out.toString()
    }

    /**Temporary function for testing JGIT behavior, creates TreeIterator from Commit**/
    fun getTreeParser(commit: RevCommit): AbstractTreeIterator {
        val reader = git.repository.newObjectReader()
        val parser = CanonicalTreeParser()
        parser.reset(reader, commit.tree.id)
        return parser
    }

    fun findCommitBySha(commitSha: String): RevCommit? {
        val objectId =
            git.repository.resolve("${commitSha}^{commit}") ?: git.repository.resolve(commitSha) ?: return null

        return RevWalk(git.repository).use { walk ->
            walk.parseCommit(objectId)
        }
    }

    fun getMissingCommitShas(commitShas: List<String>): List<String> {
        return commitShas.filter { findCommitBySha(it) == null }
    }

    fun getCommitsByShas(commitShas: List<String>): List<RevCommit> {
        return commitShas.mapNotNull { findCommitBySha(it) }
    }

    fun getCommits(dateInterval: DateInterval?): List<RevCommit> {
        val walk = RevWalk(git.repository)
        val head = git.repository.resolve("HEAD")

        walk.markStart(walk.parseCommit(head))

        val result = mutableListOf<RevCommit>()

        for (commit in walk) {
            val time = Instant.ofEpochSecond(commit.commitTime.toLong())

            if (dateInterval != null) {
                if (time.isBefore(dateInterval.beginDate)) continue
                if (time.isAfter(dateInterval.endDate)) continue
            }

            result.add(commit)
        }

        walk.close()

        return result.sortedBy { it.commitTime }
    }
}

const val reposStorageLocation = "gitRepos"