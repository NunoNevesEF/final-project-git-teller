package pt.isel.domain.account

import pt.isel.entity.FormLinkedAccountEntity
import pt.isel.entity.LinkedAccountEntity
import pt.isel.entity.OAuthLinkedAccountEntity
import pt.isel.entity.User

sealed class LinkedAccount<DOMAIN: LinkedAccount<DOMAIN, ENTITY>, ENTITY: LinkedAccountEntity<ENTITY, DOMAIN>>(
    val id: Int,
    val userId: Int,
) {
    init{
        require(id >= 0) { "id must be >= 0" }
        require(userId >= 0) { "userId must be >= 0" }
    }

    abstract fun toEntity(user: User): ENTITY
}

data class FormLinkedAccount(
    private val _id: Int, private val _userId: Int, val passwordHash: String
) : LinkedAccount<FormLinkedAccount, FormLinkedAccountEntity>(_id, _userId) {
    companion object{
        fun create(id: Int = 0, userId: Int, passwordHash: String) =
            FormLinkedAccount(id, userId, passwordHash)
    }

    override fun toEntity(user: User): FormLinkedAccountEntity = FormLinkedAccountEntity(id, user, passwordHash)
}

data class OAuthLinkedAccount(
    private val _id: Int,
    private val _userId: Int,
    val accessToken: String,
    val refreshToken: String,
    val provider: OAuthAccountProvider,
    val providerId: String
) : LinkedAccount<OAuthLinkedAccount, OAuthLinkedAccountEntity>(_id, _userId) {
    init{ require(!providerId.isBlank()) { "provider id must not be blank" } }

    companion object{
        fun create(
            id: Int = 0, userId: Int,
            accessToken: String = "", refreshToken: String = "",
            provider: String, providerId: String
        ) =
            OAuthLinkedAccount(
                id, userId,
                accessToken, refreshToken,
                OAuthAccountProvider.fromString(provider), providerId
            )
    }

    override fun toEntity(user: User): OAuthLinkedAccountEntity =
        OAuthLinkedAccountEntity(id, user, accessToken, refreshToken, provider, providerId)
}

