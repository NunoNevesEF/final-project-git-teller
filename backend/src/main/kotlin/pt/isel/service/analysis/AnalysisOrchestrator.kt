package pt.isel.service.analysis

import org.springframework.stereotype.Service
import pt.isel.model.report.GitAnalysis
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.service.error.ServiceError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.error.MissingGitLinkedAccountId
import pt.isel.utils.Either
import pt.isel.utils.failure
import pt.isel.utils.onFailure
import pt.isel.utils.rightOrNull

//TODO: DOCUMENT

@Service
class AnalysisOrchestrator(
    private val linkedAccountService: LinkedAccountService,
    private val gitAnalysisService: GitAnalysisService,
){
    fun analyseWithToken(request: GitAnalysisRequest, userId: Int): Either<ServiceError, GitAnalysis> {
        val linkedAccountResult = linkedAccountService.findUserGitAccount(
            request.gitAccountId ?: return failure(MissingGitLinkedAccountId),
            userId
        ).onFailure { return(failure(it)) }

        val gitLinkedAccount = linkedAccountResult.rightOrNull()!!

        return gitAnalysisService.analyze(request, gitLinkedAccount.accessToken)
    }
}