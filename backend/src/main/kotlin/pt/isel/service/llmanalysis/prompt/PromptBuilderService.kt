package pt.isel.service.llmanalysis.prompt

import org.springframework.stereotype.Service
import pt.isel.domain.BatchCommitAnalysisContext
import pt.isel.domain.CommitAnalysisContext
import pt.isel.domain.CommitFileChangeDto
import pt.isel.model.AnalysisMode
import pt.isel.model.CommitFileSummary
import pt.isel.model.PromptComplexityLevel

@Service
class PromptBuilderService(
    private val messageService: PromptMessageService
) {

    fun buildSingleCommitPrompt(
        context: CommitAnalysisContext,
        complexity: PromptComplexityLevel = PromptComplexityLevel.MEDIUM,
        settings: List<String> = listOf("DEFAULT")
    ): String = getTemplate(complexity).singleCommitTemplate(context, settings)

    fun buildBatchPrompt(
        batchContext: BatchCommitAnalysisContext,
        complexity: PromptComplexityLevel = PromptComplexityLevel.MEDIUM,
        settings: List<String> = listOf("DEFAULT"),
        mode: AnalysisMode = AnalysisMode.DIFF
    ): String = getTemplate(complexity).batchTemplate(batchContext, settings, mode)

    fun buildSingleCommitPromptMetadata(
        repoURI: String,
        commitSha: String,
        author: String?,
        timestamp: String,
        shortMessage: String,
        fullMessage: String?,
        fileSummaries: List<CommitFileSummary>,
        trailers: List<String> = emptyList(),
        complexity: PromptComplexityLevel = PromptComplexityLevel.MEDIUM,

    ): String = buildString {
        appendMetaPrelude(complexity)
        appendLine()
        appendCommitMetadata(repoURI, commitSha, author, timestamp, shortMessage, fullMessage, trailers)
        appendLine()
        appendLine(messageService.getMessage("common.section_file_summaries"))
        appendFileSummaries(fileSummaries)
        appendLine()
        appendLine(messageService.getMessage("common.section_what_i_want"))
        appendLine(messageService.getMessage("common.meta_single_commit_points"))
    }


    private fun getTemplate(complexity: PromptComplexityLevel): PromptTemplate = when (complexity) {
        PromptComplexityLevel.SIMPLE -> SimpleTemplate()
        PromptComplexityLevel.MEDIUM -> MediumTemplate()
        PromptComplexityLevel.COMPLEX -> ComplexTemplate()
    }

    private sealed class PromptTemplate {
        abstract val singleCommitTemplate: (CommitAnalysisContext, List<String>) -> String
        abstract val batchTemplate: (BatchCommitAnalysisContext, List<String>, AnalysisMode) -> String
    }

    private inner class SimpleTemplate : PromptTemplate() {
        override val singleCommitTemplate: (CommitAnalysisContext, List<String>) -> String = { context, settings ->
            buildString {
                appendDiffPrelude(PromptComplexityLevel.SIMPLE)
                appendLine()
                appendCommitBasicInfo(context)
                appendLine()
                appendLine(messageService.getMessage("common.section_changes"))
                appendFilesWithDiffs(context.files, showDetails = false)
                appendLine()
                appendLine(messageService.getMessage("complexity.simple.single_commit_section_analysis"))
                appendLine(messageService.getMessage("complexity.simple.single_commit_request"))
                appendSelectedSettings(settings, PromptComplexityLevel.SIMPLE)
            }
        }

        override val batchTemplate: (BatchCommitAnalysisContext, List<String>, AnalysisMode) -> String =
            { batch, settings, mode ->
                buildString {
                    appendModePrelude(mode, PromptComplexityLevel.SIMPLE)
                    appendLine()
                    appendLine(
                        messageService.interpolate(
                            messageService.getMessage("complexity.simple.final_intro_1"),
                            "commitCount" to batch.commitCount
                        )
                    )
                    appendLine(
                        messageService.interpolate(
                            messageService.getMessage("complexity.simple.final_intro_2"),
                            "insertions" to batch.totalInsertions,
                            "deletions" to batch.totalDeletions
                        )
                    )
                    appendLine()
                    batch.commits.forEachIndexed { idx, context ->
                        appendLine("### Commit ${idx + 1}/${batch.commitCount}")
                        appendCommitBasicInfo(context)
                        appendLine(messageService.getMessage("common.section_changes"))
                        appendFilesWithDiffs(context.files, showDetails = false)
                        appendLine()
                    }
                    appendLine(messageService.getMessage("complexity.simple.final_focus"))
                    appendSelectedSettings(settings, PromptComplexityLevel.SIMPLE)
                }
            }
    }

    private inner class MediumTemplate : PromptTemplate() {
        override val singleCommitTemplate: (CommitAnalysisContext, List<String>) -> String = { context, settings ->
            buildString {
                appendDiffPrelude(PromptComplexityLevel.MEDIUM)
                appendLine()
                appendCommitFullMetadata(context)
                appendLine()
                appendLine(messageService.getMessage("common.section_files"))
                appendFilesWithDiffs(context.files)
                appendLine()
                appendLine(messageService.getAnalysisTitle(PromptComplexityLevel.MEDIUM, "single_commit"))
                messageService.getAnalysisPoints(PromptComplexityLevel.MEDIUM, "single_commit").forEach { appendLine(it) }
                appendSelectedSettings(settings, PromptComplexityLevel.MEDIUM)
            }
        }

        override val batchTemplate: (BatchCommitAnalysisContext, List<String>, AnalysisMode) -> String =
            { batch, settings, mode ->
                buildString {
                    appendModePrelude(mode, PromptComplexityLevel.MEDIUM)
                    appendLine()
                    appendLine(messageService.getMessage("complexity.medium.final_intro"))
                    appendLine(
                        messageService.interpolate(
                            messageService.getMessage("complexity.medium.final_analysis_intro"),
                            "commitCount" to batch.commitCount
                        )
                    )
                    appendLine()
                    appendLine(messageService.getMessage("complexity.medium.final_executive_summary"))
                    appendLine("- ${messageService.getMessage("common.label_insertions")}: ${batch.totalInsertions}")
                    appendLine("- ${messageService.getMessage("common.label_deletions")}: ${batch.totalDeletions}")
                    appendLine("- ${messageService.getMessage("common.label_period")}: ${batch.fromDate ?: "N/A"} to ${batch.toDate ?: "N/A"}")
                    appendLine()
                    batch.commits.forEachIndexed { idx, context ->
                        appendLine("### Commit ${idx + 1}/${batch.commitCount}")
                        appendCommitFullMetadata(context)
                        appendLine(messageService.getMessage("common.section_files"))
                        appendFilesWithDiffs(context.files)
                        appendLine()
                    }
                    appendLine("### CONSOLIDATED ANALYSIS")
                    messageService.getAnalysisPoints(PromptComplexityLevel.MEDIUM, "final").forEach { appendLine(it) }
                    appendSelectedSettings(settings, PromptComplexityLevel.MEDIUM)
                }
            }
    }

    private inner class ComplexTemplate : PromptTemplate() {
        override val singleCommitTemplate: (CommitAnalysisContext, List<String>) -> String = { context, settings ->
            buildString {
                appendDiffPrelude(PromptComplexityLevel.COMPLEX)
                appendLine()
                appendCommitCompleteMetadata(context)
                appendLine()
                appendLine(messageService.getMessage("common.section_files_changed"))
                appendFilesWithDiffs(context.files, showDetails = true)
                appendLine()
                appendLine(messageService.getAnalysisTitle(PromptComplexityLevel.COMPLEX, "single_commit"))
                messageService.getAnalysisPoints(PromptComplexityLevel.COMPLEX, "single_commit").forEach { appendLine(it) }
                appendSelectedSettings(settings, PromptComplexityLevel.COMPLEX)
            }
        }

        override val batchTemplate: (BatchCommitAnalysisContext, List<String>, AnalysisMode) -> String =
            { batch, settings, mode ->
                buildString {
                    appendModePrelude(mode, PromptComplexityLevel.COMPLEX)
                    appendLine()
                    appendLine(messageService.getMessage("complexity.complex.final_intro_1"))
                    appendLine(
                        messageService.interpolate(
                            messageService.getMessage("complexity.complex.final_intro_2"),
                            "commitCount" to batch.commitCount
                        )
                    )
                    appendLine(messageService.getMessage("complexity.complex.final_intro_3"))
                    appendLine()
                    appendLine(messageService.getMessage("complexity.complex.final_context_global"))
                    appendLine("- ${messageService.getMessage("common.label_repo")}: ${safe(batch.repoURI)}")
                    appendLine("- ${messageService.getMessage("common.label_commits_analyzed")}: ${batch.commitCount}")
                    appendLine("- ${messageService.getMessage("common.label_from")}: ${batch.fromDate ?: "N/A"}")
                    appendLine("- ${messageService.getMessage("common.label_to")}: ${batch.toDate ?: "N/A"}")
                    appendLine("- ${messageService.getMessage("common.label_insertions")}: ${batch.totalInsertions}")
                    appendLine("- ${messageService.getMessage("common.label_deletions")}: ${batch.totalDeletions}")
                    appendLine()
                    appendLine(messageService.getMessage("complexity.complex.final_analyses_partial"))
                    batch.commits.forEachIndexed { idx, context ->
                        appendLine("### Commit ${idx + 1}/${batch.commitCount}")
                        appendCommitCompleteMetadata(context)
                        appendLine(messageService.getMessage("common.section_files_changed"))
                        appendFilesWithDiffs(context.files, showDetails = true)
                        appendLine()
                    }
                    appendLine(messageService.getMessage("complexity.complex.final_consolidated"))
                    messageService.getAnalysisPoints(PromptComplexityLevel.COMPLEX, "final").forEach { appendLine(it) }
                    appendSelectedSettings(settings, PromptComplexityLevel.COMPLEX)
                }
            }
    }



    private fun StringBuilder.appendDiffPrelude(complexity: PromptComplexityLevel) {
        appendLine(if (complexity == PromptComplexityLevel.COMPLEX) messageService.getMessage("common.technical_assistant_advanced") else messageService.getMessage("common.technical_assistant"))
        appendLine(messageService.getMessage("common.analyze_commit"))
        appendLine(if (complexity == PromptComplexityLevel.COMPLEX) messageService.getMessage("common.english_detailed") else messageService.getMessage("common.english_structured"))
        appendLine(messageService.getMessage("common.no_inventions"))
        appendLine("Use the diff as the main source of truth. Focus on exact changes, side effects, regressions, and implementation details.")
    }

    private fun StringBuilder.appendMetaPrelude(complexity: PromptComplexityLevel) {
        appendLine(if (complexity == PromptComplexityLevel.COMPLEX) messageService.getMessage("common.technical_assistant_advanced") else messageService.getMessage("common.technical_assistant"))
        appendLine(messageService.getMessage("common.analyze_metadata"))
        appendLine(if (complexity == PromptComplexityLevel.COMPLEX) messageService.getMessage("common.english_detailed") else messageService.getMessage("common.english_structured"))
        appendLine(messageService.getMessage("common.no_inventions"))
        appendLine("This is a metadata-only analysis. Focus on likely intent, scope, affected layers, impact, and risk. State clearly when diffs are needed to confirm conclusions.")
    }

    private fun StringBuilder.appendModePrelude(mode: AnalysisMode, complexity: PromptComplexityLevel) =
        when (mode) {
            AnalysisMode.DIFF -> appendDiffPrelude(complexity)
            AnalysisMode.META -> appendMetaPrelude(complexity)
        }



    private fun StringBuilder.appendCommitBasicInfo(context: CommitAnalysisContext) {
        appendLine(messageService.getMessage("common.section_commit"))
        appendLine("${messageService.getMessage("common.label_sha")}: ${safe(context.commitSha)}")
        appendLine("${messageService.getMessage("common.label_message")}: ${safe(context.message)}")
        appendLine("${messageService.getMessage("common.label_author")}: ${safe(context.author)}")
        appendLine("${messageService.getMessage("common.label_files")}: ${context.filesChanged} | +${context.totalInsertions} -${context.totalDeletions}")
    }

    private fun StringBuilder.appendCommitFullMetadata(context: CommitAnalysisContext) {
        appendLine(messageService.getMessage("common.section_metadata"))
        appendLine("- ${messageService.getMessage("common.label_repo")}: ${safe(context.repoURI)}")
        appendLine("- ${messageService.getMessage("common.label_commit_sha")}: ${safe(context.commitSha)}")
        appendLine("- ${messageService.getMessage("common.label_author")}: ${safe(context.author)}")
        appendLine("- ${messageService.getMessage("common.label_date")}: ${context.timestamp}")
        appendLine("- ${messageService.getMessage("common.label_message")}: ${safe(context.message)}")
        appendLine("- ${messageService.getMessage("common.label_files_changed")}: ${context.filesChanged}")
        appendLine("- ${messageService.getMessage("common.label_insertions")}: ${context.totalInsertions}")
        appendLine("- ${messageService.getMessage("common.label_deletions")}: ${context.totalDeletions}")
    }

    private fun StringBuilder.appendCommitCompleteMetadata(context: CommitAnalysisContext) {
        appendLine(messageService.getMessage("common.section_metadata_complete"))
        appendLine("- ${messageService.getMessage("common.label_repo")}: ${safe(context.repoURI)}")
        appendLine("- ${messageService.getMessage("common.label_commit_sha")}: ${safe(context.commitSha)}")
        appendLine("- ${messageService.getMessage("common.label_parent_sha")}: ${safe(context.parentSha ?: "N/A (root commit)")}")
        appendLine("- ${messageService.getMessage("common.label_author")}: ${safe(context.author)}")
        appendLine("- ${messageService.getMessage("common.label_date")}: ${context.timestamp}")
        appendLine("- ${messageService.getMessage("common.label_message")}: ${safe(context.message)}")
        appendLine("- ${messageService.getMessage("common.label_files_changed")}: ${context.filesChanged}")
        appendLine("- ${messageService.getMessage("common.label_insertions")}: ${context.totalInsertions}")
        appendLine("- ${messageService.getMessage("common.label_deletions")}: ${context.totalDeletions}")
    }

    private fun StringBuilder.appendCommitMetadata(
        repoURI: String, commitSha: String, author: String?, timestamp: String,
        shortMessage: String, fullMessage: String?, trailers: List<String>
    ) {
        appendLine(messageService.getMessage("common.section_metadata_commit"))
        appendLine("- ${messageService.getMessage("common.label_repo")}: ${safe(repoURI)}")
        appendLine("- ${messageService.getMessage("common.label_commit")}: ${safe(commitSha)}")
        appendLine("- ${messageService.getMessage("common.label_author")}: ${safe(author ?: "Unknown")}")
        appendLine("- ${messageService.getMessage("common.label_date")}: $timestamp")
        appendLine("- ${messageService.getMessage("common.label_message")}: ${safe(shortMessage)}")
        if (!fullMessage.isNullOrBlank()) appendLine("- ${messageService.getMessage("common.label_full_message")}: ${safe(fullMessage)}")
        if (trailers.isNotEmpty()) appendLine("- ${messageService.getMessage("common.label_trailers")}: ${trailers.joinToString(", ")}")
    }

    private fun StringBuilder.appendFilesWithDiffs(files: List<CommitFileChangeDto>, showDetails: Boolean = true) {
        if (files.isEmpty()) {
            appendLine(if (showDetails) messageService.getMessage("common.no_relevant_diff") else messageService.getMessage("common.no_changes"))
            return
        }
        files.forEachIndexed { i, f ->
            appendLine("#### File ${i + 1}: ${safe(f.newPath ?: f.oldPath ?: "N/A")} (${f.changeType})")
            if (showDetails) {
                appendLine("- ${messageService.getMessage("common.label_old_path")}: ${safe(f.oldPath ?: "N/A")}")
                appendLine("- ${messageService.getMessage("common.label_new_path")}: ${safe(f.newPath ?: "N/A")}")
                appendLine("- ${messageService.getMessage("common.label_insertions")}: ${f.insertions}, ${messageService.getMessage("common.label_deletions")}: ${f.deletions}")
                appendLine("- Patch:")
            }
            appendLine("```diff")
            appendLine(safe(f.patch))
            appendLine("```")
            if (showDetails) appendLine()
        }
    }

    private fun StringBuilder.appendFileSummaries(summaries: List<CommitFileSummary>) {
        if (summaries.isEmpty()) { appendLine(messageService.getMessage("common.no_files")); return }
        summaries.forEachIndexed { i, fs ->
            appendLine("#### File ${i + 1}: ${safe(fs.path)} (${fs.changeType})")
            appendLine("- ${messageService.getMessage("common.label_insertions")}: ${fs.insertions}, ${messageService.getMessage("common.label_deletions")}: ${fs.deletions}, Category: ${fs.category ?: "unknown"}, HotspotRank: ${fs.hotspotRank ?: "N/A"}")
            appendLine("- Rename: ${fs.isRename}")
            appendLine()
        }
    }

    private fun StringBuilder.appendSelectedSettings(settings: List<String>, complexity: PromptComplexityLevel) {
        val selected = settings.map { it.trim().uppercase() }.filter { it.isNotBlank() }.distinct().ifEmpty { listOf("DEFAULT") }
        appendLine()
        appendLine("### SELECTED ANALYSIS FOCUS")
        selected.forEach { setting ->
            val msg = messageService.getMessage("complexity.${complexity.name.lowercase()}.$setting")
            if (!msg.startsWith("ERROR:")) appendLine("- $msg")
        }
    }

    private fun safe(value: String): String = value.replace("```", "``\\`")
}
