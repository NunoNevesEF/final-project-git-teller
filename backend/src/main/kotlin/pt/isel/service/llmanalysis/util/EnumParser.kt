package pt.isel.service.llmanalysis.util

import org.slf4j.LoggerFactory
import pt.isel.model.AnalysisMode
import pt.isel.model.PromptComplexityLevel

object EnumParser {
    private val logger = LoggerFactory.getLogger(EnumParser::class.java)

    fun parseComplexityLevel(complexity: String?): PromptComplexityLevel = try {
        when {
            complexity.isNullOrBlank() -> PromptComplexityLevel.MEDIUM
            else -> PromptComplexityLevel.valueOf(complexity.uppercase())
        }
    } catch (_: IllegalArgumentException) {
        logger.warn("Complexidade inválida: $complexity. Usando MEDIUM por defeito.")
        PromptComplexityLevel.MEDIUM
    }

    fun parseAnalysisMode(mode: String?): AnalysisMode = try {
        when {
            mode.isNullOrBlank() -> AnalysisMode.DIFF
            else -> AnalysisMode.valueOf(mode.uppercase())
        }
    } catch (_: IllegalArgumentException) {
        logger.warn("AnalysisMode inválido: $mode. Usando DIFF por defeito.")
        AnalysisMode.DIFF
    }
}