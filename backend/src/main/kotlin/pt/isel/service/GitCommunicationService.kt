package pt.isel.service

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

data class GitCommunicationService(val git: Git) {
    companion object{
        fun create(repoURI: String): GitCommunicationService {
            val git = getOrCloneGitRepo(repoURI, getRepoFile(getRepoPath(repoURI)))
            return GitCommunicationService(git)
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
    }

    fun getNCommits(maxCommits: Int): List<RevCommit>{
        return git.log().setMaxCount(maxCommits).call().toList()
    }

    fun getAllCommits(): List<RevCommit>{
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

    fun getAllBranches(): List<Ref> {
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
        val branches = getAllBranches()
        val mainBranch = branches.firstOrNull {
            it.name.endsWith("/master") || it.name.endsWith("/main")
        }
        for(branch in branches){
            val log = git.log().add(branch.objectId)
            if(branch != mainBranch){ log.not(mainBranch?.objectId) }
            val commits = log.call().toList()
            branchCommits[branch.name] = commits
        }
        return branchCommits
    }

    fun getCommitsByUser(): Map<String, List<RevCommit>> {
        val commits = getAllCommits()
        return commits.groupBy{ it.authorIdent.name }
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
    val gitCommunicationService = GitCommunicationService.create(testRepoURI)
    val commitList = gitCommunicationService.getAllCommits()
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
}