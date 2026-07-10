package pt.isel.entity.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import pt.isel.entity.IsEntity
import pt.isel.entity.account.User
import pt.isel.domain.report.GitAnalysis
import java.time.Instant

/**
 *  `Report`
 *
 * @property id the unique identifier for this [Report] in the persistence.
 * @property user the [User] that owns this [Report] is associated to in the persistence.
 * @property gitAnalysis the analysis request parameters and result of data gathering of a git repository that was used to generate this [Report].
 * @property pdf the web presentation of a [Report] formatted into a pdf and stored by its bytes. Should only be kept temporarily due to large size.
 */
@Entity
@Table(name = "report")
data class Report (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user : User,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis", columnDefinition = "jsonb", nullable = false)
    val gitAnalysis: GitAnalysis,

    @Column(nullable = true)
    var pdf: ByteArray? = null
) : IsEntity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Report

        if (id != other.id) return false
        if (user != other.user) return false
        if (createdAt != other.createdAt) return false
        if (gitAnalysis != other.gitAnalysis) return false
        if (!pdf.contentEquals(other.pdf)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + user.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + gitAnalysis.hashCode()
        result = 31 * result + (pdf?.contentHashCode() ?: 0)
        return result
    }
}