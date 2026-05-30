package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.LlmPromptRequest
import pt.isel.service.OpenRouterLlmService



@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/llm")
class LlmController(
    private val llmService: OpenRouterLlmService
) {

    @PostMapping("/askText")
    fun askText(@RequestBody request: LlmPromptRequest): ResponseEntity<Map<String, String>> {
        val result = llmService.askText(request.prompt)
        return ResponseEntity.ok(
            mapOf("response" to result)
        )
    }
}
