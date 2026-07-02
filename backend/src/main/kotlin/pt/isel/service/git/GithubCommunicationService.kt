package pt.isel.service.git

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import pt.isel.model.*
import pt.isel.service.account.LinkedAccountService
import pt.isel.utils.Success

sealed class GithubCommunicationServiceError{
    abstract fun toStatus(): Int
}

object RepositoryNotFoundError : GithubCommunicationServiceError() {
    override fun toStatus() = 404
}

object LinkedGitAccountNotFoundError: GithubCommunicationServiceError(){
    override fun toStatus() = 404
}

object InvalidTokenError : GithubCommunicationServiceError() {
    override fun toStatus() = 403
}

object RateLimitError : GithubCommunicationServiceError() {
    override fun toStatus() = 429
}

object NetworkError : GithubCommunicationServiceError() {
    override fun toStatus() = 503
}


// SUGESTãO Para o Futuro -> reduzir a quantidade de restTemplate, headers e entity repetidos, criando funções auxiliares para isso
@Service
class GithubCommunicationService(
    private val linkedAccountService: LinkedAccountService,
) {
    private fun createHeaders(accessToken: String): HttpHeaders = HttpHeaders().apply {
        setBearerAuth(accessToken)
        accept = listOf(MediaType.APPLICATION_JSON)
    }

    private fun getGithubAccountAuthInfo(userId: Int, gitLinkedAccountId: Int): String? {
        val accountResult = linkedAccountService.findUserOAuthAccount(gitLinkedAccountId, userId)
        return if(accountResult !is Success) null
        else accountResult.right.accessToken
    }

    private fun <T> callGitHub(userId: Int, gitLinkedAccountId: Int, block: (String) -> T): Either<GithubCommunicationServiceError, T> {
        return try {
            val token = getGithubAccountAuthInfo(userId, gitLinkedAccountId) ?: return failure(LinkedGitAccountNotFoundError)
            val result = block(token)
            success(result)
        } catch (e: RestClientException) {
            when {
                e.message?.contains("401") == true || e.message?.contains("403") == true -> failure(InvalidTokenError)
                e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
                e.message?.contains("rate limit") == true || e.message?.contains("rate_limit") == true -> failure(RateLimitError)
                else -> failure(NetworkError)
            }
        }
    }

    fun getPrimaryEmailOrNull(accessToken: String): String? {
        val restTemplate = RestTemplate()
        return try {
            val headers = createHeaders(accessToken)
            val entity = HttpEntity<Unit>(headers)

            val response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                Array<GitHubEmailDTO>::class.java
            )
            val emails = response.body ?: return null
            emails.firstOrNull { it.primary }?.email
        } catch (_: RestClientException) {
            null
        }
    }

    fun getAuthenticatedUserRepositories(
        userId: Int,
        gitLinkedAccountId: Int,
        page: Int = 1,
        perPage: Int = 5
    ): Either<GithubCommunicationServiceError, UserRepositoriesDTO> =
        callGitHub(userId, gitLinkedAccountId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/user/repos?page=$page&per_page=$perPage&sort=updated&direction=desc"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<GitHubRepositoryDTO>::class.java)

            val repositories = response.body?.map { it.toSummary() } ?: emptyList()
            val lastPage = response.getLastPage()

            UserRepositoriesDTO(lastPage, repositories)
        }

    private fun ResponseEntity<Array<GitHubRepositoryDTO>>.getLastPage(): Int?{
        val linkHeader = headers.get("Link")?.firstOrNull() ?: return null    //Link does not exist if only 1 page
        val lastPageUrl = linkHeader.split(",").firstOrNull{it.contains("last")} ?: return null  //Last does not exist if curr page is last
        val baseUrlSection = lastPageUrl.split("&")[0] //baseUrl & per_page & sort & direction
        return baseUrlSection.substringAfter("=").first().digitToInt() //https:...?page=${page_number}
    }

    /*fun getRepository(
        userId: Int,
        owner: String,
        repo: String
    ): Either<GithubCommunicationServiceError, RepositorySummary> =
        callGitHub(userId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/repos/$owner/$repo"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, GitHubRepositoryDTO::class.java)
            response.body?.toSummary() ?: throw RestClientException("404")
        }

    fun getRepositoryBranches(
        userId: Int,
        owner: String,
        repo: String,
        page: Int = 1
    ): Either<GithubCommunicationServiceError, List<BranchSummary>> =
        callGitHub(userId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/repos/$owner/$repo/branches?page=$page&per_page=100"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<GitHubBranchDTO>::class.java)
            response.body?.map { it.toSummary() } ?: emptyList()
        }

    fun getRepositoryCommits(
        userId: Int,
        owner: String,
        repo: String,
        page: Int = 1
    ): Either<GithubCommunicationServiceError, List<CommitSummary>> =
        callGitHub(userId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/repos/$owner/$repo/commits?page=$page&per_page=30"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<GitHubCommitDTO>::class.java)
            response.body?.map { it.toSummary() } ?: emptyList()
        }

    fun getRepositoryLanguages(
        userId: Int,
        owner: String,
        repo: String
    ): Either<GithubCommunicationServiceError, LanguagesSummary> =
        callGitHub(userId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/repos/$owner/$repo/languages"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, Map::class.java)
            val languages = (response.body as? Map<String, Int>) ?: emptyMap()
            LanguagesSummary(languages)
        }

    fun getCommitDetails(
        userId: Int,
        owner: String,
        repo: String,
        sha: String
    ): Either<GithubCommunicationServiceError, CommitDetailsSummary> =
        callGitHub(userId) { token ->
            val restTemplate = RestTemplate()
            val headers = createHeaders(token)
            val entity = HttpEntity<Unit>(headers)
            val url = "https://api.github.com/repos/$owner/$repo/commits/$sha"
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, GitHubCommitDetailsDTO::class.java)
            val commit = response.body ?: throw RestClientException("404")
            commit.toSummary()
        }*/
}