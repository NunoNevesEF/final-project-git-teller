package pt.isel.service.git

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper

data class GitHubInstallationCandidate(
    val installationId: Long,
    val accountLogin: String?,
    val repositorySelection: String?,
    val appSlug: String?
)

@Service
class GitHubUserInstallationsService(
    @Value("\${app.github.app-slug}") private val appSlug: String
) {
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    fun discoverUserInstallations(userOAuthToken: String): List<GitHubInstallationCandidate> {
        val headers = HttpHeaders().apply {
            set("Authorization", "token $userOAuthToken")
            set("Accept", "application/vnd.github+json")
        }

        val response = restTemplate.exchange(
            "https://api.github.com/user/installations",
            HttpMethod.GET,
            HttpEntity<Unit>(headers),
            String::class.java
        )

        val body = response.body ?: return emptyList()
        val root = objectMapper.readTree(body)
        val installations = root["installations"] ?: return emptyList()
        if (!installations.isArray) return emptyList()

        return installations.mapNotNull { node ->
            val nodeAppSlug = node["app_slug"]?.asText(null)
            if (nodeAppSlug != null && nodeAppSlug != appSlug) return@mapNotNull null

            val installationId = node["id"]?.asLong() ?: return@mapNotNull null
            val accountLogin = node["account"]?.get("login")?.asText()
            val repositorySelection = node["repository_selection"]?.asText()

            GitHubInstallationCandidate(
                installationId = installationId,
                accountLogin = accountLogin,
                repositorySelection = repositorySelection,
                appSlug = nodeAppSlug ?: appSlug
            )
        }
    }
}