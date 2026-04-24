package pt.isel.repository.jdbc.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import java.sql.Statement
import java.time.Instant

@Repository
@ConditionalOnProperty(prefix = "app.repository", name = ["mode"], havingValue = "jdbc")
class LinkedAccountRepoJdbc(
    private val jdbcTemplate: JdbcTemplate
) : ILinkedAccountRepository {

    override fun create(entity: LinkedAccount): LinkedAccount {
        val keyHolder: KeyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            connection.prepareStatement(
                """
                INSERT INTO linked_accounts (
                    user_id, type, provider_id, password_hash, access_token, refresh_token
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS
            ).apply {
                setInt(1, entity.userId)
                setString(2, entity.getType().type)
                setString(3, entity.uniqueKey())
                setString(4, when (entity) {
                    is FormLinkedAccount -> entity.passwordHash
                    else -> null
                })
                setString(5, when (entity) {
                    is OAuthLinkedAccount -> entity.accessToken?.tokenValue
                    else -> null
                })
                setString(6, when (entity) {
                    is OAuthLinkedAccount -> entity.refreshToken?.tokenValue
                    else -> null
                })
            }
        }, keyHolder)

        val id = (keyHolder.keys?.get("id") as? Number)?.toInt()
            ?: keyHolder.key?.toInt()
            ?: error("Failed to retrieve generated linked account id")

        return entity.accountCopy(id)
    }

    override fun read(id: Int): LinkedAccount? =
        jdbcTemplate.query(
            """
            SELECT id, user_id, type, provider_id, password_hash, access_token, refresh_token
            FROM linked_accounts
            WHERE id = ?
            """.trimIndent(),
            { rs, _ -> mapRow(rs) },
            id
        ).firstOrNull()

    override fun readByUser(userId: Int): List<LinkedAccount>? {
        val result = jdbcTemplate.query(
            """
            SELECT id, user_id, type, provider_id, password_hash, access_token, refresh_token
            FROM linked_accounts
            WHERE user_id = ?
            ORDER BY id
            """.trimIndent(),
            { rs, _ -> mapRow(rs) },
            userId
        )

        return result.takeIf { it.isNotEmpty() }
    }

    override fun readByUserAndType(userId: Int, type: String): List<LinkedAccount>? {
        val result = jdbcTemplate.query(
            """
            SELECT id, user_id, type, provider_id, password_hash, access_token, refresh_token
            FROM linked_accounts
            WHERE user_id = ? AND type = ?
            ORDER BY id
            """.trimIndent(),
            { rs, _ -> mapRow(rs) },
            userId,
            type
        )

        return result.takeIf { it.isNotEmpty() }
    }

    override fun readByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount? {
        val sql = if (key == null) {
            """
            SELECT id, user_id, type, provider_id, password_hash, access_token, refresh_token
            FROM linked_accounts
            WHERE user_id = ? AND type = ? AND provider_id IS NULL
            """.trimIndent()
        } else {
            """
            SELECT id, user_id, type, provider_id, password_hash, access_token, refresh_token
            FROM linked_accounts
            WHERE user_id = ? AND type = ? AND provider_id = ?
            """.trimIndent()
        }

        val args = if (key == null) arrayOf(userId, type) else arrayOf(userId, type, key)

        return jdbcTemplate.query(
            sql,
            { rs, _ -> mapRow(rs) },
            *args
        ).firstOrNull()
    }

    override fun update(entity: LinkedAccount): LinkedAccount? {
        val updated = jdbcTemplate.update(
            """
            UPDATE linked_accounts
            SET user_id = ?, type = ?, provider_id = ?, password_hash = ?, access_token = ?, refresh_token = ?
            WHERE id = ?
            """.trimIndent(),
            entity.userId,
            entity.getType().type,
            entity.uniqueKey(),
            when (entity) {
                is FormLinkedAccount -> entity.passwordHash
                else -> null
            },
            when (entity) {
                is OAuthLinkedAccount -> entity.accessToken?.tokenValue
                else -> null
            },
            when (entity) {
                is OAuthLinkedAccount -> entity.refreshToken?.tokenValue
                else -> null
            },
            entity.id
        )

        return if (updated == 0) null else entity
    }

    override fun delete(id: Int): LinkedAccount? {
        val existing = read(id) ?: return null

        jdbcTemplate.update(
            "DELETE FROM linked_accounts WHERE id = ?",
            id
        )

        return existing
    }

    override fun deleteByUserTypeAndKey(
        userId: Int,
        type: String,
        key: String?
    ): LinkedAccount? {
        val existing = readByUserTypeAndKey(userId, type, key) ?: return null

        jdbcTemplate.update(
            "DELETE FROM linked_accounts WHERE id = ?",
            existing.id
        )

        return existing
    }

    private fun mapRow(rs: java.sql.ResultSet): LinkedAccount {
        val id = rs.getInt("id")
        val userId = rs.getInt("user_id")
        val type = rs.getString("type")
        val providerKey = rs.getString("provider_id")
        val passwordHash = rs.getString("password_hash")
        val accessTokenValue = rs.getString("access_token")
        val refreshTokenValue = rs.getString("refresh_token")

        return when (AccountType.fromString(type)) {
            AccountType.FORM -> FormLinkedAccount(
                id, userId,
                passwordHash ?: error("Form account without password_hash")
            )

            AccountType.GOOGLE, AccountType.GITHUB -> OAuthLinkedAccount(
                id, userId,
                accessToken = accessTokenValue?.let {
                    OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        it,
                        Instant.now(),
                        Instant.now().plusSeconds(3600),
                        emptySet()
                    )
                },
                refreshToken = refreshTokenValue?.let {
                    OAuth2RefreshToken(it, Instant.now())
                },
                provider = AccountType.fromString(type),
                providerId = providerKey ?: error("OAuth account without provider_id")
            )
        }
    }
}