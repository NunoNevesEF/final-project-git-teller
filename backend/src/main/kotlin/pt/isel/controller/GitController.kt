
package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.model.git.LinkedAccountNotFoundError
import pt.isel.model.git.GitAccountAuthInfo
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.InvalidProviderError
import pt.isel.model.git.UserRepositoriesDTO
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.gitProviders.*
import pt.isel.utils.Either
import pt.isel.utils.Success
import pt.isel.utils.failure

@RestController
@RequestMapping("/api/git")
class GitController(
    private val linkedAccountService: LinkedAccountService,
    private val providerResolver: GitProviderResolver
) {
    private fun <R: Any> withAuthenticated(
        principal: UserPrincipal,
        gitLinkedAccountId: Int,
        call: (GitAccountAuthInfo) -> Either<GitProviderServiceError, R>
    ): ResponseEntity<R> {
        val gitAccountAuthInfo = getGitAccountAuthInfo(principal.getUserId(), gitLinkedAccountId)
            ?: return ResponseEntity.status(LinkedAccountNotFoundError.status).build()

        return when (val result = call(gitAccountAuthInfo)) {
            is Either.Right -> ResponseEntity.ok(result.right)
            is Either.Left -> ResponseEntity.status(result.left.status).build()
        }
    }

    @GetMapping("/repos/{gitLinkedAccountId}/{currPage}")
    fun getUserRepositories(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable gitLinkedAccountId: Int,
        @PathVariable currPage: Int,
    ): ResponseEntity<UserRepositoriesDTO>{
        return withAuthenticated(principal, gitLinkedAccountId) { (token, provider) ->
            val service = providerResolver.get(provider) ?:
                return@withAuthenticated failure(InvalidProviderError)

            service.getAuthenticatedUserRepositories(token, currPage)
        }
    }

    private fun getGitAccountAuthInfo(userId: Int, gitLinkedAccountId: Int): GitAccountAuthInfo? {
        val accountResult = linkedAccountService.findUserOAuthAccount(gitLinkedAccountId, userId)
        return if(accountResult !is Success) null
        else GitAccountAuthInfo(accountResult.right.accessToken, accountResult.right.provider)
    }

    /*@GetMapping("/repos/{owner}/{repo}")
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
        withAuthenticated(authentication, 404) { githubService.getCommitDetails(it, owner, repo, sha) }*/
}

