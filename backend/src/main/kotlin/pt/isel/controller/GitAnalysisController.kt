package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.report.GitAnalysis
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.service.analysis.AnalysisOrchestrator
import pt.isel.service.analysis.GitAnalysisService
import pt.isel.service.error.toHttpStatus
import pt.isel.utils.Failure
import pt.isel.utils.Success

/**
 *  `PublicGitAnalysisController`
 *
 * Exposes analysis related presentation-layer endpoints of the application that do not require authorization.
 * */
@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/gitCommunication")
class PublicGitAnalysisController(
    private val gitAnalysisService: GitAnalysisService,
){
    /**
     * Generates a git analysis for the given parameters to display in the frontend.
     *
     * Returns:
     * - `200 Ok` with the generated [GitAnalysis] on success.
     * - `400 Bad Request` if repoUri or request filters are invalid
     * - `404 Not Found` if the repo is not found or private
     * - `504 Gateway Timeout` if connecting to git repo fails by timeout.
     *
     * @param request The analysis parameters
     *
     * @return A [ResponseEntity] containing the generated [GitAnalysis] on success, or an empty response with the appropriate HTTP status on failure.
     */
    @PostMapping("/gitAnalysis")
    fun generateAnalysis(
        @RequestBody request: GitAnalysisRequest,
    ): ResponseEntity<GitAnalysis> {
        return when (val result = gitAnalysisService.analyze(request, token = null)) {
            is Success -> ResponseEntity.ok(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
}

/**
 * `PrivateGitAnalysisController`
 *
 * Exposes analysis related presentation-layer endpoints of the application that require authorization.
 * */
@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/gitCommunication")
class PrivateGitAnalysisController(
    private val gitAnalysisOrchestrator: AnalysisOrchestrator
) {
    /**
     * Generates a git analysis for the given parameters to display in the frontend.
     *
     * Returns:
     * - `200 Ok` with the generated [GitAnalysis] on success.
     * - `400 Bad Request` if repoUri or request filters are invalid
     * - `404 Not Found` if the repo is not found or is private and user has no permission.
     * - `504 Gateway Timeout` if connecting to git repo fails by timeout.
     *
     * @param request The analysis parameters. Includes an id to the user's associated gitAccount as to access private repos.
     *
     * @return A [ResponseEntity] containing the generated [GitAnalysis] on success, or an empty response with the appropriate HTTP status on failure.
     */
    @PostMapping("/gitAnalysis")
    fun generateAnalysis(
        @RequestBody request: GitAnalysisRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<GitAnalysis> {
        return when (val result = gitAnalysisOrchestrator.analyseWithToken(request, principal.getUserId())) {
            is Success -> ResponseEntity.ok(result.right)
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }
}

