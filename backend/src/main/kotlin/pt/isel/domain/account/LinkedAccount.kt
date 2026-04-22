package pt.isel.domain.account

import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken

sealed class LinkedAccount(
    val id: Int,
    val userId: Int,
) {
    init{
        require(id >= 0) { "id must be >= 0" }
        require(userId >= 0) { "userId must be >= 0" }
    }
    abstract fun getType(): AccountType
    abstract fun accountCopy(id: Int): LinkedAccount
    abstract fun uniqueKey(): String?
}

data class FormLinkedAccount(
    private val _id: Int, private val _userId: Int, val passwordHash: String
) : LinkedAccount(_id, _userId) {
    init{
        //TODO: CONSIDER MORE THINGS LIKE SIZE CHECK
        require(!passwordHash.isBlank()) { "password hash cannot be blank" }
    }
    companion object{
        fun create(id: Int = 0, userId: Int, passwordHash: String) =
            FormLinkedAccount(id, userId, passwordHash)
    }

    override fun getType(): AccountType = AccountType.FORM
    override fun accountCopy(id: Int) = copy(_id = id)
    override fun uniqueKey(): String? = null
}

data class OAuthLinkedAccount(
    private val _id: Int,
    private val _userId: Int,
    val accessToken: OAuth2AccessToken? = null,
    val refreshToken: OAuth2RefreshToken? = null,
    val provider: AccountType,
    val providerId: String
) : LinkedAccount(_id, _userId) {
    init{
        require(provider != AccountType.FORM) { "provider cannot be form"}
        require(!providerId.isBlank()) { "provider id must not be blank" }
    }

    companion object{
        fun create(
            id: Int = 0, userId: Int,
            accessToken: OAuth2AccessToken? = null, refreshToken: OAuth2RefreshToken? = null,
            provider: String, providerId: String
        ) =
            OAuthLinkedAccount(
                id, userId,
                accessToken, refreshToken,
                AccountType.fromString(provider), providerId
            )
    }

    override fun getType(): AccountType = provider
    override fun accountCopy(id: Int): LinkedAccount = copy(_id = id)
    override fun uniqueKey(): String = providerId
}

enum class AccountType(val type: String, val max: Int?){
    FORM("form",1),
    GOOGLE("google",1),
    GITHUB("github",null);

    companion object {
        fun fromString(type: String): AccountType =
            when (type.lowercase()) {
                FORM.type -> FORM
                GOOGLE.type -> GOOGLE
                GITHUB.type -> GITHUB
                else -> throw IllegalArgumentException("Invalid account type: $type")
            }
    }
}