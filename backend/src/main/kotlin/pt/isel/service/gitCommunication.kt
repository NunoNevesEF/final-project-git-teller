package pt.isel.service

import org.eclipse.jgit.api.Git
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
const val testRepoURI = "https://github.com/octocat/Hello-World"

fun main(){
    val repoFile = getRepoFile(getRepoPath(testRepoURI))
    //val clonedRepo = cloneGitRepo(testRepoURI, repoFile)
    //val existingRepo = getGitRepo(repoFile)

    getOrCloneGitRepo(testRepoURI, repoFile)
}