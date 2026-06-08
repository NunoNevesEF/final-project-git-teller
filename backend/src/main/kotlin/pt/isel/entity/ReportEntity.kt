package pt.isel.entity

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
import pt.isel.model.report.GitAnalysis
import java.time.Instant

@Entity
@Table(name = "report")
class ReportEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user : User,

    @Column(name = "repo_uri", nullable = false)
    val repoURI: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    //TODO: SPECIFY REQUEST PARAMETERS WHEN PARAMETERIZING ANALYSIS REQUEST

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis", columnDefinition = "jsonb", nullable = false)
    val gitAnalysis: GitAnalysis,

    @Column(nullable = true)
    var pdf: ByteArray? = null
) : IsEntity