package pt.isel.gitteller.repository.jpa.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.account.FormLinkedAccountRepoTest
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.jpa.account.UserRepoJpaAdapter
import pt.isel.repository.jpa.account.UserRepoJpa
import pt.isel.repository.jpa.account.FormLinkedAccountRepoJpaAdapter
import pt.isel.repository.jpa.account.FormLinkedAccountRepoJpa

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["/db/reset-schema.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class FormLinkedAccountRepoJpaTest(
    @Autowired val jpa: FormLinkedAccountRepoJpa,
    @Autowired val userJpa: UserRepoJpa,
): FormLinkedAccountRepoTest() {
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

    override fun repo(): IFormLinkedAccountRepository =
        FormLinkedAccountRepoJpaAdapter(jpa)

    override fun userRepo(): IUserRepository =
        UserRepoJpaAdapter(userJpa)


}