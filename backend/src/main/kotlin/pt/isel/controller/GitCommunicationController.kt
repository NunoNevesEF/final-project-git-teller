package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.GitAnalysis
import pt.isel.service.GitCommunicationService
import pt.isel.service.Success

@CrossOrigin(origins = ["http://localhost:8080"])
@RestController
@RequestMapping("/api/gitCommunication")
class GitCommunicationController(
    private val gitCommunicationService: GitCommunicationService
){
    @GetMapping("/gitAnalysis")
    fun getGitAnalysis(
        @RequestParam repoURI: String,
    ): ResponseEntity<GitAnalysis> {
        return when (val gitAnalysis = gitCommunicationService.getRepoAnalysis(repoURI)){
            is Success -> ResponseEntity.ok(gitAnalysis.right)
            else -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/test")
    fun test() = "working"
}