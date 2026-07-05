package pt.isel.controller.report

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.model.report.GitAnalysis
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.model.GenericError
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.report.FailureDoNotRetry
import pt.isel.service.report.GitAnalysisService
import pt.isel.utils.Success
import pt.isel.utils.leftOrNull

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/gitCommunication")
class GitCommunicationController(
    private val gitAnalysisService: GitAnalysisService,
){

    @PostMapping("/gitAnalysis")
    fun generateAnalysis(
        @RequestBody request: GitAnalysisRequest,
    ): ResponseEntity<Any> {
        return when (val result = gitAnalysisService.analyze(request, token = null)) {
            is Success -> ResponseEntity.ok(result.right)
//            else -> ResponseEntity.status(result.leftOrNull()!!.toStatus()).build()
            else -> {
                val error = result.leftOrNull() as FailureDoNotRetry
                ResponseEntity
                    .status(error.toStatus())
                    .body(
                        GenericError(
                            code = error.toStatus(),
                            message = error.err.message
                        )
                    )
            }
        }
    }
}

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/gitCommunication")
class GitCommunicationPrivateController(
    private val gitAnalysisService: GitAnalysisService,
    private val linkedAccountService: LinkedAccountService
) {

    @PostMapping("/gitAnalysis")
    fun generateAnalysis(
        @RequestBody request: GitAnalysisRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<GitAnalysis> {

        val accountResult =
            linkedAccountService.findUserOAuthAccount(
                request.gitAccountId ?: return ResponseEntity.badRequest().build(),
                principal.getUserId()
            )

        if (accountResult !is Success)
            return ResponseEntity.notFound().build()

        val result = gitAnalysisService.analyze(
            request,
            token = accountResult.right.accessToken
        )

        return when (result) {
            is Success -> ResponseEntity.ok(result.right)
            else -> ResponseEntity.notFound().build()
        }
    }
}

