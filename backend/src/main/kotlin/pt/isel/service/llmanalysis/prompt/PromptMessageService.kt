package pt.isel.service.llmanalysis.prompt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pt.isel.model.PromptComplexityLevel
import java.io.InputStream

@Service
class PromptMessageService {

    private val logger = LoggerFactory.getLogger(PromptMessageService::class.java)
    private val messages: Map<String, Any> = loadMessages()


    fun getMessage(path: String): String {
        val parts = path.split(".")
        var current: Any? = messages

        for (part in parts) {
            current = (current as? Map<*, *>)?.get(part)
        }

        return (current as? String) ?: "ERROR: mensagem não encontrada: $path"
    }


    fun getMessages(path: String): List<String> {
        val parts = path.split(".")
        var current: Any? = messages

        for (part in parts) {
            current = (current as? Map<*, *>)?.get(part)
        }

        return (current as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }


    fun interpolate(template: String, vararg pairs: Pair<String, Any>): String {
        var result = template
        pairs.forEach { (key, value) ->
            result = result.replace("%{$key}", value.toString())
        }
        return result
    }


    fun getInstructions(complexity: PromptComplexityLevel): List<String> {
        val complexityKey = complexity.name.lowercase()
        val path = "complexity.$complexityKey.instructions"
        return getMessages(path)
    }


    fun getAnalysisPoints(complexity: PromptComplexityLevel, type: String): List<String> {
        val complexityKey = complexity.name.lowercase()
        val path = "complexity.$complexityKey.${type}_points"
        return getMessages(path)
    }



    fun getAnalysisTitle(complexity: PromptComplexityLevel, type: String): String {
        val complexityKey = complexity.name.lowercase()
        val path = "complexity.$complexityKey.${type}_title"
        return getMessage(path)
    }


    @Suppress("UNCHECKED_CAST")
    private fun loadMessages(): Map<String, Any> {
        return try {
            val inputStream: InputStream? =
                this.javaClass.classLoader.getResourceAsStream("prompts/messages.yml")

            if (inputStream == null) {
                logger.error("ERRO: Arquivo prompts/messages.yml não encontrado!")
                return emptyMap()
            }

            val mapper = ObjectMapper(YAMLFactory())
            mapper.readValue(inputStream, Map::class.java) as Map<String, Any>
        } catch (ex: Exception) {
            logger.error("Erro ao carregar prompts/messages.yml: ${ex.message}", ex)
            emptyMap()
        }
    }
}