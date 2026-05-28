package pt.isel.service.llmanalysis

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.isel.domain.*
import pt.isel.model.AnalysisMode
import pt.isel.model.CommitFileSummary
import pt.isel.model.PromptComplexityLevel
import pt.isel.service.OpenRouterLlmService
import pt.isel.service.llmanalysis.prompt.PromptBuilderService
import pt.isel.service.llmanalysis.util.*
import java.time.Instant

@Service
class CommitAnalysisOrchestrator(
    private val llmService: OpenRouterLlmService,
    private val commitFetcherService: CommitFetcherService,
    private val commitFileFilteringService: CommitFileFilteringService,
    private val diffExtractionService: DiffExtractionService,
    private val commitContextBuilderService: CommitContextBuilderService,
    private val promptBuilderService: PromptBuilderService,
) {
    private val logger = LoggerFactory.getLogger(CommitAnalysisOrchestrator::class.java)



    fun analyzeCommit(request: CommitAnalysisRequest): CommitAnalysisResponse {
        val limits = AnalysisLimits.fromSingle(request)
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

    fun analyzeCommitsByShas(request: CommitShasAnalysisRequest): BatchCommitAnalysisResponse {
        require(request.commitShas.isNotEmpty()) { "Commit SHAs list cannot be empty." }
        val gitComm = GitCommunication.openExisting(request.repoURI)

        val missing = gitComm.getMissingCommitShas(request.commitShas)
        if (missing.isNotEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Commits not found: ${missing.joinToString(", ")}")

        return runBatchAnalysis(
            repoURI = request.repoURI,
            gitComm = gitComm,
            commits = commitFetcherService.getCommitsByShas(gitComm, request.commitShas),
            limits = AnalysisLimits.fromBatch( request.maxCharsPerFile),
            complexity = EnumParser.parseComplexityLevel(request.promptComplexity),
            mode = EnumParser.parseAnalysisMode(request.analysisMode),
            requestedAnalyses = request.requestedAnalyses
        )
    }

    fun analyzeCommitsBetweenDates(request: CommitDateRangeAnalysisRequest): BatchCommitAnalysisResponse {
        if (request.fromDate.isAfter(request.toDate))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.")

        val gitComm = GitCommunication.openExisting(request.repoURI)
        val commits = commitFetcherService.getCommitsBetweenDates(gitComm, request.fromDate, request.toDate, request.maxCommits)

        if (commits.isEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No commits found between ${request.fromDate} and ${request.toDate}.")

        return runBatchAnalysis(
            repoURI = request.repoURI,
            gitComm = gitComm,
            commits = commits,
            limits = AnalysisLimits.fromBatch(request.maxCharsPerFile),
            complexity = EnumParser.parseComplexityLevel(request.promptComplexity),
            mode = EnumParser.parseAnalysisMode(request.analysisMode),
            requestedAnalyses = request.requestedAnalyses,
            fromDate = request.fromDate,
            toDate = request.toDate
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
        map { CommitFileChangeDto(it.oldPath, it.newPath, it.changeType, it.insertions, it.deletions,
            diffExtractionService.extractPatch(repo, it.entry, limits.maxCharsPerFile)) }

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
}
