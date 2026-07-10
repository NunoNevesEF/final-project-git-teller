package pt.isel.entity.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import pt.isel.entity.account.model.Role
import pt.isel.entity.IsEntity

/**
 *  `UserEntityException`
 *
 * Represents exceptions that can occur while constructing a [User] entity.
 *
 * These exceptions are thrown by the entity layer and can be translated into
 * service error objects by the service layer.
 *
 * THIS DOES NOT HANDLE PERSISTENCE EXCEPTIONS ONLY OBJECT DATA VALIDATION
 * */
sealed class UserEntityException : RuntimeException()
/** Provided email is blank. */
class BlankEmailException : UserEntityException()
/** Provided username is blank. */
class BlankUsernameException : UserEntityException()

/**
 *  `User`
 *
 * Represents the user and it's entity
 *
 * @property id the unique identifier for this [User] in the persistence.
 * @property email a unique identifier for this [User], used in service layer and authorization infrastructure to attain this entity.
 * @property username the name used to refer to the [User] by the application.
 * @property role the role that the user takes on by the application. Used for role-based authorization checks.
 * */
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    val username: String?,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER
): IsEntity {
    init {
        if(email.isBlank()) throw BlankEmailException()
        if(username?.isNotBlank() == false) throw BlankUsernameException()
    }
}