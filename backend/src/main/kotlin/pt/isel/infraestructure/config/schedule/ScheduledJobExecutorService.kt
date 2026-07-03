package pt.isel.infraestructure.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.DateInterval
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.service.ScheduledReportService
import pt.isel.service.report.FailureDoNotRetry
import pt.isel.service.report.GitAnalysisService
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

            val request = GitAnalysisRequest(
                repoURI = repoUri,
                llmRequest = llmConfig,
                dateFilter = DateInterval(pendingJob.dataFrom, Instant.now()),
                gitAccountId = null
            )

            when (val analysisResult = analysisService.analyze(request,null)) {
//                is Success -> {
//                    val llmAnalysis = if (llmConfig != null) {
//                        try {
//                            if (llmConfig.flag) {
//                                commitAnalysisService.analyzeGitOverview(analysisResult.right).llmAnalysis
//                            } else {
//                                require(llmConfig.byDetailedSettings != null)
//                                val request = CommitDetailledSettingsAnalysisRequest(
//                                    promptComplexity = llmConfig.byDetailedSettings.promptComplexity,
//                                    analysisMode = llmConfig.byDetailedSettings.analysisMode,
//                                    requestedAnalyses = llmConfig.byDetailedSettings.requestedAnalyses,
//                                )
//                                commitAnalysisService.analyzeCommitsDetailedSettings(request, repoUri, null).llmAnalysis
//                            }
//                        } catch (e: Exception) {""}
//                    } else { ""}
//
//                    val gitAnalysis = analysisResult.right.copy(llmAnalysis = llmAnalysis)
//
//                    val successJob = scheduledReportService.endJob(runningJob, true) as? SuccessfulJob
//                        ?: throw IllegalStateException("what")
//                    scheduledReportService.updateReportLastRun(successJob.scheduledReportId, successJob.startedAt)
//                    reportService.createReport(gitAnalysis, repoUri, userId)
//                }
                is Success -> {
                    val gitAnalysis = analysisResult.right

                    val successJob = scheduledReportService.endJob(runningJob, true) as? SuccessfulJob
                        ?: throw IllegalStateException()

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
