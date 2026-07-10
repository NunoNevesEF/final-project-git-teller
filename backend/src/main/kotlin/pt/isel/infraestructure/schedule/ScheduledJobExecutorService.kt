package pt.isel.infraestructure.schedule

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import pt.isel.domain.report.GitAnalysisRequest
import pt.isel.domain.DateInterval
import pt.isel.model.report.GitAnalysis
import pt.isel.domain.report.schedule.FailedJob
import pt.isel.domain.report.schedule.PendingJob
import pt.isel.domain.report.schedule.RunningJob
import pt.isel.service.report.ScheduledReportService
import pt.isel.service.account.LinkedAccountService
import pt.isel.infraestructure.schedule.model.FailureDoNotRetry
import pt.isel.infraestructure.schedule.model.FailureDoRetry
import pt.isel.infraestructure.schedule.model.ScheduledReportResult
import pt.isel.infraestructure.schedule.model.toScheduledReportResult
import pt.isel.service.analysis.GitAnalysisService
import pt.isel.service.error.LinkedAccountServiceError
import pt.isel.service.report.ReportOrchestrator
import pt.isel.utils.Either
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.utils.map
import pt.isel.utils.onFailure
import pt.isel.utils.rightOrNull
import pt.isel.utils.success

//TODO: DOCUMENT

@Service
class ScheduledJobExecutor(
    private val taskScheduler: ThreadPoolTaskScheduler,
    private val scheduledReportService: ScheduledReportService,
    private val analysisService: GitAnalysisService,
    private val reportOrchestrator: ReportOrchestrator,
    private val linkedAccountService: LinkedAccountService,
) {
    fun schedule(job: PendingJob, userId: Int, repoUri: String, gitAccountId: Int?) {
        taskScheduler.schedule({ execute(job, repoUri, userId, gitAccountId) }, job.runAt)
    }

    private fun execute(pendingJob: PendingJob, repoUri: String, userId: Int, gitAccountId: Int?) {
        try {
            val runningJob = scheduledReportService.runJob(pendingJob)
            val llmConfig = scheduledReportService.getScheduleLlmConfig(pendingJob.scheduledReportId)

            val tokenResult = resolveAccessToken(runningJob, gitAccountId, userId)
                .onFailure { return }
            val token = tokenResult.rightOrNull()!!

            val request = GitAnalysisRequest(
                repoURI = repoUri,
                llmRequest = llmConfig,
                dateFilter = DateInterval(runningJob.dataFrom, runningJob.dataTo),
                gitAccountId = gitAccountId
            )

            when (val analysisResult = analysisService.analyze(request, token)) {
                is Success -> handleSuccess(runningJob, analysisResult.right, userId)
                is Failure -> handleFailure(
                    runningJob,
                    analysisResult.left.toScheduledReportResult(),
                    userId,
                    repoUri,
                    gitAccountId
                )
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun handleSuccess(
        runningJob: RunningJob,
        analysis: GitAnalysis,
        userId: Int
    ) {
        val success = scheduledReportService.endJobSuccess(runningJob)

        scheduledReportService.updateReportLastRun(
            success.scheduledReportId,
            success.startedAt
        )

        reportOrchestrator.createReport(analysis, userId)
    }

    private fun handleFailure(
        runningJob: RunningJob,
        error: ScheduledReportResult,
        userId: Int,
        repoUri: String,
        gitAccountId: Int?
    ) {
        when (error) {
            is FailureDoNotRetry -> handlePermanentFailure(runningJob, error)

            is FailureDoRetry -> handleRetryableFailure(
                runningJob,
                error.errorMsg,
                userId,
                repoUri,
                gitAccountId
            )
        }
    }

    private fun handlePermanentFailure(
        runningJob: RunningJob,
        error: FailureDoNotRetry
    ) {
        val failed = scheduledReportService.endJobFailure(
            runningJob,
            error.errorMsg
        )

        scheduledReportService.cancelReport(
            failed.scheduledReportId,
            error.errorMsg
        )

        scheduledReportService.updateReportLastRun(
            failed.scheduledReportId,
            failed.startedAt
        )
    }

    private fun handleRetryableFailure(
        runningJob: RunningJob,
        errorMsg: String,
        userId: Int,
        repoUri: String,
        gitAccountId: Int?
    ) {
        when (val job = scheduledReportService.failOrRetryJob(runningJob, errorMsg)) {
            is FailedJob ->
                scheduledReportService.updateReportLastRun(
                    job.scheduledReportId,
                    job.startedAt
                )

            is PendingJob ->
                schedule(job, userId, repoUri, gitAccountId)

            else ->
                error("Unexpected job state")
        }
    }

    private fun resolveAccessToken(
        runningJob: RunningJob,
        gitAccountId: Int?,
        userId: Int
    ): Either<LinkedAccountServiceError, String?> {
        if (gitAccountId == null) return success(null)

        val result = linkedAccountService.findUserGitAccount(gitAccountId, userId)

        return result
            .map{it.accessToken}
            .onFailure {
                val errorMsg = "Could Not Find Requested Git Account"

                val failed = scheduledReportService.endJobFailure(
                    runningJob,
                    errorMsg
                )

                scheduledReportService.cancelReport(
                    failed.scheduledReportId,
                    errorMsg
                )

                scheduledReportService.updateReportLastRun(
                    failed.scheduledReportId,
                    failed.startedAt
                )
            }
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
