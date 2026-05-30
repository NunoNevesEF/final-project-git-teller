package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.BatchCommitAnalysisResponse
import pt.isel.domain.CommitAnalysisRequest
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.service.llmanalysis.CommitAnalysisService


@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/gitAnalysis")
class CommitAnalysisController(
    private val orchestrator: CommitAnalysisService,
) {
    @PostMapping("/commitAnalysis")
    fun postCommitAnalysis(@RequestBody request: CommitAnalysisRequest) =
        ResponseEntity.ok(orchestrator.analyzeCommit(request))
    @PostMapping("/by-shas")
    fun analyzeByShas(
        @RequestBody request: CommitShasAnalysisRequest
    ): ResponseEntity<BatchCommitAnalysisResponse> {
        val response = orchestrator.analyzeCommitsByShas(request)
        return ResponseEntity.ok(response)
    }
    @PostMapping("/by-date-range")
    fun analyzeByDateRange(
        @RequestBody request: CommitDateRangeAnalysisRequest
    ): ResponseEntity<BatchCommitAnalysisResponse> {
        val response = orchestrator.analyzeCommitsBetweenDates(request)
        return ResponseEntity.ok(response)
    }

}