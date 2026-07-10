package pt.isel.gitteller.repository.jpa.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import pt.isel.entity.account.model.Role
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.account.UserRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.jpa.account.UserRepoJpaAdapter
import pt.isel.repository.jpa.account.UserRepoJpa

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["/db/reset-schema.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class UserRepoJpaTest(
    @Autowired val jpa: UserRepoJpa
): UserRepoTest(){
    companion object {
        @Container
        @JvmStatic
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
    }

    override fun repo(): IUserRepository = UserRepoJpaAdapter(jpa)
}