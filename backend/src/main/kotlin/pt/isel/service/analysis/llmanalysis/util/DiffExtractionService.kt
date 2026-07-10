package pt.isel.service.analysis.llmanalysis.util

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

//TODO: DOCUMENT

@Service
class DiffExtractionService {

    fun extractPatch(repository: Repository, entry: DiffEntry, maxChars: Int): String {
        val out = ByteArrayOutputStream()
        DiffFormatter(out).use { formatter ->
            formatter.setRepository(repository)
            formatter.setContext(3) // linhas de contexto explícitas
            formatter.format(entry)
        }

        val fullDiff = out.toString(StandardCharsets.UTF_8.name())
        return filterAndTruncate(fullDiff, maxChars)
    }

    private fun filterAndTruncate(diff: String, maxChars: Int): String {
        val lines = diff.lines()
        val result = mutableListOf<String>()

        for (line in lines) {
            when {

                line.startsWith("diff --git") ||
                        line.startsWith("index ") ||
                        line.startsWith("---") ||
                        line.startsWith("+++") ||
                        line.startsWith("@@") -> result.add(line)

                line.startsWith("-") -> result.add(line)

                line.startsWith("+") -> {
                    if (!isPureNoise(line.substring(1))) result.add(line)
                }

                line.startsWith(" ") -> result.add(line)
            }
        }

        if (result.isEmpty()) return "[No relevant changes]"

        val filtered = result.joinToString("\n")


        return if (filtered.length <= maxChars) {
            sanitizeAndTruncate(filtered, maxChars)
        } else {
            truncateMiddle(filtered, maxChars)
        }
    }


    private fun isPureNoise(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isEmpty() ||
                trimmed.matches(Regex("^[{}()\\[\\];,]*$"))
    }


    private fun truncateMiddle(text: String, maxChars: Int): String {
        val half = maxChars / 2
        val start = text.take(half)
        val end = text.takeLast(half)
        return "$start\n...[truncated]...\n$end"
    }


    fun sanitizeAndTruncate(text: String, maxChars: Int): String {
        val sanitized = text.replace("```", "``\\`")
        return if (sanitized.length <= maxChars) sanitized else sanitized.take(maxChars) + "\n...[truncated]"
    }
}