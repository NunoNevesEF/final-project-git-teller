package pt.isel.service.gitProviders

import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.UserRepositoriesDTO
import pt.isel.utils.Either

interface IGitProviderService {
    val provider: OAuthAccountProvider

    fun getAuthenticatedUserRepositories(
        accessToken: String, page: Int, perPage: Int = 5
    ): Either<GitProviderServiceError, UserRepositoriesDTO>
}