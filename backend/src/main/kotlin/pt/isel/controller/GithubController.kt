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

    @GetMapping("/repos")
    fun getUserRepositories(
        authentication: Authentication  // Vem do JWT
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()
        return when (val result = githubService.getAuthenticatedUserRepositories(userId)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(401).body(mapOf("error" to result.left.toString()))
        }
    }

    @GetMapping("/repos/{owner}/{repo}")
    fun getRepository(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()

        return when (val result = githubService.getRepository(userId, owner, repo)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(404).body(mapOf("error" to result.left.toString()))
        }
    }


    @GetMapping("/repos/{owner}/{repo}/branches")
    fun getRepositoryBranches(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()

        return when (val result = githubService.getRepositoryBranches(userId, owner, repo)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(401).body(mapOf("error" to result.left.toString()))
        }
    }

    @GetMapping("/repos/{owner}/{repo}/commits")
    fun getRepositoryCommits(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()

        return when (val result = githubService.getRepositoryCommits(userId, owner, repo)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(401).body(mapOf("error" to result.left.toString()))
        }
    }

    @GetMapping("/repos/{owner}/{repo}/languages")
    fun getRepositoryLanguages(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()

        return when (val result = githubService.getRepositoryLanguages(userId, owner, repo)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(401).body(mapOf("error" to result.left.toString()))
        }
    }

    @GetMapping("/repos/{owner}/{repo}/commits/{sha}")
    fun getCommitDetails(
        authentication: Authentication,
        @PathVariable owner: String,
        @PathVariable repo: String,
        @PathVariable sha: String
    ): ResponseEntity<Any> {
        val principal = authentication.principal as? UserPrincipal
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Invalid user"))

        val userId = principal.getUserId()

        return when (val result = githubService.getCommitDetails(userId, owner, repo, sha)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(404).body(mapOf("error" to result.left.toString()))
        }
    }


}

