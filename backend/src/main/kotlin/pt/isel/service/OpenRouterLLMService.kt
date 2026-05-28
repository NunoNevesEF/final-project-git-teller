package pt.isel.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class OpenRouterLlmService(
    @Value("\${app.openrouter.api-key}") private val apiKey: String,
    @Value("\${app.openrouter.model:deepseek/deepseek-v4-flash}") private val model: String,
    @Value("\${app.openrouter.base-url:https://openrouter.ai/api/v1}") private val baseUrl: String,
) {// deepseek/deepseek-v4-flash:free         nvidia/nemotron-3-super-120b-a12b:free        google/gemini-3.1-flash-lite
    private val mapper = jacksonObjectMapper()
    private val http = HttpClient.newHttpClient()

    fun ask(prompt: String): JsonNode {
        check(apiKey.isNotBlank()) { "app.openrouter.api-key não está configurado" }

        val body = """{"model":"$model","messages":[{"role":"user","content":${mapper.writeValueAsString(prompt)}}]}"""

        val raw = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        check(raw.statusCode() in 200..299) { "OpenRouter HTTP ${raw.statusCode()}: ${raw.body()}" }



        return mapper.readTree(raw.body())
    }

    fun askText(prompt: String): String =
        ask(prompt)["choices"]?.firstOrNull()?.get("message")?.get("content")?.asText()
            ?: error("OpenRouter returned no choices")
}



