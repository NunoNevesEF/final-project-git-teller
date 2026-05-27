package pt.isel.service.llmanalysis

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pt.isel.domain.GitCommunication
import pt.isel.model.AnalysisMode
import pt.isel.service.llmanalysis.util.AnalysisLimits
import java.io.ByteArrayOutputStream

data class FileCandidate(
    val entry: DiffEntry,
    val oldPath: String?,
    val newPath: String?,
    val changeType: String,
    val insertions: Int,
    val deletions: Int,
    val score: Int,
)

@Service
class CommitFileFilteringService {

    private val logger = LoggerFactory.getLogger(CommitFileFilteringService::class.java)

    fun filterAndRankFiles(
        gitCommunication: GitCommunication,
        repository: Repository,
        commit: RevCommit,
        parent: RevCommit?,
        limits: AnalysisLimits,
        maxRelevantFiles: Int
    ): Pair<List<FileCandidate>, List<Pair<String, String>>> {
        val oldTree = parent?.let { gitCommunication.getTreeParser(it) } ?: EmptyTreeIterator()
        val newTree = gitCommunication.getTreeParser(commit)

        val allScanned = mutableListOf<FileCandidate>()
        val rejected = mutableListOf<Pair<String, String>>()

        DiffFormatter(ByteArrayOutputStream()).use { formatter ->
            formatter.setRepository(repository)
            formatter.setDetectRenames(true)

            formatter.scan(oldTree, newTree).forEach { entry ->
                val oldPath = entry.oldPath.takeIf { it != DiffEntry.DEV_NULL }
                val newPath = entry.newPath.takeIf { it != DiffEntry.DEV_NULL }
                val fullPath = newPath ?: oldPath ?: return@forEach

                if (!isRelevantPath(fullPath)) {
                    rejected.add(fullPath to "Caminho irrelevante (excluído por padrão)")
                    return@forEach
                }

                val edits = formatter.toFileHeader(entry).toEditList()
                val insertions = edits.sumOf { it.endB - it.beginB }
                val deletions = edits.sumOf { it.endA - it.beginA }

                val fileCandidate = FileCandidate(
                    entry = entry,
                    oldPath = oldPath,
                    newPath = newPath,
                    changeType = entry.changeType.name,
                    insertions = insertions,
                    deletions = deletions,
                    score = scoreFile(fullPath, insertions, deletions)
                )

                allScanned.add(fileCandidate)
            }
        }

        logger.debug("Ficheiros encontrados no commit: ${(allScanned.size + rejected.size)}")
        logger.debug("Ficheiros relevantes: ${allScanned.size}")
        logger.debug("Ficheiros rejeitados: ${rejected.size}")

        if (rejected.isNotEmpty()) {
            logger.debug("Ficheiros rejeitados:")
            rejected.forEach { (path, reason) ->
                logger.debug("  ✗ $path | Motivo: $reason")
            }
        }

        val sorted = allScanned.sortedByDescending { it.score }
        val selected = sorted.take(maxRelevantFiles)

        logger.debug("Ficheiros ordenados por score (top ${selected.size}):")
        sorted.forEachIndexed { idx, candidate ->
            val mark = if (idx < selected.size) "✓" else "✗"
            logger.debug("  $mark ${candidate.newPath ?: candidate.oldPath} | Score: ${candidate.score} | +${candidate.insertions} -${candidate.deletions}")
        }

        return selected to rejected
    }

    private fun isRelevantPath(path: String): Boolean {
        val lower = path.lowercase()

        val irrelevantPrefixes = listOf(
            "node_modules/",
            "vendor/",
            ".gradle/",
            "build/",
            "target/",
            ".git/"
        )
        if (irrelevantPrefixes.any { lower.startsWith(it) }) return false

        val irrelevantSuffixes = listOf(
            ".lock",
            ".sum",
            "package-lock.json",
            "yarn.lock",
            ".env"
        )
        if (irrelevantSuffixes.any { lower.endsWith(it) }) return false

        val irrelevantPatterns = listOf(
            ".min.js",
            ".min.css",
            ".png",
            ".jpg",
            ".jpeg",
            ".gif",
            ".class"
        )
        if (irrelevantPatterns.any { lower.contains(it) }) return false

        return true
    }

    fun scoreFile(path: String, insertions: Int, deletions: Int): Int {
        val lower = path.lowercase()
        var score = insertions + deletions

        // Source code boost
        if (lower.startsWith("src/")) score += 100
        if (lower.contains("/main/")) score += 50

        // Language boost
        when {
            lower.endsWith(".kt") || lower.endsWith(".java") -> score += 40
            lower.endsWith(".ts") || lower.endsWith(".tsx") -> score += 40
            lower.endsWith(".py") || lower.endsWith(".go") -> score += 35
        }

        // Layer boost — lógica de negócio é mais relevante
        when {
            lower.contains("service") -> score += 40
            lower.contains("controller") || lower.contains("handler") -> score += 35
            lower.contains("repository") || lower.contains("dao") -> score += 30
            lower.contains("domain") || lower.contains("model") -> score += 25
            lower.contains("util") || lower.contains("helper") -> score += 10
        }

        // Test files — relevantes mas menos que produção
        if (lower.contains("test") || lower.contains("spec")) score -= 10

        // Config/docs — penalizar mais
        when {
            lower.endsWith(".md") || lower.endsWith(".txt") -> score -= 30
            lower.endsWith(".json") || lower.endsWith(".yml") || lower.endsWith(".yaml") -> score -= 15
            lower.endsWith(".xml") && !lower.contains("pom") -> score -= 20
        }

        return score
    }

    fun adaptiveMaxFiles(commitCount: Int, mode: AnalysisMode): Int = when {
        mode == AnalysisMode.META -> 15
        commitCount == 1 -> 10
        commitCount <= 5 -> 7
        commitCount <= 15 -> 5
        else -> 3
    }


}