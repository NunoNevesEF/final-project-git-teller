package pt.isel.service.gitProviders

import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import pt.isel.domain.account.OAuthProvider
import pt.isel.infraestructure.providerGit.GitProviderRequestFactory
import pt.isel.utils.Either
import pt.isel.model.git.GitHubEmailDTO
import pt.isel.model.git.GitHubRepositoryDTO
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.UserRepositoriesDTO

//TODO: DOCUMENT

// SUGESTãO Para o Futuro -> reduzir a quantidade de restTemplate, headers e entity repetidos, criando funções auxiliares para isso
@Service
class GithubCommunicationService(
    private val restTemplate: RestTemplate,
    private val requestFactory: GitProviderRequestFactory,
): IGitProviderService {
    override val provider = OAuthProvider.GITHUB

    fun getPrimaryEmailOrNull(accessToken: String): String? {
        return try {
            val entity = requestFactory.createEntity(accessToken)

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

    override fun getAuthenticatedUserRepositories(
        accessToken: String, page: Int, perPage: Int
    ): Either<GitProviderServiceError, UserRepositoriesDTO> =
        requestFactory.callGit(accessToken) { token ->
            val entity = requestFactory.createEntity(token)
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