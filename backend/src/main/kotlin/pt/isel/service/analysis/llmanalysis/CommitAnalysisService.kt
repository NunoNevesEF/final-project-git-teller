package pt.isel.service.analysis.llmanalysis

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.isel.domain.DateInterval
import pt.isel.domain.report.BatchCommitAnalysisContext
import pt.isel.domain.report.BatchCommitAnalysisResponse
import pt.isel.domain.report.CommitAnalysisContext
import pt.isel.domain.report.CommitAnalysisRequest
import pt.isel.domain.report.CommitAnalysisResponse
import pt.isel.domain.report.CommitDetailedSettingsAnalysisRequest
import pt.isel.domain.report.CommitFileChangeDto
import pt.isel.domain.report.CommitShasAnalysisRequest
import pt.isel.domain.report.GitCommunication
import pt.isel.model.AnalysisMode
import pt.isel.model.CommitFileSummary
import pt.isel.model.PromptComplexityLevel
import pt.isel.domain.report.GitAnalysis
import pt.isel.service.analysis.OpenRouterLlmService
import pt.isel.service.analysis.llmanalysis.prompt.PromptBuilderService
import pt.isel.service.analysis.llmanalysis.util.AnalysisLimits
import pt.isel.service.analysis.llmanalysis.util.DiffExtractionService
import pt.isel.service.analysis.llmanalysis.util.EnumParser
import pt.isel.service.analysis.llmanalysis.util.FilePathAnalyzer
import java.time.Instant

//TODO: DOCUMENT

@Service
class CommitAnalysisService(
    private val llmService: OpenRouterLlmService,
    private val commitFetcherService: CommitFetcherService,
    private val commitFileFilteringService: CommitFileFilteringService,
    private val diffExtractionService: DiffExtractionService,
    private val commitContextBuilderService: CommitContextBuilderService,
    private val promptBuilderService: PromptBuilderService,
) {
    private val logger = LoggerFactory.getLogger(CommitAnalysisService::class.java)



    fun analyzeCommit(request: CommitAnalysisRequest): CommitAnalysisResponse {
        val limits = AnalysisLimits.Companion.fromSingle(request)
        val complexity = EnumParser.parseComplexityLevel(request.promptComplexity)
        val mode = EnumParser.parseAnalysisMode(request.analysisMode)
        val gitComm = GitCommunication.openExisting(request.repoURI)
        val repo = gitComm.git.repository
        val maxFiles = commitFileFilteringService.adaptiveMaxFiles(commitCount = 1, mode = mode)  // <--

        logger.info("=== STARTING SINGLE COMMIT ANALYSIS === Commit: ${request.commitSha} | Mode: $mode | Complexity: $complexity | MaxFiles: $maxFiles")

        val (commit, parent) = commitFetcherService.readCommitAndParent(repo, request.commitSha)
        val (selectedFiles, _) = commitFileFilteringService.filterAndRankFiles(gitComm, repo, commit, parent, limits, maxFiles)  // <--

        val (context, prompt) = when (mode) {
            AnalysisMode.DIFF -> {
                val files = selectedFiles.toDiffDtos(repo, limits)
                val ctx = commitContextBuilderService.buildCommitContext(request.repoURI, commit, parent, files)
                ctx to promptBuilderService.buildSingleCommitPrompt(ctx, complexity, request.requestedAnalyses)
            }
            AnalysisMode.META -> {
                val summaries = selectedFiles.toSummaries()
                val ctx = commitContextBuilderService.buildCommitContext(request.repoURI, commit, parent, summaries.toMetaDtos())
                ctx to promptBuilderService.buildSingleCommitPromptMetadata(
                    repoURI = request.repoURI, commitSha = commit.id.name,
                    author = commit.authorIdent?.name, timestamp = commit.toTimestamp(),
                    shortMessage = commit.shortMessage, fullMessage = commit.fullMessage,
                    fileSummaries = summaries, trailers = FilePathAnalyzer.extractTrailers(commit.fullMessage),
                    complexity = complexity
                )
            }
        }

        logger.info("Prompt length ($mode): ${prompt.length} chars")
        return CommitAnalysisResponse(context, llmService.askText(prompt))
    }

    fun analyzeCommitsByShas(request: CommitShasAnalysisRequest, repoURI: String): BatchCommitAnalysisResponse {
        require(request.commitShas.isNotEmpty()) { "Commit SHAs list cannot be empty." }
        val gitComm = GitCommunication.openExisting(repoURI)

        val missing = gitComm.getMissingCommitShas(request.commitShas)
        if (missing.isNotEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Commits not found: ${missing.joinToString(", ")}")

        return runBatchAnalysis(
            repoURI = repoURI,
            gitComm = gitComm,
            commits = commitFetcherService.getCommitsByShas(gitComm, request.commitShas),
            limits = AnalysisLimits.Companion.fromBatch( request.maxCharsPerFile),
            complexity = EnumParser.parseComplexityLevel(request.promptComplexity),
            mode = EnumParser.parseAnalysisMode(request.analysisMode),
            requestedAnalyses = request.requestedAnalyses
        )
    }

    fun analyzeCommitsDetailedSettings(request: CommitDetailedSettingsAnalysisRequest, repoURI: String, dateFilter: DateInterval?): BatchCommitAnalysisResponse {
        val gitComm = GitCommunication.openExisting(repoURI)
        val commits = commitFetcherService.getCommitsBetweenDatesOrAll(gitComm,
            dateFilter, request.maxCommits)

        if (commits.isEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No commits found between ${dateFilter?.beginDate} and ${dateFilter?.endDate}.")

        return runBatchAnalysis(
            repoURI = repoURI,
            gitComm = gitComm,
            commits = commits,
            limits = AnalysisLimits.Companion.fromBatch(request.maxCharsPerFile),
            complexity = EnumParser.parseComplexityLevel(request.promptComplexity),
            mode = EnumParser.parseAnalysisMode(request.analysisMode),
            requestedAnalyses = request.requestedAnalyses,
            fromDate = dateFilter?.beginDate,
            toDate = dateFilter?.endDate,
        )
    }



    private fun runBatchAnalysis(
        repoURI: String,
        gitComm: GitCommunication,
        commits: List<RevCommit>,
        limits: AnalysisLimits,
        complexity: PromptComplexityLevel,
        mode: AnalysisMode,
        requestedAnalyses: List<String> = listOf("DEFAULT"),
        fromDate: Instant? = null,
        toDate: Instant? = null,
    ): BatchCommitAnalysisResponse {
        val repo = gitComm.git.repository
        val maxFilesPerCommit = commitFileFilteringService.adaptiveMaxFiles(commitCount = commits.size, mode = mode)

        logger.info("=== STARTING BATCH ANALYSIS === Commits: ${commits.size} | Mode: $mode | MaxFiles: $maxFilesPerCommit | Complexity: $complexity")

        val contexts = mutableListOf<CommitAnalysisContext>()

        commits.forEachIndexed { idx, commit ->
            val parent = commitFetcherService.resolveParent(repo, commit)
            logger.info("--- Building context ${idx + 1}/${commits.size}: ${commit.id.name.take(8)} | ${commit.shortMessage} ---")

            val (selectedFiles, _) = commitFileFilteringService.filterAndRankFiles(gitComm, repo, commit, parent, limits, maxFilesPerCommit)

            val context = when (mode) {
                AnalysisMode.DIFF -> {
                    val files = selectedFiles.toDiffDtos(repo, limits)
                    logger.info("Selected files (DIFF): ${files.size}")
                    commitContextBuilderService.buildCommitContext(repoURI, commit, parent, files)
                }
                AnalysisMode.META -> {
                    val summaries = selectedFiles.toSummaries()
                    logger.info("Selected files (META): ${summaries.size}")
                    commitContextBuilderService.buildCommitContext(repoURI, commit, parent, summaries.toMetaDtos())
                }
            }
            contexts.add(context)
        }

        val batchContext = BatchCommitAnalysisContext(
            repoURI = repoURI, fromDate = fromDate, toDate = toDate,
            commitCount = contexts.size,
            totalInsertions = contexts.sumOf { it.totalInsertions },
            totalDeletions = contexts.sumOf { it.totalDeletions },
            commits = contexts
        )

        val prompt = promptBuilderService.buildBatchPrompt(batchContext, complexity, requestedAnalyses, mode)
        logger.info("=== BATCH PROMPT === Length: ${prompt.length} chars | Commits included: ${contexts.size}")

        val result = llmService.askText(prompt)
        logger.info("=== BATCH ANALYSIS FINISHED ===")
        return BatchCommitAnalysisResponse(batchContext, result)
    }

    // --- Helpers ---

    private fun RevCommit.toTimestamp(): String =
        Instant.ofEpochSecond(commitTime.toLong()).toString()

    private fun List<FileCandidate>.toDiffDtos(repo: Repository, limits: AnalysisLimits): List<CommitFileChangeDto> =
        map {
            CommitFileChangeDto(
                it.oldPath, it.newPath, it.changeType, it.insertions, it.deletions,
                diffExtractionService.extractPatch(repo, it.entry, limits.maxCharsPerFile)
            )
        }

    private fun List<FileCandidate>.toSummaries(): List<CommitFileSummary> =
        map { CommitFileSummary(
            path = it.newPath ?: it.oldPath ?: "N/A",
            changeType = it.changeType,
            insertions = it.insertions,
            deletions = it.deletions,
            isRename = it.changeType.equals("RENAME", ignoreCase = true),
            category = FilePathAnalyzer.inferCategoryFromPath(it.newPath ?: it.oldPath),
            hotspotRank = null
        ) }

    private fun List<CommitFileSummary>.toMetaDtos(): List<CommitFileChangeDto> =
        map { CommitFileChangeDto(null, it.path, it.changeType, it.insertions, it.deletions, "[omitted]") }

    fun analyzeGitOverview(gitAnalysis: GitAnalysis): BatchCommitAnalysisResponse {
        val prompt = buildOverviewPrompt(gitAnalysis)
        logger.info("=== STARTING OVERVIEW ANALYSIS === Prompt length: ${prompt.length} chars")
        val result = llmService.askText(prompt)
        logger.info("=== OVERVIEW ANALYSIS FINISHED ===")
        val totalCommits = gitAnalysis.commitsByUser.values.sumOf { it.size }
        val totalInsertions = gitAnalysis.commitsByUser.values.flatten().sumOf { it.additions }
        val totalDeletions = gitAnalysis.commitsByUser.values.flatten().sumOf { it.deletions }
        return BatchCommitAnalysisResponse(
            context = BatchCommitAnalysisContext(
                repoURI = "",
                fromDate = gitAnalysis.firstCommitTime,
                toDate = gitAnalysis.lastCommitTime,
                commitCount = totalCommits,
                totalInsertions = totalInsertions,
                totalDeletions = totalDeletions,
                commits = emptyList()
            ),
            llmAnalysis = result
        )
    }

    private fun buildOverviewPrompt(gitAnalysis: GitAnalysis): String = buildString {
        appendLine("You are a technical assistant. Analyze this Git repository overview and provide a concise, structured summary.")
        appendLine("Answer in English. Be factual. Do not invent information not present in the data.")
        appendLine()
        appendLine("## Repository Time Span")
        appendLine("- First commit: ${gitAnalysis.firstCommitTime}")
        appendLine("- Last commit:  ${gitAnalysis.lastCommitTime}")
        appendLine()
        appendLine("## Commits by Contributor")
        gitAnalysis.commitsByUser.entries.sortedByDescending { it.value.size }.forEach { (user, commits) ->
            val additions = commits.sumOf { it.additions }
            val deletions = commits.sumOf { it.deletions }
            appendLine("- $user: ${commits.size} commits (+$additions / -$deletions lines)")
        }
        appendLine()
        val files = gitAnalysis.mostModifiedFiles
        if (!files.isNullOrEmpty()) {
            appendLine("## Most Modified Files")
            files.forEachIndexed { i, file ->
                appendLine("${i + 1}. ${file.path} — ${file.changes} changes")
            }
            appendLine()
        }
        appendLine("## Provide")
        appendLine("1. A brief summary of the project activity and scope")
        appendLine("2. Key contributors and their relative contribution")
        appendLine("3. Most active areas of the codebase")
        appendLine("4. Overall project health and collaboration assessment")
    }


}
