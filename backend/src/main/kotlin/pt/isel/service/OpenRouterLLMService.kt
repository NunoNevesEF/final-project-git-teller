package pt.isel.service

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.responses.ResponseCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


// IMPLEMENTACAO PARA O OPEN ROUTER

@Service
class OpenAiLlmService(
    @Value("\${app.openrouter.api-key}") private val apiKey: String,
    @Value("\${app.openrouter.model:deepseek/deepseek-v4-flash}") private val model: String,
) { // deepseek/deepseek-v4-flash:free         nvidia/nemotron-3-super-120b-a12b:free        google/gemini-3.1-flash-lite
    private val client: OpenAIClient = OpenAIOkHttpClient.builder()
        .apiKey(apiKey)
        .baseUrl("https://openrouter.ai/api/v1")
        .build()

    private val mapper = jacksonObjectMapper()
    fun ask(prompt: String): String {
        if (apiKey.isBlank()) {
            return "Erro: OPENROUTER_API_KEY não configurado"
        }

        return try {
            val response = client.responses().create(
                ResponseCreateParams.builder()
                    .model(model)
                    .input(prompt)
                    .build()
            )

            runCatching { response.toString() }.getOrNull()
                ?: response.toString()
        } catch (ex: Exception) {
            "Erro : ${ex.message}"
        }
    }
}

