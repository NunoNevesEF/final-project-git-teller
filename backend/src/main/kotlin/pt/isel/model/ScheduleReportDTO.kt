package pt.isel.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
    val userId: Int
    val repoURI: String
}

data class CreateOneTimeScheduledReportDTO(
    override val userId: Int,
    override val repoURI: String,
    val dataStart: Instant,
    val runAt: Instant,
): CreateScheduleReportDTO

data class CreatePeriodicScheduledReportDTO(
    override val userId: Int,
    override val repoURI: String,

    val timeZone: String,
    val time: LocalTime,

    val freqMode: FrequencyMode,
): CreateScheduleReportDTO

