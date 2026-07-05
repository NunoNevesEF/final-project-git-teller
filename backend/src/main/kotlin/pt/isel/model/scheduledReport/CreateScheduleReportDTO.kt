package pt.isel.model.scheduledReport

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import pt.isel.domain.report.AnalysisRequestWrapper
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.utils.CronInput
import pt.isel.utils.FrequencyMode
import java.time.Instant
import java.time.LocalTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = CreateOneTimeScheduledReportDTO::class,
        name = "ONE_TIME"
    ),
    JsonSubTypes.Type(
        value = CreatePeriodicScheduledReportDTO::class,
        name = "PERIODIC"
    )
)
sealed interface CreateScheduleReportDTO{
    val repoUri: String
    val gitAccountId: Int?
    fun toDomain(userId: Int): ScheduledReport
}

data class CreateOneTimeScheduledReportDTO(
    override val repoUri: String,
    val dataStart: Instant,
    val runAt: Instant,
    val llmConfig: AnalysisRequestWrapper? = null,
    override val gitAccountId: Int?,
): CreateScheduleReportDTO{
    override fun toDomain(userId: Int) =
        OneTimeScheduledReport.create(
            userId = userId, repoURI = repoUri, nextRun = runAt,
            dataStart = dataStart, llmConfig = llmConfig
        )
}

data class CreatePeriodicScheduledReportDTO(
    override val repoUri: String,
    val timeZone: String,
    val time: LocalTime,
    val freqMode: FrequencyMode,
    val llmConfig: AnalysisRequestWrapper? = null,
    override val gitAccountId: Int?,
): CreateScheduleReportDTO{
    override fun toDomain(userId: Int) =
        PeriodicScheduledReport.create(
            userId = userId, repoURI = repoUri,
            timeZone = timeZone, cronInput = CronInput(time.minute, time.hour, freqMode),
            llmConfig = llmConfig
        )
}
