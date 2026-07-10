package pt.isel.gitteller.repository.jpa.report

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import pt.isel.entity.account.User
import pt.isel.entity.report.schedule.JobStateEmbeddable
import pt.isel.entity.report.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportEntity
import pt.isel.entity.report.schedule.ScheduledReportJobEntity
import pt.isel.gitteller.repository.interfaces.report.ScheduledReportRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IScheduledReportRepository
import pt.isel.repository.jpa.account.UserRepoJpa
import pt.isel.repository.jpa.account.UserRepoJpaAdapter
import pt.isel.repository.jpa.report.ScheduledReportRepoJpaAdapter
import pt.isel.repository.jpa.report.ScheduledReportRepoJpa
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = ["/db/reset-schema.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ScheduledReportRepoJpaTest(
    @Autowired val jpa: ScheduledReportRepoJpa,
    @Autowired val userJpa: UserRepoJpa,
) : ScheduledReportRepoTest() {
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

    override fun repo(): IScheduledReportRepository = ScheduledReportRepoJpaAdapter(jpa)

    override fun userRepo(): IUserRepository = UserRepoJpaAdapter(userJpa)

    override fun createScheduledReportEntity(
        repoUri: String,
        nextRunAt: Instant,
        lastRunAt: Instant,
        dataFrom: Instant,
        user: User,
        isCancelled: Boolean,
        doAddJob: Boolean
    ): ScheduledReportEntity= OneTimeScheduledReportEntity(
        0, repoUri, nextRunAt, lastRunAt, lastRunAt, isCancelled, ""
    ).apply{
        this.user = user
        if(doAddJob) this.addJob(
            ScheduledReportJobEntity(
                dataFrom = dataFrom,
                dataTo = nextRunAt,
                scheduledFor = nextRunAt,
                retryCount = 0,
                state = JobStateEmbeddable.pending(nextRunAt),
            )
        )
    }

    override fun updateEntity(entity: ScheduledReportEntity): ScheduledReportEntity {
        val job = entity.jobs.first()

        entity.updateJob(job.id){
            job.state = JobStateEmbeddable.running(entity.nextRunAt!!)
            job
        }!!
        return entity
    }
}