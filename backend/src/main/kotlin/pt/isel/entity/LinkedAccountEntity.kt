package pt.isel.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "linked_accounts")
data class LinkedAccountEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val userId: Int,

    @Column(nullable = false)
    val type: String,

    val providerId: String? = null,

    @Column(columnDefinition = "TEXT")
    val passwordHash: String? = null,

    @Column(columnDefinition = "TEXT")
    val accessToken: String? = null,

    @Column(columnDefinition = "TEXT")
    val refreshToken: String? = null
)