package pt.isel.repository.jdbc.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import pt.isel.domain.account.Role
import pt.isel.domain.account.User
import pt.isel.repository.IUserRepository
import java.sql.Statement

@Repository
@ConditionalOnProperty(prefix = "app.repository", name = ["mode"], havingValue = "jdbc")
class UserRepoJdbc(
    private val jdbcTemplate: JdbcTemplate
) : IUserRepository {

    override fun create(entity: User): User {
        val keyHolder: KeyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            connection.prepareStatement(
                """
                INSERT INTO users (email, user_name, role)
                VALUES (?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS
            ).apply {
                setString(1, entity.email)
                setString(2, entity.userName)
                setString(3, entity.role.name)
            }
        }, keyHolder)

        val id = (keyHolder.keys?.get("id") as? Number)?.toInt()
            ?: keyHolder.key?.toInt()
            ?: error("Failed to retrieve generated user id")

        return entity.copy(id = id)
    }

    override fun read(id: Int): User? =
        jdbcTemplate.query(
            """
            SELECT id, email, user_name, role
            FROM users
            WHERE id = ?
            """.trimIndent(),
            { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    userName = rs.getString("user_name"),
                    role = Role.valueOf(rs.getString("role"))
                )
            },
            id
        ).firstOrNull()

    override fun read(email: String): User? =
        jdbcTemplate.query(
            """
            SELECT id, email, user_name, role
            FROM users
            WHERE email = ?
            """.trimIndent(),
            { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    userName = rs.getString("user_name"),
                    role = Role.valueOf(rs.getString("role"))
                )
            },
            email
        ).firstOrNull()

    override fun readOrCreateByEmail(entity: User): User =
        read(entity.email) ?: create(entity)

    override fun update(entity: User): User? {
        val updated = jdbcTemplate.update(
            """
            UPDATE users
            SET email = ?, user_name = ?, role = ?
            WHERE id = ?
            """.trimIndent(),
            entity.email,
            entity.userName,
            entity.role.name,
            entity.id
        )

        return if (updated == 0) null else entity
    }

    override fun delete(id: Int): User? {
        val existing = read(id) ?: return null

        jdbcTemplate.update(
            "DELETE FROM users WHERE id = ?",
            id
        )

        return existing
    }
}