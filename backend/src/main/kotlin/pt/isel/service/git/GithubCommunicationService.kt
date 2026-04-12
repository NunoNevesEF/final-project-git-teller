package pt.isel.service.git

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pt.isel.service.Either
import pt.isel.service.toEither

sealed class GithubCommunicationServiceError
object PrimaryEmailNotFoundError : GithubCommunicationServiceError()

@Service
class GithubCommunicationService(){
    fun getPrimaryEmail(accessToken: String): Either<PrimaryEmailNotFoundError, String> =
        getPrimaryEmailOrNull(accessToken).toEither { PrimaryEmailNotFoundError }

    fun getPrimaryEmailOrNull(accessToken: String): String?{
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val entity = HttpEntity<Unit>(headers)

        val response = restTemplate.exchange(
            "https://api.github.com/user/emails",
            HttpMethod.GET,
            entity,
            Array<GitHubEmailDTO>::class.java
        )

        val emails = response.body ?: return null

        val primary = emails.firstOrNull{ it.primary }

        return primary?.email
    }

    private data class GitHubEmailDTO(
        val email: String,
        val primary: Boolean,
        val verified: Boolean,
        val visibility: String?
    )
}