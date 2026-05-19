package pt.isel.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "github_installations")
data class GithubInstallationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "installation_id", nullable = false, unique = true)
    val installationId: Long,

    @Column(name = "account_login")
    val accountLogin: String? = null,

    @Column(name = "installed_at", nullable = false)
    val installedAt: Instant = Instant.now()
)