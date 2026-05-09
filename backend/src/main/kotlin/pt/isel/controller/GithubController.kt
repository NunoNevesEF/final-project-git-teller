
package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.git.*
import pt.isel.utils.Either

@RestController
@RequestMapping("/api/github")
class GithubController(
    private val githubService: GithubCommunicationService
) {

    // para nao duplicar o principal e userId, assim todos passam a funcao especifica call
    // converte o Either retornado para ResponseEntity usando `failureStatus`.
    private fun <L, R> withAuthenticated(
        authentication: Authentication,
        failureStatus: Int = 401,
        call: (Int) -> Either<L, R>
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()
        return when (val result = call(userId)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(failureStatus)
                .body(mapOf("error" to result.left.toString()))
        }
    }

    @GetMapping("/repos")
    fun getUserRepositories(authentication: Authentication): ResponseEntity<Any> =
        withAuthenticated(authentication) { githubService.getAuthenticatedUserRepositories(it) }

    @GetMapping("/repos/{owner}/{repo}")
    fun getRepository(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> =
        withAuthenticated(authentication, 404) { githubService.getRepository(it, owner, repo) }

    @GetMapping("/repos/{owner}/{repo}/branches")
    fun getRepositoryBranches(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> =
        withAuthenticated(authentication) { githubService.getRepositoryBranches(it, owner, repo) }

    @GetMapping("/repos/{owner}/{repo}/commits")
    fun getRepositoryCommits(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> =
        withAuthenticated(authentication) { githubService.getRepositoryCommits(it, owner, repo) }

    @GetMapping("/repos/{owner}/{repo}/languages")
    fun getRepositoryLanguages(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> =
        withAuthenticated(authentication) { githubService.getRepositoryLanguages(it, owner, repo) }

    @GetMapping("/repos/{owner}/{repo}/commits/{sha}")
    fun getCommitDetails(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String,
        @PathVariable sha: String
    ): ResponseEntity<Any> =
        withAuthenticated(authentication, 404) { githubService.getCommitDetails(it, owner, repo, sha) }
}

