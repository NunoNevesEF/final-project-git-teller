package pt.isel.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import pt.isel.domain.account.Role

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    val userName: String?,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER
) {
    init {
        require(email.isNotBlank()) { "email cannot be blank" }
//        require(userName.isNullOrBlank().not()) { "userName cannot be blank" }
    }
}