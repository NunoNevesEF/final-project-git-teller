package pt.isel.service.git
/*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pt.isel.utils.Either
import pt.isel.utils.toEither

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


}*/

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.utils.Either
import pt.isel.utils.toEither
import pt.isel.utils.success
import pt.isel.utils.failure
import java.time.Instant

// Error types - Sealed class hierarchy
sealed class GithubCommunicationServiceError
object PrimaryEmailNotFoundError : GithubCommunicationServiceError()
object RepositoryNotFoundError : GithubCommunicationServiceError()
object InvalidTokenError : GithubCommunicationServiceError()
object RateLimitError : GithubCommunicationServiceError()
object NetworkError : GithubCommunicationServiceError()

// DTOs for service responses (what we expose)
data class RepositorySummary(
    val id: Long,
    val name: String,
    val fullName: String,
    val htmlUrl: String,
    val description: String?,
    val private: Boolean,
    val language: String?,
    val starsCount: Int,
    val forksCount: Int,
    val updatedAt: Instant
)

data class BranchSummary(
    val name: String,
    val protected: Boolean,
    val lastCommitSha: String
)

data class CommitSummary(
    val sha: String,
    val message: String,
    val author: String,
    val timestamp: Instant,
    val htmlUrl: String
)

data class LanguagesSummary(
    val languages: Map<String, Int>
)

// DTOs for GitHub API responses (internal - parse GitHub's response format)
private data class GitHubEmailDTO(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)

private data class GitHubRepositoryDTO(
    val id: Long,
    val name: String,
    val full_name: String,
    val html_url: String,
    val description: String?,
    val private: Boolean,
    val language: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val updated_at: String
) {
    fun toSummary(): RepositorySummary = RepositorySummary(
        id = id,
        name = name,
        fullName = full_name,
        htmlUrl = html_url,
        description = description,
        private = private,
        language = language,
        starsCount = stargazers_count,
        forksCount = forks_count,
        updatedAt = Instant.parse(updated_at)
    )
}

private data class GitHubBranchDTO(
    val name: String,
    val protected: Boolean,
    val commit: GitHubCommitReferenceDTO
) {
    fun toSummary(): BranchSummary = BranchSummary(
        name = name,
        protected = protected,
        lastCommitSha = commit.sha
    )
}

private data class GitHubCommitReferenceDTO(
    val sha: String
)

private data class GitHubCommitDTO(
    val sha: String,
    val commit: GitHubCommitDataDTO,
    val html_url: String,
    val author: GitHubUserDTO?
) {
    fun toSummary(): CommitSummary = CommitSummary(
        sha = sha,
        message = commit.message,
        author = author?.login ?: "Unknown",
        timestamp = Instant.parse(commit.author.date),
        htmlUrl = html_url
    )
}

private data class GitHubCommitDataDTO(
    val message: String,
    val author: GitHubAuthorDTO
)

private data class GitHubAuthorDTO(
    val name: String,
    val date: String
)

private data class GitHubUserDTO(
    val login: String
)


data class CommitDetailsSummary(
    val sha: String,
    val message: String,
    val author: String,
    val date: Instant,
    val htmlUrl: String,
    val additions: Int,
    val deletions: Int,
    val totalChanges: Int,
    val files: List<CommitFileSummary>
)

data class CommitFileSummary(
    val filename: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val changes: Int,
    val patch: String?
)

@Service
class GithubCommunicationService(
    private val linkedAccountRepo: ILinkedAccountRepository
) {
    private fun createHeaders(accessToken: String): HttpHeaders = HttpHeaders().apply {
        setBearerAuth(accessToken)
        accept = listOf(MediaType.APPLICATION_JSON)
    }



    fun getPrimaryEmail(accessToken: String): Either<GithubCommunicationServiceError, String> =
        getPrimaryEmailOrNull(accessToken).toEither { PrimaryEmailNotFoundError }

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
        } catch (e: RestClientException) {
            null
        }
    }



    fun getAuthenticatedUserRepositories(
        userId: Int,
        page: Int = 1,
        perPage: Int = 30
    ): Either<GithubCommunicationServiceError, List<RepositorySummary>> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)


        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/user/repos?page=$page&per_page=$perPage&sort=updated&direction=desc"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Array<GitHubRepositoryDTO>::class.java
        )

        val repositories = response.body?.map { it.toSummary() } ?: emptyList()
        success(repositories)
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true || e.message?.contains("403") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }


    fun getRepository(
        userId: Int,
        owner: String,
        repo: String
    ): Either<GithubCommunicationServiceError, RepositorySummary> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)

        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/repos/$owner/$repo"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            GitHubRepositoryDTO::class.java
        )

        val repository = response.body?.toSummary()
        if (repository != null) success(repository) else failure(RepositoryNotFoundError)
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }


    fun getRepositoryBranches(
        userId: Int,
        owner: String,
        repo: String,
        page: Int = 1
    ): Either<GithubCommunicationServiceError, List<BranchSummary>> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)

        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/repos/$owner/$repo/branches?page=$page&per_page=100"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Array<GitHubBranchDTO>::class.java
        )

        val branches = response.body?.map { it.toSummary() } ?: emptyList()
        success(branches)
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }


    fun getRepositoryCommits(
        userId: Int,
        owner: String,
        repo: String,
        page: Int = 1
    ): Either<GithubCommunicationServiceError, List<CommitSummary>> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)

        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/repos/$owner/$repo/commits?page=$page&per_page=30"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Array<GitHubCommitDTO>::class.java
        )

        val commits = response.body?.map { it.toSummary() } ?: emptyList()
        success(commits)
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }


    fun getRepositoryLanguages(
        userId: Int,
        owner: String,
        repo: String
    ): Either<GithubCommunicationServiceError, LanguagesSummary> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)

        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/repos/$owner/$repo/languages"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Map::class.java
        )

        val languages = (response.body as? Map<String, Int>) ?: emptyMap()
        success(LanguagesSummary(languages))
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }





    private data class GitHubCommitDetailsDTO(
        val sha: String,
        val commit: GitHubCommitDataDTO,
        val html_url: String,
        val author: GitHubUserDTO?,
        val stats: GitHubCommitStatsDTO?,
        val files: List<GitHubCommitFileDTO>?
    ) {
        fun toSummary(): CommitDetailsSummary = CommitDetailsSummary(
            sha = sha,
            message = commit.message,
            author = author?.login ?: commit.author.name,
            date = Instant.parse(commit.author.date),
            htmlUrl = html_url,
            additions = stats?.additions ?: 0,
            deletions = stats?.deletions ?: 0,
            totalChanges = stats?.total ?: 0,
            files = files?.map { it.toSummary() } ?: emptyList()
        )
    }


    fun getCommitDetails(
        userId: Int,
        owner: String,
        repo: String,
        sha: String
    ): Either<GithubCommunicationServiceError, CommitDetailsSummary> = try {
        val restTemplate = RestTemplate()

        val githubToken =
            linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
                ?.filterIsInstance<OAuthLinkedAccount>()
                ?.firstOrNull()
                ?.accessToken
                ?.tokenValue
                ?: return failure(InvalidTokenError)

        val headers = createHeaders(githubToken)
        val entity = HttpEntity<Unit>(headers)

        val url = "https://api.github.com/repos/$owner/$repo/commits/$sha"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            GitHubCommitDetailsDTO::class.java
        )

        val commit = response.body ?: return failure(RepositoryNotFoundError)
        success(commit.toSummary())
    } catch (e: RestClientException) {
        when {
            e.message?.contains("401") == true -> failure(InvalidTokenError)
            e.message?.contains("404") == true -> failure(RepositoryNotFoundError)
            else -> failure(NetworkError)
        }
    }


    private data class GitHubCommitStatsDTO(
        val additions: Int,
        val deletions: Int,
        val total: Int
    )

    private data class GitHubCommitFileDTO(
        val filename: String,
        val status: String,
        val additions: Int,
        val deletions: Int,
        val changes: Int,
        val patch: String?
    ) {
        fun toSummary(): CommitFileSummary = CommitFileSummary(
            filename = filename,
            status = status,
            additions = additions,
            deletions = deletions,
            changes = changes,
            patch = patch
        )
    }



}