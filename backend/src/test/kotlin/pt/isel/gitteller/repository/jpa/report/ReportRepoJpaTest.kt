package pt.isel.gitteller.repository.jpa.report

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import pt.isel.gitteller.repository.interfaces.report.ReportRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.repository.jpa.account.UserRepoJpa
import pt.isel.repository.jpa.account.UserRepoJpaAdapter
import pt.isel.repository.jpa.report.ReportRepoJpa
import pt.isel.repository.jpa.report.ReportRepoJpaAdapter

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["/db/reset-schema.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ReportRepoJpaTest(
    @Autowired val jpa: ReportRepoJpa,
    @Autowired val userJpa: UserRepoJpa,
): ReportRepoTest() {
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

    override fun repo(): IReportRepository = ReportRepoJpaAdapter(jpa)
    override fun userRepo(): IUserRepository = UserRepoJpaAdapter(userJpa)
}