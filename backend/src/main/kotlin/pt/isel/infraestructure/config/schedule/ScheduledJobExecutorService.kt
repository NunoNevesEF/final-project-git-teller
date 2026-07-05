package pt.isel.infraestructure.config.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.DateInterval
import pt.isel.domain.schedule.FailedJob
import pt.isel.domain.schedule.PendingJob
import pt.isel.domain.schedule.SuccessfulJob
import pt.isel.service.ScheduledReportService
import pt.isel.service.account.LinkedAccountService
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
    private val linkedAccountService: LinkedAccountService,
    private val commitAnalysisService: CommitAnalysisService,
) {
    fun schedule(job: PendingJob, userId: Int, repoUri: String, gitAccountId: Int?) {
        taskScheduler.schedule({ execute(job, repoUri, userId, gitAccountId) }, job.runAt)
    }

    private fun execute(pendingJob: PendingJob, repoUri: String, userId: Int, gitAccountId: Int?) {
        try {
            val runningJob = scheduledReportService.runJob(pendingJob)
            val llmConfig = scheduledReportService.getScheduleLlmConfig(pendingJob.scheduledReportId)

            val accountResult =
                if(gitAccountId != null) linkedAccountService.findUserOAuthAccount(gitAccountId, userId)
                else null

            val token = //TODO: REPLACE WITH SUCCESS -> TOKEN / FAILURE -> INSTANT END / NULL -> NULL
                if(accountResult is Success) accountResult.right.accessToken
                else null

            val request = GitAnalysisRequest(
                repoURI = repoUri,
                llmRequest = llmConfig,
                dateFilter = DateInterval(runningJob.dataFrom, runningJob.dataTo),
                gitAccountId = gitAccountId
            )

            when (val analysisResult = analysisService.analyze(request, token)) {
                is Success -> {
                    val gitAnalysis = analysisResult.right

                    val successJob = scheduledReportService.endJob(runningJob, true) as? SuccessfulJob
                        ?: throw IllegalStateException()

                    scheduledReportService.updateReportLastRun(successJob.scheduledReportId, successJob.startedAt)
                    reportService.createReport(gitAnalysis, userId)
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
                                is PendingJob -> schedule(failedJob, userId, repoUri, gitAccountId)
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
