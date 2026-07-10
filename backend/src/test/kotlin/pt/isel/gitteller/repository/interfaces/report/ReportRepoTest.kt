package pt.isel.gitteller.repository.interfaces.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.domain.report.SearchInfo
import pt.isel.entity.account.User
import pt.isel.entity.report.Report
import pt.isel.gitteller.repository.interfaces.RepoTest
import pt.isel.domain.report.CommitAnalysis
import pt.isel.domain.report.GitAnalysis
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IReportRepository
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

abstract class ReportRepoTest: RepoTest<Report> {
    val validEmail = "test@email.com"
    val validUsername = "test"

    val validUser = User(0, validEmail, validUsername)
    val validCommitAnalysis = CommitAnalysis(
        "test", "test", "test", 1, Instant.now(), "test", 1, 1
    )

    val validGitAnalysis = GitAnalysis(
        SearchInfo("test", "test", "test", "test"),
        "llm_test",
        mapOf(validUsername to listOf(validCommitAnalysis)),
        null,
        Instant.now().minus(Duration.ofDays(1)),
        Instant.now()
    )

    abstract override fun repo(): IReportRepository
    abstract fun userRepo(): IUserRepository


    fun createReport(
        user: User = validUser
    ) = Report(0, user, gitAnalysis = validGitAnalysis, pdf = byteArrayOf(1))

    override fun createEntity(): Report {
        val user = userRepo().create(validUser)

        return createReport(user = user)
    }

    override fun updateEntity(entity: Report): Report =
        entity.copy(pdf = byteArrayOf(2))

    override fun assertEquality(expected: Report, actual: Report?) {
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.user.id, actual?.user?.id)
        assertEquals(expected.gitAnalysis, actual?.gitAnalysis)
        assertTrue(expected.pdf.contentEquals(actual?.pdf))
    }

    @Test
    fun `method findByUserId returns list of reports belonging to user`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected1 = repo.create(createReport(user))
        val expected2 = repo.create(createReport(user))

        val expected = listOf(expected1, expected2)

        val actual = repo.findByUserId(user.id)

        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { i, expected ->  assertEquality(expected, actual[i])}
    }

    @Test
    fun `method findByUserId returns empty list if no reports belonging to user`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val actual = repo.findByUserId(user.id)

        assertEquals(emptyList<Report>(), actual)
    }


    @Test
    fun `method findByIdAndUserId returns correct OAuthLinkedAccount`() {
        val repo = repo()
        val userRepo = userRepo()

        val user = userRepo.create(validUser)

        val expected = repo.create(createReport(user = user))

        val actual = repo.findByIdAndUserId(expected.id, expected.user.id)

        assertEquality(expected, actual)
    }

    @Test
    fun `method findByIdAndUserId returns null if userId not found`() {
        val repo = repo()

        val actual = repo.findByIdAndUserId(1, 1)

        assertNull(actual)
    }
}