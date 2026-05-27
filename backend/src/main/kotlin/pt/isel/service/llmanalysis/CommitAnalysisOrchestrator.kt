package pt.isel.service.llmanalysis

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import pt.isel.domain.BatchCommitAnalysisContext
import pt.isel.domain.BatchCommitAnalysisResponse
import pt.isel.domain.CommitAnalysisContext
import pt.isel.domain.CommitAnalysisRequest
import pt.isel.domain.CommitAnalysisResponse
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.CommitFileChangeDto
import pt.isel.domain.CommitShasAnalysisRequest
import pt.isel.domain.GitCommunication
import pt.isel.model.AnalysisMode
import pt.isel.model.CommitFileSummary
import pt.isel.model.PromptComplexityLevel
import pt.isel.service.OpenAiLlmService
import pt.isel.service.llmanalysis.prompt.PromptBuilderService
import pt.isel.service.llmanalysis.util.AnalysisLimits
import pt.isel.service.llmanalysis.util.DiffExtractionService
import pt.isel.service.llmanalysis.util.EnumParser
import pt.isel.service.llmanalysis.util.FilePathAnalyzer
import java.time.Instant
import kotlin.math.max

@Service
class CommitAnalysisOrchestrator(
    private val llmService: OpenAiLlmService,
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

        logger.info("=== STARTING SINGLE COMMIT ANALYSIS ===")
        logger.info("Commit: ${request.commitSha}")
        logger.info("Mode: $mode, Complexity: $complexity")

        val (commit, parent) = commitFetcherService.readCommitAndParent(repo, request.commitSha)

        val (selectedFiles, _) = commitFileFilteringService.filterAndRankFiles(
            gitCommunication = gitComm,
            repository = repo,
            commit = commit,
            parent = parent,
            limits = limits,
            maxRelevantFiles = limits.maxFiles
        )

        return when (mode) {
            AnalysisMode.DIFF -> {
                val files = selectedFiles.map { candidate ->
                    CommitFileChangeDto(
                        oldPath = candidate.oldPath,
                        newPath = candidate.newPath,
                        changeType = candidate.changeType,
                        insertions = candidate.insertions,
                        deletions = candidate.deletions,
                        patch = diffExtractionService.extractPatch(repo, candidate.entry, limits.maxCharsPerFile),

                    )
                }

                val context = commitContextBuilderService.buildCommitContext(request.repoURI, commit, parent, files)
                val prompt = promptBuilderService.buildSingleCommitPrompt(
                    context = context,
                    complexity = complexity,
                    settings = request.requestedAnalyses
                )
                logger.info("Prompt length for LLM (DIFF): ${prompt.length} chars")
                logger.debug("Generated prompt:\n$prompt")
                val analysis = llmService.ask(prompt)
                logger.debug("Generated Response:\n$analysis")

                CommitAnalysisResponse(context = context, llmAnalysis = extractLlmText(analysis))
            }

            AnalysisMode.META -> {
                val fileSummaries = selectedFiles.map { candidate ->
                    CommitFileSummary(
                        path = candidate.newPath ?: candidate.oldPath ?: "N/A",
                        changeType = candidate.changeType,
                        insertions = candidate.insertions,
                        deletions = candidate.deletions,
                        isRename = candidate.changeType.equals("RENAME", ignoreCase = true),
                        category = FilePathAnalyzer.inferCategoryFromPath(candidate.newPath ?: candidate.oldPath),
                        hotspotRank = null
                    )
                }

                val timestamp = Instant.ofEpochSecond(commit.commitTime.toLong()).toString()
                val commitPrompt = promptBuilderService.buildSingleCommitPromptMetadata(
                    repoURI = request.repoURI,
                    commitSha = commit.id.name,
                    author = commit.authorIdent?.name,
                    timestamp = timestamp,
                    shortMessage = commit.shortMessage,
                    fullMessage = commit.fullMessage,
                    fileSummaries = fileSummaries,
                    trailers = FilePathAnalyzer.extractTrailers(commit.fullMessage),
                    complexity = complexity,
                    settings = request.requestedAnalyses
                )
                logger.info("Prompt length for LLM (META): ${commitPrompt.length} chars")
                logger.debug("Generated prompt:\n$commitPrompt")
                val analysis = llmService.ask(commitPrompt)
                logger.debug("Generated Response:\n$analysis")

                val filesForContext = fileSummaries.map { fs ->
                    CommitFileChangeDto(
                        oldPath = null,
                        newPath = fs.path,
                        changeType = fs.changeType,
                        insertions = fs.insertions,
                        deletions = fs.deletions,
                        patch = "[omitted]",

                    )
                }

                val context =
                    commitContextBuilderService.buildCommitContext(request.repoURI, commit, parent, filesForContext)
                CommitAnalysisResponse(context = context, llmAnalysis = extractLlmText(analysis))
            }
        }
    }


    fun analyzeCommitsByShas(request: CommitShasAnalysisRequest): BatchCommitAnalysisResponse {
        require(request.commitShas.isNotEmpty()) { "Commit SHAs list cannot be empty." }

        val limits = AnalysisLimits.fromBatch(request.maxFilesPerCommit, request.maxCharsPerFile)
        val complexity = EnumParser.parseComplexityLevel(request.promptComplexity)
        val mode = EnumParser.parseAnalysisMode(request.analysisMode)
        val gitComm = GitCommunication.openExisting(request.repoURI)
        val repo = gitComm.git.repository

        logger.info("Mode: $mode, Complexity: $complexity")

        val missing = gitComm.getMissingCommitShas(request.commitShas)
        if (missing.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Commits not found: ${missing.joinToString(", ")}")
        }

        val commits = commitFetcherService.getCommitsByShas(gitComm, request.commitShas)

        val res = analyzeCommitsInConversation(
            repoURI = request.repoURI,
            gitComm = gitComm,
            repo = repo,
            commits = commits,
            limits = limits,
            fromDate = null,
            toDate = null,
            complexity = complexity,
            mode = mode,
            requestedAnalyses = request.requestedAnalyses
        )

        return BatchCommitAnalysisResponse(res.context, extractLlmText(res.llmAnalysis))
    }

    fun analyzeCommitsBetweenDates(request: CommitDateRangeAnalysisRequest): BatchCommitAnalysisResponse {
        if (request.fromDate.isAfter(request.toDate)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate.")
        }

        val limits = AnalysisLimits.fromBatch(request.maxFilesPerCommit, request.maxCharsPerFile)
        val complexity = EnumParser.parseComplexityLevel(request.promptComplexity)
        val mode = EnumParser.parseAnalysisMode(request.analysisMode)
        val gitComm = GitCommunication.openExisting(request.repoURI)
        val repo = gitComm.git.repository

        logger.info("Mode: $mode, Complexity: $complexity")

        val commits =
            commitFetcherService.getCommitsBetweenDates(gitComm, request.fromDate, request.toDate, request.maxCommits)

        if (commits.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No commits found between ${request.fromDate} and ${request.toDate}."
            )
        }

        val res = analyzeCommitsInConversation(
            repoURI = request.repoURI,
            gitComm = gitComm,
            repo = repo,
            commits = commits,
            limits = limits,
            fromDate = request.fromDate,
            toDate = request.toDate,
            complexity = complexity,
            mode = mode,
            requestedAnalyses = request.requestedAnalyses
        )

        return BatchCommitAnalysisResponse(res.context, extractLlmText(res.llmAnalysis))
    }

    private fun analyzeCommitsInConversation(
        repoURI: String,
        gitComm: GitCommunication,
        repo: Repository,
        commits: List<RevCommit>,
        limits: AnalysisLimits,
        fromDate: Instant?,
        toDate: Instant?,
        complexity: PromptComplexityLevel,
        mode: AnalysisMode = AnalysisMode.DIFF,
        requestedAnalyses: List<String> = listOf("DEFAULT")
    ): BatchCommitAnalysisResponse {
        val maxRelevantFilesPerCommit = max(3, limits.maxFiles)

        logger.info("=== STARTING BATCH ANALYSIS ===")
        logger.info("Total commits to analyze: ${commits.size}")
        logger.info("Mode: $mode")
        logger.info("Max relevant files per commit: $maxRelevantFilesPerCommit")
        logger.info("Max chars per file: ${limits.maxCharsPerFile}")
        logger.info("Prompt complexity: $complexity")
        logger.info("Chronological order (oldest first):")
        commits.forEachIndexed { idx, commit ->
            val timestamp = Instant.ofEpochSecond(commit.commitTime.toLong())
            logger.info("  ${idx + 1}. ${commit.id.name.take(8)} | $timestamp | ${commit.shortMessage}")
        }

        val partialAnalyses = mutableListOf<String>()
        val contexts = mutableListOf<CommitAnalysisContext>()

        commits.forEachIndexed { idx, commit ->
            val parent = commitFetcherService.resolveParent(repo, commit)

            logger.info("\n--- Processing commit ${idx + 1}/${commits.size}: ${commit.id.name.take(8)} ---")
            logger.info("Message: ${commit.shortMessage}")
            logger.info("Date: ${Instant.ofEpochSecond(commit.commitTime.toLong())}")

            val (selectedFiles, _) = commitFileFilteringService.filterAndRankFiles(
                gitCommunication = gitComm,
                repository = repo,
                commit = commit,
                parent = parent,
                limits = limits,
                maxRelevantFiles = maxRelevantFilesPerCommit
            )

            when (mode) {
                AnalysisMode.DIFF -> {
                    val files = selectedFiles.map { candidate ->
                        CommitFileChangeDto(
                            oldPath = candidate.oldPath,
                            newPath = candidate.newPath,
                            changeType = candidate.changeType,
                            insertions = candidate.insertions,
                            deletions = candidate.deletions,
                            patch = diffExtractionService.extractPatch(repo, candidate.entry, limits.maxCharsPerFile),

                        )
                    }

                    logger.info("Selected files (DIFF): ${files.size}")
                    files.forEach { f ->
                        logger.info("  ✓ ${f.newPath ?: f.oldPath ?: "N/A"} | +${f.insertions} -${f.deletions} | ${f.patch.length} chars")
                    }

                    val context = commitContextBuilderService.buildCommitContext(repoURI, commit, parent, files)

                    val perCommitPrompt = promptBuilderService.buildPerCommitPrompt(
                        context = context,
                        index = idx,
                        total = commits.size,
                        previousAnalyses = partialAnalyses,
                        complexity = complexity,
                        settings = requestedAnalyses,
                        mode = mode
                    )

                    logger.info("Prompt length for LLM: ${perCommitPrompt.length} chars")
                    logger.debug("Per-commit prompt:\n$perCommitPrompt")

                    val perCommitAnalysis = llmService.ask(perCommitPrompt)
                    partialAnalyses.add(perCommitAnalysis)
                    contexts.add(context)

                    logger.info("LLM analysis received: ${perCommitAnalysis.length} chars")
                }

                AnalysisMode.META -> {
                    val fileSummaries = selectedFiles.map { candidate ->
                        CommitFileSummary(
                            path = candidate.newPath ?: candidate.oldPath ?: "N/A",
                            changeType = candidate.changeType,
                            insertions = candidate.insertions,
                            deletions = candidate.deletions,
                            isRename = candidate.changeType.equals("RENAME", ignoreCase = true),
                            category = FilePathAnalyzer.inferCategoryFromPath(candidate.newPath ?: candidate.oldPath),
                            hotspotRank = null
                        )
                    }

                    logger.info("Selected files (META): ${fileSummaries.size}")

                    val timestamp = Instant.ofEpochSecond(commit.commitTime.toLong()).toString()
                    val perCommitPrompt = promptBuilderService.buildSingleCommitPromptMetadata(
                        repoURI = repoURI,
                        commitSha = commit.id.name,
                        author = commit.authorIdent?.name,
                        timestamp = timestamp,
                        shortMessage = commit.shortMessage,
                        fullMessage = commit.fullMessage,
                        fileSummaries = fileSummaries,
                        trailers = FilePathAnalyzer.extractTrailers(commit.fullMessage),
                        complexity = complexity,
                        settings = requestedAnalyses
                    )

                    logger.info("Prompt length for LLM: ${perCommitPrompt.length} chars")
                    logger.debug("Generated prompt:\n$perCommitPrompt")
                    val perCommitAnalysis = llmService.ask(perCommitPrompt)
                    logger.debug("Generated Response:\n$perCommitAnalysis")
                    partialAnalyses.add(perCommitAnalysis)

                    val filesForContext = fileSummaries.map { fs ->
                        CommitFileChangeDto(
                            oldPath = null,
                            newPath = fs.path,
                            changeType = fs.changeType,
                            insertions = fs.insertions,
                            deletions = fs.deletions,
                            patch = "[omitted]",

                        )
                    }

                    val context =
                        commitContextBuilderService.buildCommitContext(repoURI, commit, parent, filesForContext)
                    contexts.add(context)

                    logger.info("LLM analysis received: ${perCommitAnalysis.length} chars")
                }
            }
        }

        val batchContext = BatchCommitAnalysisContext(
            repoURI = repoURI,
            fromDate = fromDate,
            toDate = toDate,
            commitCount = contexts.size,
            totalInsertions = contexts.sumOf { it.totalInsertions },
            totalDeletions = contexts.sumOf { it.totalDeletions },
            commits = contexts
        )

        val finalPrompt = promptBuilderService.buildFinalConsolidatedPrompt(
            batchContext = batchContext,
            partialAnalyses = partialAnalyses,
            complexity = complexity,
            settings = requestedAnalyses,
            mode = mode
        )
        logger.info("\n=== FINAL PROMPT ===")
        logger.info("Total final prompt length: ${finalPrompt.length} chars")
        logger.info("Number of partial analyses included: ${partialAnalyses.size}")
        logger.debug("Generated prompt:\n$finalPrompt")
        val finalAnalysis = llmService.ask(finalPrompt)
        logger.debug("Generated Response:\n$finalAnalysis")

        logger.info("Final analysis received: ${finalAnalysis.length} chars")
        logger.info("=== BATCH ANALYSIS FINISHED ===\n")

        return BatchCommitAnalysisResponse(context = batchContext, llmAnalysis = extractLlmText(finalAnalysis))
    }

    fun extractLlmText(llmAnalysis: String): String {
        val regex = Regex("""text=(.*?), type=output_text""", RegexOption.DOT_MATCHES_ALL)

        return regex.findAll(llmAnalysis)
            .lastOrNull()
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?: "could not filter"
    }
}