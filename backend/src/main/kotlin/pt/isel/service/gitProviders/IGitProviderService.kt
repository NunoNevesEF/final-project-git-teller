package pt.isel.service.gitProviders

import pt.isel.domain.account.OAuthProvider
import pt.isel.model.git.GitProviderServiceError
import pt.isel.model.git.UserRepositoriesDTO
import pt.isel.utils.Either

//TODO: DOCUMENT

interface IGitProviderService {
    val provider: OAuthProvider

    fun getAuthenticatedUserRepositories(
        accessToken: String, page: Int, perPage: Int = 5
    ): Either<GitProviderServiceError, UserRepositoriesDTO>
}