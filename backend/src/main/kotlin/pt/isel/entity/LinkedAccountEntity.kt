package pt.isel.entity

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
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthAccountProvider
import pt.isel.domain.account.OAuthLinkedAccount

@Entity
@Table(name = "linked_accounts")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class LinkedAccountEntity<ENTITY: LinkedAccountEntity<ENTITY, DOMAIN>, DOMAIN: LinkedAccount<DOMAIN, ENTITY>>(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0
): IsEntity{
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    abstract fun toDomain(): DOMAIN
}

@Entity
@Table(name = "form_accounts")
class FormLinkedAccountEntity(
    id: Int = 0,

    user: User,

    @Column(columnDefinition = "TEXT", nullable = false)
    var passwordHash: String = "",
): LinkedAccountEntity<FormLinkedAccountEntity, FormLinkedAccount>(id){
    init{ this.user = user }

    override fun toDomain(): FormLinkedAccount = FormLinkedAccount(id, user.id, passwordHash)
}

@Entity
@Table(name = "oauth_accounts")
class OAuthLinkedAccountEntity(
    id: Int = 0,

    user: User,

    @Column(columnDefinition = "TEXT")
    var accessToken: String = "",

    @Column(columnDefinition = "TEXT")
    var refreshToken: String = "",

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var provider: OAuthAccountProvider = OAuthAccountProvider.UNDEFINED,

    @Column(nullable = false)
    var providerId: String = "",
): LinkedAccountEntity<OAuthLinkedAccountEntity, OAuthLinkedAccount>(id){
    init{ this.user = user }

    override fun toDomain(): OAuthLinkedAccount =
        OAuthLinkedAccount(id, user.id, accessToken, refreshToken, provider, providerId)
}