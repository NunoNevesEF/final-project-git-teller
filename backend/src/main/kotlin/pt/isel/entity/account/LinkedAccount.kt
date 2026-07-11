package pt.isel.entity.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.IsEntity

/**
 *  `LinkedAccountEntityException`
 *
 * Represents exceptions that can occur while constructing a [LinkedAccount] domain object.
 *
 * These exceptions are thrown by the entity layer and can be translated into
 * service error objects by the service layer.
 *
 * THIS DOES NOT HANDLE PERSISTENCE EXCEPTIONS ONLY OBJECT DATA VALIDATION
 * */
sealed class LinkedAccountEntityException : RuntimeException()

/** Provided providerId is blank. */
class BlankProviderIdException : LinkedAccountEntityException()


/**
 *  `LinkedAccount`
 *
 * Represents a account used for registration or later linked by a user in the app
 *
 * @property id the unique identifier for this [LinkedAccount] in the persistence.
 * @property user the [User] that this [LinkedAccount] is associated to in the persistence.
 */
@Entity
@Table(name = "linked_accounts")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class LinkedAccount(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Int = 0
) : IsEntity {
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User
}

/**
 *  `FormLinkedAccount`
 *
 * Represents a account used for a form-based registration (email + password)
 *
 * @property passwordHash the server-stored hash of the password the [User] used during registration.
 * */
@Entity
@Table(name = "form_accounts")
class FormLinkedAccount(
    id: Int = 0,
    user: User,
    @Column(columnDefinition = "TEXT", nullable = false) var passwordHash: String,
) : LinkedAccount(id) {
    init{
        this.user = user
    }

    fun copy(
        id: Int = this.id,
        user: User = this.user,
        passwordHash: String = this.passwordHash,
    ): FormLinkedAccount = FormLinkedAccount(id, user, passwordHash)
}

/**
 *  `OAuthLinkedAccount`
 *
 * Represents a account used for a oauth provider based registration or that was later linked.
 *
 * USE SECONDARY CONSTRUCTOR AS IT ASSIGNS [User]
 *
 * @property accessToken the value of the access token from the provider.
 * @property refreshToken the value of the refresh token from the provider.
 * @property provider a supported provider and it's app-logic restrictions as defined in [OAuthProvider]
 * @property providerId the provider unique identifier for the user provider account.
 * */
@Entity
@Table(name = "oauth_accounts")
class OAuthLinkedAccount(
    id: Int = 0,
    user: User,

    @Column(columnDefinition = "TEXT") val accessToken: String = "",

    @Column(columnDefinition = "TEXT") val refreshToken: String = "",

    @Column(nullable = false) @Enumerated(EnumType.STRING) val provider: OAuthProvider,

    @Column(nullable = false) val providerId: String,
) : LinkedAccount(id) {
    init {
        if (providerId.isBlank()) throw BlankProviderIdException()
        this.user = user
    }

    /**Secondary constructor
     *
     * @param id the unique identifier for this [LinkedAccount] in the persistence.
     * @param user the [User] that this [LinkedAccount] is associated to in the persistence.
     * @param accessToken the value of the access token from the provider.
     * @param refreshToken the value of the refresh token from the provider.
     * @param providerName the name of the provider to map to [OAuthProvider]
     * @property providerId the provider unique identifier for the user provider account.
     * */
    constructor(
        id: Int = 0,
        user: User,
        accessToken: String = "",
        refreshToken: String = "",
        providerName: String,
        providerId: String
    ) : this(id, user, accessToken, refreshToken, OAuthProvider.fromString(providerName), providerId)

    fun copy(
        id: Int = this.id,
        user: User = this.user,
        accessToken: String = this.accessToken,
        refreshToken: String = this.refreshToken,
        provider: OAuthProvider = this.provider,
        providerId: String = this.providerId
    ): OAuthLinkedAccount = OAuthLinkedAccount(id, user, accessToken, refreshToken, provider, providerId)
}