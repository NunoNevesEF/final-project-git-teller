package pt.isel.security.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.CommitDateRangeAnalysisRequest
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.service.ScheduledReportService
import pt.isel.service.git.FailureDoNotRetry
import pt.isel.service.git.GitAnalysisService
import pt.isel.service.llmanalysis.CommitAnalysisService
import pt.isel.service.report.UserReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import java.time.Instant

@Service
class ScheduledJobExecutor(
    private val taskScheduler: ThreadPoolTaskScheduler,
    private val scheduledReportService: ScheduledReportService,
    private val analysisService: GitAnalysisService,
    private val reportService: UserReportService,
    private val commitAnalysisService: CommitAnalysisService,
) {
    fun schedule(job: PendingJob, repoUri: String, userId: Int) {
        taskScheduler.schedule({ execute(job, repoUri, userId) }, job.runAt)
    }

    private fun execute(pendingJob: PendingJob, repoUri: String, userId: Int) {
        try {
            val runningJob = scheduledReportService.runJob(pendingJob)
            val llmConfig = scheduledReportService.getScheduleLlmConfig(pendingJob.scheduledReportId)

            when (val analysisResult = analysisService.createAnalysis(repoUri)) {
                is Success -> {
                    val llmAnalysis = if (llmConfig != null) {
                        try {
                            if (llmConfig.overviewOnly) {
                                commitAnalysisService.analyzeGitOverview(analysisResult.right).llmAnalysis
                            } else {
                                val request = CommitDateRangeAnalysisRequest(
                                    repoURI = repoUri,
                                    fromDate = pendingJob.dataFrom,
                                    toDate = Instant.now(),
                                    promptComplexity = llmConfig.promptComplexity,
                                    analysisMode = llmConfig.analysisMode,
                                    requestedAnalyses = llmConfig.requestedAnalyses
                                )
                                commitAnalysisService.analyzeCommitsBetweenDates(request).llmAnalysis
                            }
                        } catch (e: Exception) {""}
                    } else { ""}

                    val gitAnalysis = analysisResult.right.copy(llmAnalysis = llmAnalysis)

                    val successJob = scheduledReportService.endJob(runningJob, true) as? SuccessfulJob
                        ?: throw IllegalStateException("what")
                    scheduledReportService.updateReportLastRun(successJob.scheduledReportId, successJob.startedAt)
                    reportService.createReport(gitAnalysis, repoUri, userId)
                }

                is Failure -> {
                    when (val error = analysisResult.left) {
                        is FailureDoNotRetry -> {
                            val failedJob = scheduledReportService.endJob(
                                runningJob, false, error.err.name, false
                            ) as? FailedJob ?: throw IllegalStateException("what")
                            scheduledReportService.cancelReport(failedJob.scheduledReportId, error.err.name)
                            scheduledReportService.updateReportLastRun(failedJob.scheduledReportId, failedJob.startedAt)
                        }
                        else -> {
                            when (val failedJob = scheduledReportService.endJob(runningJob, false, allowRetry = true)) {
                                is FailedJob -> scheduledReportService.updateReportLastRun(failedJob.scheduledReportId, failedJob.startedAt)
                                is PendingJob -> schedule(failedJob, repoUri, userId)
                                else -> throw IllegalStateException("what")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
        //TODO: IMPROVE CODE. WE KNOW WHEN TO REPEAT VS NOT SO CONDITIONAL END JOB IS ONLY CAUSING UNECESSARY CASTING
    }
}

@Configuration
class SchedulerConfig {
    @Bean
    fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            poolSize = 5
            setWaitForTasksToCompleteOnShutdown(true)
            initialize()
        }
    }
}
