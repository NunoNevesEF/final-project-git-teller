package pt.isel.service

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.ByteArrayOutputStream
import java.io.File


/**Temporary function for testing JGIT behavior, clones a repo to gitRepos directory**/
fun cloneGitRepo(repoURI: String, repoPathFile: File): Git {
    return Git.cloneRepository()
        .setURI(repoURI)
        .setDirectory(repoPathFile)
        .call()
}

/**Temporary function for testing JGIT behavior, get a repo from gitRepos directory**/
fun getGitRepo(repoPathFile: File): Git{
    return Git.open(repoPathFile)
}

/**Temporary function for testing JGIT behavior, Lazy get repo, doesn't verify it exists on creation**/
/*
fun getGitRepoLazy(repoPath: String): Git{
    val existingRepo : Repository = FileRepositoryBuilder()
        .setGitDir(File(repoPath))
        .build()
    return Git(existingRepo)
}*/

/**Temporary function for testing JGIT behavior, tries to get a repo from gitRepos directory, if it doesn't exist then it clones**/
fun getOrCloneGitRepo(repoURI: String, repoPathFile: File): Git {
    return try{
        getGitRepo(repoPathFile)
    } catch(_: Exception){
        cloneGitRepo(repoURI, repoPathFile)
    }
}

/**Temporary function for testing JGIT behavior, returns gitRepo Status.**/
/*
fun getGitRepoStatus(repo: Git): Status {
    return repo.status().call()
}*/

/**Temporary function for testing JGIT behavior, returns a list of n commits**/
fun getGitCommitLogs(repo: Git, maxCommits: Int): List<RevCommit> {
    return repo.log().setMaxCount(maxCommits).call().toList()
}

/**Temporary function for testing JGIT behavior, returns a list of diffs**/
fun getGitDiff(repo: Git, oldCommit: RevCommit, newCommit: RevCommit): List<DiffEntry>{
    return repo.diff()
        .setOldTree(getTreeParser(repo, oldCommit))
        .setNewTree(getTreeParser(repo, newCommit))
        .call()
}

/**Temporary function for testing JGIT behavior, creates a readable complete diff (akin to git terminal command)**/
fun formatDiff(repo: Git, diffEntries: List<DiffEntry>): List<String>{
    return diffEntries.map{ formatDiffEntry(repo, it) }
}

/**Temporary function for testing JGIT behavior, creates a readable diff entry (akin to git terminal command)**/
fun formatDiffEntry(repo: Git, diffEntry: DiffEntry): String {
    val out = ByteArrayOutputStream()
    val df = DiffFormatter(out)
    df.setRepository(repo.repository)
    df.format(diffEntry)
    return out.toString()
}

/**Temporary function for testing JGIT behavior, creates TreeIterator from Commit**/
fun getTreeParser(repo: Git, commit: RevCommit): AbstractTreeIterator {
    val reader = repo.repository.newObjectReader()
    val parser = CanonicalTreeParser()
    parser.reset(reader, commit.tree.id)
    return parser
}

/**Temporary function for testing JGIT behavior, creates directory path from git repo URI**/
fun getRepoPath(repoURI: String): String{
    val splitPath = repoURI.split("/").drop(2)
    val userDirectory = splitPath[1]
    val serviceDirectory = splitPath[0].removeSuffix(".com")
    val repoDirectory = splitPath[2]

    return "$reposStorageLocation/$userDirectory/$serviceDirectory/$repoDirectory"
}

/**Temporary function for testing JGIT behavior, creates file from directory path**/
fun getRepoFile(repoPath: String): File{
    return File(repoPath)
}

const val reposStorageLocation = "gitRepos"
const val testRepoURISmall = "https://github.com/octocat/Hello-World"
const val testRepoURILarge = "https://github.com/githubtraining/hellogitworld"
const val testRepoURI = testRepoURILarge

fun main(){
    val repoFile = getRepoFile(getRepoPath(testRepoURI))
    //val clonedRepo = cloneGitRepo(testRepoURI, repoFile)
    //val existingRepo = getGitRepo(repoFile)

    val gitRepo = getOrCloneGitRepo(testRepoURI, repoFile)
    //val status = getGitRepoStatus(gitRepo)

    val commits = getGitCommitLogs(gitRepo, 5)

    if(commits.isNotEmpty()){
        val testCommit = commits.first()
        if(testCommit.parentCount != 0){
            val firstParent = testCommit.getParent(0)
            val gitDiff = getGitDiff(gitRepo, firstParent, testCommit)
            val formatedDiff = formatDiff(gitRepo, gitDiff)
            println(formatedDiff.forEach{ println(it) })
        }
    }
}