package pt.isel.infraestructure.providerGit

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.InvalidProviderTokenError
import pt.isel.model.git.NetworkError
import pt.isel.model.git.RateLimitError
import pt.isel.model.git.RepositoryNotFoundError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.success

//TODO: DOCUMENT

@Component
class GitProviderRequestFactory{
    fun <T> callGit(accessToken: String, block: (String) -> T): Either<GitProviderServiceError, T> {
        return try {
            val result = block(accessToken)
            success(result)
        } catch (e: RestClientException) {
            when {
                e.message?.contains("401") == true || e.message?.contains("403") == true -> failure(InvalidProviderTokenError)
                e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
                e.message?.contains("rate limit") == true || e.message?.contains("rate_limit") == true -> failure(RateLimitError)
                else -> failure(NetworkError)
            }
        }
    }

    fun createEntity(accessToken: String) = HttpEntity<Unit>(createHeaders(accessToken))

    private fun createHeaders(accessToken: String): HttpHeaders = HttpHeaders().apply {
        setBearerAuth(accessToken)
        accept = listOf(MediaType.APPLICATION_JSON)
    }
}