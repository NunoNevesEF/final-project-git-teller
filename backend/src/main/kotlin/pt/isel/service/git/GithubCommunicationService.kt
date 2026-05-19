package pt.isel.service.git

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.model.GitHubEmailDTO
import pt.isel.model.GitHubRepositoryDTO
import pt.isel.model.RepositorySummary
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.repository.jpa.GithubInstallationRepoJpa
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success
import tools.jackson.databind.ObjectMapper

sealed class GithubCommunicationServiceError
object PrimaryEmailNotFoundError : GithubCommunicationServiceError()
object RepositoryNotFoundError : GithubCommunicationServiceError()
object InvalidTokenError : GithubCommunicationServiceError()
object RateLimitError : GithubCommunicationServiceError()
object NetworkError : GithubCommunicationServiceError()

@Service
class GithubCommunicationService(
    private val linkedAccountRepo: ILinkedAccountRepository,
    private val githubInstallationRepo: GithubInstallationRepoJpa,
    private val gitHubAppService: GitHubAppService
) {
    private val objectMapper = ObjectMapper()

    private fun createHeaders(accessToken: String): HttpHeaders = HttpHeaders().apply {
        setBearerAuth(accessToken)
        set("Accept", "application/vnd.github+json")
    }

    private fun getOAuthTokenOrNull(userId: Int): String? =
        linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
            ?.filterIsInstance<OAuthLinkedAccount>()
            ?.firstOrNull { it.accessToken != null }
            ?.accessToken
            ?.tokenValue

    private fun parseRepositoryList(body: String?, repositoriesField: String? = null): List<RepositorySummary> {
        if (body.isNullOrBlank()) return emptyList()

        val root = objectMapper.readTree(body)

        val nodes = if (repositoriesField == null) {
            if (!root.isArray) return emptyList()
            root
        } else {
            val nested = root[repositoriesField] ?: return emptyList()
            if (!nested.isArray) return emptyList()
            nested
        }

        return nodes.mapNotNull { node ->
            runCatching {
                objectMapper.treeToValue(node, GitHubRepositoryDTO::class.java).toSummary()
            }.getOrNull()
        }
    }

    fun getPrimaryEmail(accessToken: String) =
        getPrimaryEmailOrNull(accessToken)?.let { success(it) } ?: failure(PrimaryEmailNotFoundError)

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
        page: Int = 1,
        perPage: Int = 30
    ): Either<GithubCommunicationServiceError, List<RepositorySummary>> {
        val restTemplate = RestTemplate()

        return try {
            val installationRecords = githubInstallationRepo.findByUserId(userId)

            if (installationRecords.isNotEmpty()) {
                val repos = mutableListOf<RepositorySummary>()
                var anySuccessfulRequest = false

                installationRecords.forEach { installation ->
                    val result = runCatching {
                        val token = gitHubAppService.getInstallationToken(installation.installationId)
                        val headers = createHeaders(token)
                        val entity = HttpEntity<Unit>(headers)

                        val response = restTemplate.exchange(
                            "https://api.github.com/installation/repositories?page=$page&per_page=$perPage",
                            HttpMethod.GET,
                            entity,
                            String::class.java
                        )

                        parseRepositoryList(response.body, "repositories")
                    }

                    if (result.isSuccess) {
                        anySuccessfulRequest = true
                        repos += result.getOrThrow()
                    }
                }

                if (anySuccessfulRequest) {
                    success(repos.distinctBy { it.fullName })
                } else {
                    failure(InvalidTokenError)
                }
            } else {
                val token = getOAuthTokenOrNull(userId) ?: return failure(InvalidTokenError)

                val headers = createHeaders(token)
                val entity = HttpEntity<Unit>(headers)

                val response = restTemplate.exchange(
                    "https://api.github.com/user/repos?page=$page&per_page=$perPage&sort=updated&direction=desc",
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )

                success(parseRepositoryList(response.body))
            }
        } catch (e: RestClientException) {
            when {
                e.message?.contains("401") == true || e.message?.contains("403") == true -> failure(InvalidTokenError)
                e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
                e.message?.contains("rate limit") == true || e.message?.contains("rate_limit") == true -> failure(RateLimitError)
                else -> failure(NetworkError)
            }
        } catch (_: Exception) {
            failure(NetworkError)
        }
    }
}