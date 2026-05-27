package pt.isel.service.llmanalysis.util

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Service
class DiffExtractionService {

    fun extractPatch(repository: Repository, entry: DiffEntry, maxChars: Int): String {
        val out = ByteArrayOutputStream()
        DiffFormatter(out).use { formatter ->
            formatter.setRepository(repository)
            formatter.format(entry)
        }

        val fullDiff = out.toString(Charset.defaultCharset().name())


        return extractAdditionsOnly(fullDiff, maxChars)
    }


    private fun extractAdditionsOnly(diff: String, maxChars: Int): String {
        val lines = diff.split("\n")
        val result = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("@@") -> result.add(line)
                line.startsWith("+") && !line.startsWith("+++") -> {
                    val contentLine = line.substring(1) // Remove o "+"
                    if (!isIrrelevantLine(contentLine)) {
                        result.add(line)
                    }
                }
                line.startsWith("-") && !line.startsWith("---") -> { /* ignorar */ }
                line.startsWith(" ") -> { /* ignorar contexto */ }
                line.startsWith("diff --git") || line.startsWith("index ") ||
                        line.startsWith("---") || line.startsWith("+++") -> result.add(line)
            }
        }

        val filtered = result.joinToString("\n")

        if (result.isEmpty() || result.all { it.startsWith("diff") || it.startsWith("index") ||
                    it.startsWith("---") || it.startsWith("+++") ||
                    it.startsWith("@@") }) {
            return "[Sem linhas adicionadas]"
        }

        return sanitizeAndTruncate(filtered, maxChars)
    }


    private fun isIrrelevantLine(line: String): Boolean {
        val trimmed = line.trim()

        // Vazio ou apenas espaço
        if (trimmed.isEmpty()) return true

        // Imports
        if (trimmed.startsWith("import ") || trimmed.startsWith("from ") || trimmed.startsWith("require(")) {
            return true
        }

        // Packages/namespaces
        if (trimmed.startsWith("package ")) return true

        // Comentários apenas com símbolos (linhas de separação)
        if (trimmed.matches(Regex("^(//|/\\*|\\*|--|#|--|--|--|/|\\\\).*$"))) {
            return true
        }

        // Apenas chaves ou parênteses
        if (trimmed.matches(Regex("^[{}()\\[\\];,]*$"))) return true

        // Linhas com apenas whitespace ou comentário vazio
        if (trimmed == "{" || trimmed == "}" || trimmed == "()" || trimmed == "[]") {
            return true
        }

        // Linhas de decoradores/anotações (apenas o símbolo)
        if (trimmed.startsWith("@") && trimmed.length < 50) {
            // Se for algo tipo @Override, @Deprecated, etc
            if (trimmed.matches(Regex("^@[A-Za-z]+$"))) {
                return true
            }
        }

        return false
    }

    fun extractBeforeSnippet(repository: Repository, commit: RevCommit?, path: String?, maxChars: Int): String? {
        if (commit == null || path.isNullOrBlank() || path == DiffEntry.DEV_NULL) return null
        return try {
            val treeWalk = TreeWalk.forPath(repository, path, commit.tree) ?: return null
            val loader = repository.open(treeWalk.getObjectId(0))
            val content = String(loader.bytes, StandardCharsets.UTF_8)
            sanitizeAndTruncate(content, maxChars)
        } catch (_: Exception) {
            null
        }
    }

    fun sanitizeAndTruncate(text: String, maxChars: Int): String {
        val sanitized = text.replace("```", "``\\`")
        return if (sanitized.length <= maxChars) sanitized else sanitized.take(maxChars) + "\n...[truncated]"
    }
}