package pt.isel.controller.report

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.model.report.GitAnalysis
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.git.GitAnalysisService
import pt.isel.utils.Failure
import pt.isel.utils.Success

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/gitCommunication")
class GitCommunicationPublicController(
    private val gitAnalysisService: GitAnalysisService,
){
    @GetMapping("/gitAnalysis")
    fun generateAnalysis(@RequestParam repoURI: String): ResponseEntity<GitAnalysis> {
        return when (val gitAnalysis = gitAnalysisService.createAnalysis(repoURI)){
            is Success -> ResponseEntity.ok(gitAnalysis.right)
            else -> ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/commitAnalysis")
    fun analyzeCommitWithLLM(@RequestBody request: AnalysisRequestWrapper): ResponseEntity<GitAnalysis> {
        return when (val result = gitAnalysisService.analyzeCommit(
            repoURI = request.repoURI,
            flag = request.flag,
            byShas= request.byShas,
            byDateRange= request.byDateRange,
        )) {
            is Success -> ResponseEntity.ok(result.right)
            else -> ResponseEntity.notFound().build()
        }
    }
}

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/gitCommunication")
class GitCommunicationPrivateController(
    private val gitAnalysisService: GitAnalysisService,
    private val linkedAccountService: LinkedAccountService
){
    @GetMapping("/gitAnalysis")
    fun generateAnalysis(
        @RequestParam repoURI: String,
        @RequestParam gitAccountId: Int,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<GitAnalysis> {
        val accountResult = linkedAccountService.findUserOAuthAccount(gitAccountId, principal.getUserId())
        if (accountResult !is Success) return ResponseEntity.notFound().build()

        return when(val analysisResult = gitAnalysisService.createAnalysis(repoURI, accountResult.right.accessToken)){
            is Success -> ResponseEntity.ok(analysisResult.right)
            is Failure -> ResponseEntity.notFound().build() //TODO: REPLACE ERROR RESPONSE WITH PROPER STATUS
        }
    }

    @PostMapping("/commitAnalysis")
    fun analyseCommitWithLLM(
        @RequestBody request: AnalysisRequestWrapper,
        @RequestParam gitAccountId: Int,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<GitAnalysis> {
        val accountResult = linkedAccountService.findUserOAuthAccount(gitAccountId, principal.getUserId())
        if (accountResult !is Success) return ResponseEntity.notFound().build()

        return when (
            val result = gitAnalysisService.analyzeCommit(
                repoURI = request.repoURI, token = accountResult.right.accessToken,
                flag = request.flag, byShas= request.byShas, byDateRange= request.byDateRange,
            )
        ) {
            is Success -> ResponseEntity.ok(result.right)
            else -> ResponseEntity.notFound().build() //TODO: REPLACE ERROR RESPONSE WITH PROPER STATUS
        }
    }
}

data class AnalysisRequestWrapper(
    val repoURI: String,
    val flag: Boolean,
    val byShas: CommitShasAnalysisRequest?,
    val byDateRange: CommitDateRangeAnalysisRequest?
)