package pt.isel.service.analysis.llmanalysis

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.springframework.stereotype.Service
import pt.isel.domain.report.GitCommunication
import pt.isel.model.AnalysisMode
import pt.isel.service.analysis.llmanalysis.util.AnalysisLimits
import java.io.ByteArrayOutputStream

//TODO: DOCUMENT

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



    fun filterAndRankFiles(
        gitCommunication: GitCommunication,
        repository: Repository,
        commit: RevCommit,
        parent: RevCommit?,
        limits: AnalysisLimits,
        maxRelevantFiles: Int,
        requestedAnalyses: List<String> = listOf("DEFAULT")
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
                    rejected.add(fullPath to "Not relevant")
                    return@forEach
                }

                val fileHeader = formatter.toFileHeader(entry)
                if (fileHeader.patchType == FileHeader.PatchType.BINARY) {
                    rejected.add(fullPath to "Binary file")
                    return@forEach
                }

                val edits = fileHeader.toEditList()
                val insertions = edits.sumOf { it.endB - it.beginB }
                val deletions = edits.sumOf { it.endA - it.beginA }

                val fileCandidate = FileCandidate(
                    entry = entry,
                    oldPath = oldPath,
                    newPath = newPath,
                    changeType = entry.changeType.name,
                    insertions = insertions,
                    deletions = deletions,
                    score = scoreFile(fullPath, insertions, deletions, requestedAnalyses)
                )

                allScanned.add(fileCandidate)
            }
        }
        val sorted = allScanned.sortedByDescending { it.score }
        val selected = sorted.take(maxRelevantFiles)
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

    private val securityKeywords = listOf(
        "auth", "security", "crypto", "jwt", "oauth", "permission", "cors", "secret", "credential", "csrf"
    )

    fun scoreFile(path: String, insertions: Int, deletions: Int, requestedAnalyses: List<String> = listOf("DEFAULT")): Int {
        val lower = path.lowercase()
        val wants = requestedAnalyses.map { it.uppercase() }
        val wantsTests = "TESTS" in wants
        val wantsSecurity = "SECURITY" in wants
        val wantsArchitecture = "ARCHITECTURE" in wants


        var score = minOf(insertions + deletions, 300)


        if (lower.startsWith("src/")) score += 100
        if (lower.contains("/main/")) score += 50


        when {
            lower.endsWith(".kt") || lower.endsWith(".java") -> score += 40
            lower.endsWith(".ts") || lower.endsWith(".tsx") || lower.endsWith(".js") || lower.endsWith(".jsx") -> score += 40
            lower.endsWith(".py") || lower.endsWith(".go") || lower.endsWith(".rs") ||
                lower.endsWith(".rb") || lower.endsWith(".php") -> score += 35
            lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h") ||
                lower.endsWith(".hpp") || lower.endsWith(".swift") -> score += 35
            lower.endsWith(".sql") -> score += 20
        }


        when {
            lower.contains("service") -> score += 40
            lower.contains("controller") || lower.contains("handler") -> score += 35
            lower.contains("repository") -> score += 30
            lower.contains("domain") || lower.contains("model") -> score += 25
            lower.contains("util") || lower.contains("helper") -> score += 10
        }


        if (wantsSecurity && securityKeywords.any { lower.contains(it) }) score += 40


        if (lower.contains("test") || lower.contains("spec")) {
            score += if (wantsTests) 30 else -10
        }


        when {
            lower.endsWith(".md") || lower.endsWith(".txt") -> score -= 30
            lower.endsWith(".json") || lower.endsWith(".yml") || lower.endsWith(".yaml") ->
                score += if (wantsSecurity || wantsArchitecture) 20 else -15
            lower.endsWith(".xml") && !lower.contains("pom") ->
                score += if (wantsSecurity || wantsArchitecture) 10 else -20
        }

        return score
    }

    fun adaptiveMaxFiles(commitCount: Int, mode: AnalysisMode): Int = when {
        mode == AnalysisMode.META -> 20
        commitCount == 1 -> 15
        commitCount <= 5 -> 13
        commitCount <= 15 -> 10
        else -> 5
    }


}