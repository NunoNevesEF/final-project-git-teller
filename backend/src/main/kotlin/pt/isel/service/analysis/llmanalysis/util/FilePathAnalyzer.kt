package pt.isel.service.analysis.llmanalysis.util

//TODO: DOCUMENT

object FilePathAnalyzer {

    fun inferCategoryFromPath(path: String?): String? = path?.let {
        val lower = it.lowercase()
        when {
            lower.contains("/controller") -> "controller"
            lower.contains("/service") -> "service"
            lower.contains("/repository") -> "repository"
            lower.contains("/test") || lower.contains("/spec") -> "test"
            lower.endsWith(".md") || lower.endsWith(".txt") -> "docs"
            lower.contains("resources") || lower.endsWith(".properties") -> "config"
            else -> "code"
        }
    }

    fun extractTrailers(fullMessage: String?): List<String> {
        if (fullMessage.isNullOrBlank()) return emptyList()
        val lines = fullMessage.lines().map { it.trim() }
        val trailers = mutableListOf<String>()
        for (line in lines.reversed()) {
            if (line.isEmpty()) break
            if (line.contains(":") || line.contains("Fixes") || line.contains("Co-authored-by")) {
                trailers.add(0, line)
            }
        }
        return trailers
    }
}