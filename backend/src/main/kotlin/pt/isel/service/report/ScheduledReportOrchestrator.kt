package pt.isel.service.report

import org.springframework.stereotype.Service
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.service.error.InvalidScheduledReportDomainArguments
import pt.isel.service.error.ServiceError
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.onFailure
import pt.isel.utils.rightOrNull
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.UnexpectedProvider

/**
 *  `ScheduledReportOrchestrator`
 *
 * The service layer orchestrator responsible for sequenced actions necessary for [ScheduledReportService]
 *
 * @property [scheduledReportService] The service layer scheduled-report-related operations
 * @property [userService] The service layer user-related operations
 * @property [linkedAccountService] The service layer linked-account-related-operations
 * */
@Service
class ScheduledReportOrchestrator(
    private val scheduledReportService: ScheduledReportService,
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService,
){
    /**
     * Creates a new scheduled report.
     *
     * @param dto The scheduled report creation request.
     * @param userId The identifier of user who owns the scheduled report.
     *
     * @return
     * - `Unit` on success.
     * - [UserNotFound] if no user matches [userId].
     * - [LinkedAccountNotFound] if no account found matching dto's git account id.
     * - [UnexpectedProvider] if account's provider is not a Git service account.
     * - [InvalidScheduledReportDomainArguments] if the supplied arguments are invalid.
     */
    fun createScheduledReport(
        dto: CreateScheduleReportDTO,
        userId: Int
    ): Either<ServiceError, Unit> {
        val userResult = userService.findById(userId).onFailure { return(failure(it)) }
        val user = userResult.rightOrNull()!!

        val gitAccountResult = when(val id = dto.gitAccountId){
            null -> null
            else -> linkedAccountService.findUserGitAccount(id, userId)
        }
        gitAccountResult?.onFailure { return(failure(it)) }
        val gitAccount = gitAccountResult?.rightOrNull()

        return scheduledReportService.createScheduledReport(dto, user, gitAccount)
    }
}