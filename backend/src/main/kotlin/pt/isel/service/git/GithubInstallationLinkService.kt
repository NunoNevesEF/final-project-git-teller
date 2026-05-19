package pt.isel.service.git

import org.springframework.stereotype.Service
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.entity.GithubInstallationEntity
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.repository.jpa.GithubInstallationRepoJpa
import java.time.Instant

@Service
class GitHubInstallationLinkService(
    private val linkedAccountRepo: ILinkedAccountRepository,
    private val githubInstallationRepo: GithubInstallationRepoJpa,
    private val userInstallationsService: GitHubUserInstallationsService
) {

    fun discoverForUser(userId: Int): List<GitHubInstallationCandidate> {
        val token = getGitHubOAuthTokenOrNull(userId) ?: return emptyList()
        return userInstallationsService.discoverUserInstallations(token)
    }

    fun discoverAndAutoLink(userId: Int, userOAuthToken: String): List<GitHubInstallationCandidate> {
        val installations = userInstallationsService.discoverUserInstallations(userOAuthToken)
        if (installations.size == 1) {
            val candidate = installations.first()
            linkUserInstallation(userId, candidate.installationId, candidate.accountLogin)
        }
        return installations
    }

    fun linkUserInstallation(
        userId: Int,
        installationId: Long,
        accountLogin: String?
    ): GithubInstallationEntity {
        val existing = githubInstallationRepo.findByInstallationId(installationId)
        val entity = if (existing == null) {
            GithubInstallationEntity(
                userId = userId,
                installationId = installationId,
                accountLogin = accountLogin,
                installedAt = Instant.now()
            )
        } else {
            existing.copy(
                userId = userId,
                accountLogin = accountLogin ?: existing.accountLogin
            )
        }

        return githubInstallationRepo.save(entity)
    }

    fun linkByAccountLogin(installationId: Long, accountLogin: String?): Boolean {
        if (accountLogin == null) return false

        val linkedAccount = linkedAccountRepo.readByTypeAndKey(AccountType.GITHUB.type, accountLogin)
            ?: return false

        linkUserInstallation(linkedAccount.userId, installationId, accountLogin)
        return true
    }

    fun unlinkInstallation(installationId: Long) {
        githubInstallationRepo.deleteByInstallationId(installationId)
    }

    fun installationsForUser(userId: Int): List<GithubInstallationEntity> =
        githubInstallationRepo.findByUserId(userId)

    private fun getGitHubOAuthTokenOrNull(userId: Int): String? =
        linkedAccountRepo.readByUserAndType(userId, AccountType.GITHUB.type)
            ?.filterIsInstance<OAuthLinkedAccount>()
            ?.firstOrNull()
            ?.accessToken
            ?.tokenValue
}