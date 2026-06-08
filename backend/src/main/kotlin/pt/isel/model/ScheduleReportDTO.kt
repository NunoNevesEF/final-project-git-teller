package pt.isel.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.domain.schedule.PeriodicScheduledReport
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.ScheduledReportEntity
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
sealed interface CreateScheduleReportDTO<T: ScheduledReport<T, *>>{
    val repoURI: String
    fun toDomain(userId: Int): ScheduledReport<T,*>
}

data class CreateOneTimeScheduledReportDTO(
    override val repoURI: String,
    val dataStart: Instant,
    val runAt: Instant,
): CreateScheduleReportDTO<OneTimeScheduledReport>{
    override fun toDomain(userId: Int) =
        OneTimeScheduledReport.create(
            userId = userId, repoURI = repoURI, nextRun = runAt, dataStart = dataStart
        )
}

data class CreatePeriodicScheduledReportDTO(
    override val repoURI: String,
    val timeZone: String,
    val time: LocalTime,
    val freqMode: FrequencyMode,
): CreateScheduleReportDTO<PeriodicScheduledReport>{
    override fun toDomain(userId: Int) =
        PeriodicScheduledReport.create(
            userId = userId, repoURI = repoURI,
            timeZone = timeZone, cronInput = CronInput(time.minute, time.hour, freqMode)
        )
}

