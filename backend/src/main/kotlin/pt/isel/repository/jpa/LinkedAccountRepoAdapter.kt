package pt.isel.repository.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.time.Instant
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.entity.LinkedAccountEntity
import pt.isel.repository.interfaces.account.ILinkedAccountRepository

@Repository
@ConditionalOnProperty(
    name = ["app.repository.mode"],
    havingValue = "jpa"
)
class LinkedAccountRepoAdapter(private val jpa: LinkedAccountRepoJpa) : ILinkedAccountRepository {

    override fun create(entity: LinkedAccount): LinkedAccount =
        jpa.save(entity.toEntity()).toDomain()

    override fun findById(id: Int): LinkedAccount? {
        return jpa.findById(id).orElse(null)?.toDomain()
    }

    override fun update(entity: LinkedAccount): LinkedAccount? =
        if (jpa.existsById(entity.id))
            jpa.save(entity.toEntity()).toDomain()
        else null

    override fun delete(id: Int): LinkedAccount? {
        val existing = jpa.findById(id).orElse(null)?.toDomain() ?: return null
        jpa.deleteById(id)
        return existing
    }

    override fun readByUser(userId: Int): List<LinkedAccount>? =
        jpa.findByUserId(userId).map { it.toDomain() }
            .takeIf { it.isNotEmpty() }

    override fun readByUserAndType(userId: Int, type: String): List<LinkedAccount>? =
        jpa.findByUserIdAndType(userId, type).map { it.toDomain() }
            .takeIf { it.isNotEmpty() }

    override fun readByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount? =
        jpa.findByUserIdAndTypeAndProviderId(userId, type, key)
            ?.toDomain()

    override fun deleteByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount? {
        val existing = jpa.findByUserIdAndTypeAndProviderId(userId, type, key)?.toDomain() ?: return null
        jpa.deleteById(existing.id)
        return existing
    }

    fun LinkedAccountEntity.toDomain(): LinkedAccount =
        when (AccountType.fromString(type)) {
            AccountType.FORM ->
                FormLinkedAccount(
                    _id = id,
                    _userId = userId,
                    passwordHash = passwordHash
                        ?: error("Form account missing password_hash")
                )
            AccountType.GOOGLE,
            AccountType.GITHUB ->
                OAuthLinkedAccount(
                    _id = id,
                    _userId = userId,
                    accessToken = accessToken?.let { tokenValue ->
                        org.springframework.security.oauth2.core.OAuth2AccessToken(
                            org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                            tokenValue,
                            Instant.now(),
                            null
                        )
                    },
                    refreshToken = refreshToken?.let { tokenValue ->
                        org.springframework.security.oauth2.core.OAuth2RefreshToken(
                            tokenValue,
                            Instant.now()
                        )
                    },
                    provider = AccountType.fromString(type),
                    providerId = providerId
                        ?: error("OAuth account missing providerId")
                )
        }

    fun LinkedAccount.toEntity(): LinkedAccountEntity =
        when (this) {
            is FormLinkedAccount ->
                LinkedAccountEntity(
                    id = id,
                    userId = userId,
                    type = getType().type,
                    passwordHash = passwordHash
                )
            is OAuthLinkedAccount ->
                LinkedAccountEntity(
                    id = id,
                    userId = userId,
                    type = provider.type,
                    providerId = providerId,
                    accessToken = accessToken?.tokenValue,
                    refreshToken = refreshToken?.tokenValue
                )
        }
}
