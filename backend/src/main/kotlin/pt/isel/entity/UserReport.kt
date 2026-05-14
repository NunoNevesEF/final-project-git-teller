package pt.isel.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user_reports")
class UserReport(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false, columnDefinition = "bytea")
    val data: ByteArray
)