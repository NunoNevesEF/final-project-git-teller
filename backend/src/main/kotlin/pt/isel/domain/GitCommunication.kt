package pt.isel.domain

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import kotlin.collections.iterator
import org.eclipse.jgit.revwalk.RevWalk

data class GitAnalysis(
    val commitsByUser: Map<String, List<CommitDTO>>,
//    val commitsByBranch: Map<String, List<CommitDTO>>,
    val mostModifiedFiles : List<Pair<String, Int>>?,
    val firstCommitTime: Instant,
    val lastCommitTime: Instant,
){
    companion object {
        fun create(gitCommunication: GitCommunication): GitAnalysis {
            val (firstCommitTime, lastCommitTime) = gitCommunication.getFirstAndLastCommitDate()
            return GitAnalysis(
                commitsByUser = gitCommunication.getCommitsByUser(),
//                gitCommunication.getCommitsByBranch().mapValues{ (_, commitList) ->
//                    commitList.map{ CommitDTO.create(it)}
//                },
                mostModifiedFiles = gitCommunication.getMostModifiedFiles(),
                firstCommitTime,
                lastCommitTime
            )
        }
    }
}

class CommitDTO(
    val id: String,
    val name: String,
    val author: String,
    val parentCount: Int,
    val timestamp: Instant,
    val message: String,
    val additions: Int,
    val deletions: Int
){
    companion object {
        fun create(revCommit: RevCommit, additions: Int, deletions: Int): CommitDTO {
            return CommitDTO(
                id = revCommit.id.name,
                name = revCommit.name,
                author = revCommit.authorIdent.name,
                parentCount = revCommit.parentCount,
                timestamp = Instant.ofEpochSecond(revCommit.commitTime.toLong()),
                message = revCommit.firstMessageLine,
                additions = additions,
                deletions = deletions
            )
        }
    }
}

data class GitCommunication(val git: Git) {
    val commits by lazy{ getAllCommits() }
    val branches by lazy{ getAllBranches() }

    companion object{
        fun create(repoURI: String): GitCommunication {
            val git = getOrCloneGitRepo(repoURI, getRepoFile(getRepoPath(repoURI)))
            return GitCommunication(git)
        }

        /**Temporary function for testing JGIT behavior, get a repo from gitRepos directory**/
        private fun getGitRepo(repoPathFile: File): Git{
            return Git.open(repoPathFile)
        }

        /**Temporary function for testing JGIT behavior, clones a repo to gitRepos directory**/
        private fun cloneGitRepo(repoURI: String, repoPathFile: File): Git {
            return Git.cloneRepository()
                .setURI(repoURI)
                .setDirectory(repoPathFile)
                .call()
        }

        /**Temporary function for testing JGIT behavior, tries to get a repo from gitRepos directory, if it doesn't exist then it clones**/
        private fun getOrCloneGitRepo(repoURI: String, repoPathFile: File): Git {
            return try{
                getGitRepo(repoPathFile)
            } catch(e: RepositoryNotFoundException){
                cloneGitRepo(repoURI, repoPathFile)
            }
        }

        /**Temporary function for testing JGIT behavior, creates directory path from git repo URI**/
        private fun getRepoPath(repoURI: String): String{
            val splitPath = repoURI.split("/").drop(2)
            val userDirectory = splitPath[1]
            val serviceDirectory = splitPath[0].removeSuffix(".com")
            val repoDirectory = splitPath[2]

            return "$reposStorageLocation/$userDirectory/$serviceDirectory/$repoDirectory"
        }

        /**Temporary function for testing JGIT behavior, creates file from directory path**/
        private fun getRepoFile(repoPath: String): File{
            return File(repoPath)
        }

        fun openExisting(repoURI: String): GitCommunication {
            val repoPath = getRepoPath(repoURI)
            val repoPathFile = File(repoPath)

            if (!repoPathFile.exists() || !File(repoPathFile, ".git").exists()) {
                throw RepositoryNotFoundException("Local repository not found: $repoPath")
            }

            return GitCommunication(Git.open(repoPathFile))
        }
    }

    /*fun getNCommits(maxCommits: Int): List<RevCommit>{
        return git.log().setMaxCount(maxCommits).call().toList()
    }*/

    private fun getAllCommits(): List<RevCommit>{
        return git.log().all().call().toList()
    }

    fun logCommit(revCommit: RevCommit){
        val footerLines = revCommit.footerLines
        println("==== Commit: ====\n" +
                "id: ${revCommit.id.name}\n" +
                "name: ${revCommit.name}\n" +
                "author: ${revCommit.authorIdent.name}\n" +
                "commiter: ${revCommit.committerIdent.name}\n" +
                "time: ${Instant.ofEpochSecond(revCommit.commitTime.toLong())}\n" +
                "nº parents: ${revCommit.parentCount}\n" +
                "full message: ${revCommit.fullMessage}\n" +
                "short message: ${revCommit.shortMessage}\n" +
                "first line msg: ${revCommit.firstMessageLine}\n" +
                "Footer Lines: ${footerLines.size}\n"
        )
    }

    private fun getAllBranches(): List<Ref> {
        return git.branchList()
            .setListMode(ListBranchCommand.ListMode.ALL)
            .call()
    }

    fun logBranch(branch: Ref){
        println("==== Branch ====\n" +
                "id: ${branch.objectId.name}\n" +
                "name: ${branch.name}")
    }

    fun getCommitsByBranch(): MutableMap<String, List<RevCommit>> {
        val branchCommits = mutableMapOf<String, List<RevCommit>>()
        val mainBranch = git.repository.exactRef("HEAD")
        for(branch in branches){
            val log = git.log().add(branch.objectId)
            if(branch.objectId != mainBranch.objectId){ log.not(mainBranch?.objectId) }
            val commits = log.call().toList()
            branchCommits[branch.name] = commits
        }
        return branchCommits
    }

    fun getCommitsByUser(): Map<String, List<CommitDTO>> {

        return commits
            .groupBy { it.authorIdent.name }
            .mapValues { (_, commits) ->

                commits.map { commit ->

                    val (additions, deletions) =
                        getCommitChanges(commit)

                    CommitDTO(
                        id = commit.id.name,
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
    }

    private fun getCommitChanges(commit: RevCommit): Pair<Int, Int> {
        if (commit.parentCount == 0) {
            return Pair(0, 0)
        }

        val parent = commit.getParent(0)

        val diffs = git.diff()
            .setOldTree(getTreeParser(parent))
            .setNewTree(getTreeParser(commit))
            .call()

        var additions = 0
        var deletions = 0

        val diffFormatter = DiffFormatter(ByteArrayOutputStream())
        diffFormatter.setRepository(git.repository)

        for (diff in diffs) {

            val edits = diffFormatter
                .toFileHeader(diff)
                .toEditList()

            for (edit in edits) {

                additions += edit.endB - edit.beginB
                deletions += edit.endA - edit.beginA
            }
        }
        return Pair(additions, deletions)
    }

    fun getMostModifiedFiles(): List<Pair<String, Int>>? {
        val fileFrequency = mutableMapOf<String, Int>()

        for ( commit in commits) {
            if (commit.parentCount == 0) continue

            val parent = commit.getParent(0)

            val diffs = git.diff()
                .setOldTree(getTreeParser(parent))
                .setNewTree(getTreeParser(commit))
                .call()

            for (diff in diffs) {

                val path =
                    if (diff.newPath != "/dev/null")
                        diff.newPath
                    else
                        diff.oldPath

                if (path == "/dev/null") continue

                fileFrequency[path] =
                    (fileFrequency[path] ?: 0) + 1
            }
        }
        return fileFrequency.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { Pair(it.key, it.value) };
    }

    fun getFirstAndLastCommitDate(): Pair<Instant, Instant> {
        val lastCommitTime = Instant.ofEpochSecond(commits.first().commitTime.toLong())
        val firstCommitTime = Instant.ofEpochSecond(commits.last().commitTime.toLong())
        return Pair(firstCommitTime, lastCommitTime)
    }

    /**Temporary function for testing JGIT behavior, returns a list of diffs**/
    fun getGitDiff(oldCommit: RevCommit, newCommit: RevCommit): List<DiffEntry>{
        return git.diff()
            .setOldTree(getTreeParser(oldCommit))
            .setNewTree(getTreeParser(newCommit))
            .call()
    }

    /**Temporary function for testing JGIT behavior, creates a readable complete diff (akin to git terminal command)**/
    fun formatDiff(diffEntries: List<DiffEntry>): List<String>{
        return diffEntries.map{ formatDiffEntry(it) }
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
        val objectId = git.repository.resolve("${commitSha}^{commit}")
            ?: git.repository.resolve(commitSha)
            ?: return null

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

    fun getCommitsBetween(from: Instant, to: Instant): List<RevCommit> {
        require(!from.isAfter(to)) { "fromDate não pode ser depois de toDate" }

        return commits
            .filter { commit ->
                val commitInstant = Instant.ofEpochSecond(commit.commitTime.toLong())
                !commitInstant.isBefore(from) && !commitInstant.isAfter(to)
            }
            .sortedBy { it.commitTime }
    }


}

/**Temporary function for testing JGIT behavior, Lazy get repo, doesn't verify it exists on creation**/
/*
fun getGitRepoLazy(repoPath: String): Git{
    val existingRepo : Repository = FileRepositoryBuilder()
        .setGitDir(File(repoPath))
        .build()
    return Git(existingRepo)
}*/

/**Temporary function for testing JGIT behavior, returns gitRepo Status.**/
/*
fun getGitRepoStatus(repo: Git): Status {
    return repo.status().call()
}*/

const val reposStorageLocation = "gitRepos"
const val testRepoURISmall = "https://github.com/octocat/Hello-World"
const val testRepoURILarge = "https://github.com/github/testrepo"
const val testRepoURI = testRepoURILarge

fun main(){
    val gitCommunicationService = GitCommunication.create(testRepoURI)
    val commitList = gitCommunicationService.commits
    /*commitList.forEachIndexed { i, commit ->
        println("${i+1}.")
        gitCommunicationService.logCommit(commit)}
    val branchList = gitCommunicationService.getAllBranches()
    branchList.forEachIndexed { i, branch ->
        println("${i+1}..")
        gitCommunicationService.logBranch(branch)
    }*/
    println("\nBRANCH TEST")
    val branchCommits = gitCommunicationService.getCommitsByBranch()
    for(branch in branchCommits){
        println("=== ${branch.key} ===")
        println("Commits in Branch: ${branch.value.size}")
        branch.value.forEachIndexed{ i, commit ->
            println("${i+1}. ${commit.name}")
        }
        println()
    }
    println("\nUSER TEST")
    val userCommits = gitCommunicationService.getCommitsByUser()
    for(user in userCommits){
        println("=== ${user.key} ===")
        println("Commits by User: ${user.value.size}")
        user.value.forEachIndexed{ i, commit ->
            println("${i+1}. ${commit.name}")
        }
        println()
    }
    println("\nCOMMIT TIME TEST")
    val (firstCommitDate, lastCommitDate) = gitCommunicationService.getFirstAndLastCommitDate()
    println("First Commit: $firstCommitDate")
    println("Last Commit: $lastCommitDate")
}