package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.CommitAnalysisRequest
import pt.isel.domain.CommitAnalysisResponse
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.domain.GitAnalysis
import pt.isel.service.git.GitCommunicationService
import pt.isel.utils.Success

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/gitCommunication")
class GitCommunicationController(
    private val gitCommunicationService: GitCommunicationService
){
    @GetMapping("/gitAnalysis")
    fun getGitAnalysis(
        @RequestParam repoURI: String
    ): ResponseEntity<GitAnalysis> {
        return when (val gitAnalysis = gitCommunicationService.getRepoAnalysis(repoURI)){
            is Success -> ResponseEntity.ok(gitAnalysis.right)
            else -> ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/commitAnalysis")
    fun analyzeCommitWithLLM(
        @RequestBody request: AnalysisRequestWrapper
    ): ResponseEntity<GitAnalysis> {
        return when (val result = gitCommunicationService.analyzeCommit(
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


data class AnalysisRequestWrapper(
    val repoURI: String,
    val flag: Boolean,
    val byShas: CommitShasAnalysisRequest?,
    val byDateRange: CommitDateRangeAnalysisRequest?
)