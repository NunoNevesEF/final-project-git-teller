package pt.isel.service.gitProviders

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.infraestructure.config.git.GitRequestFactory
import pt.isel.model.git.GitLabProjectDTO
import pt.isel.model.git.GitLabProjectLanguagesDTO
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.UserRepositoriesDTO
import pt.isel.model.git.mainLanguage
import pt.isel.utils.Either

@Service
class GitlabCommunicationService(
    private val requestFactory: GitRequestFactory,
    private val restTemplate: RestTemplate,
): IGitProviderService {
    override val provider = OAuthAccountProvider.GITLAB

    override fun getAuthenticatedUserRepositories(
        accessToken: String,
        page: Int,
        perPage: Int
    ): Either<GitProviderServiceError, UserRepositoriesDTO> =
        requestFactory.callGit(accessToken) { token ->
            val entity = requestFactory.createEntity(token)

            val url = "https://gitlab.com/api/v4/projects" +
                        "?owned=true" +
                        "&page=$page" +
                        "&per_page=$perPage" +
                        "&order_by=last_activity_at" +
                        "&sort=desc"

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Array<GitLabProjectDTO>::class.java
            )

            val projects = response.body?.map { it.toSummary(
                getProjectMainLanguage(it.id, entity)
            ) } ?: emptyList()

            val lastPage = response.getLastPage()

            UserRepositoriesDTO(lastPage, projects)
        }

    private fun getProjectMainLanguage(projectId: Int, entity: HttpEntity<Unit>): String{
        val url = "https://gitlab.com/api/v4/projects/$projectId/languages"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<GitLabProjectLanguagesDTO>() {}
        )

        return response.body?.mainLanguage() ?: "None"
    }

    private fun ResponseEntity<Array<GitLabProjectDTO>>.getLastPage(): Int? {
        val totalPages = headers.getFirst("X-Total-Pages")
        return totalPages?.toIntOrNull()
    }
}