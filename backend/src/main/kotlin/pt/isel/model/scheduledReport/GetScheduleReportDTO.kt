package pt.isel.model.scheduledReport

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import pt.isel.domain.schedule.OneTimeScheduledReport
import pt.isel.entity.schedule.OneTimeScheduledReportEntity
import pt.isel.entity.schedule.PeriodicScheduledReportEntity
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = GetOneTimeScheduledReportDTO::class,
        name = "ONE_TIME"
    ),
    JsonSubTypes.Type(
        value = GetPeriodicScheduledReportDTO::class,
        name = "PERIODIC"
    )
)
sealed interface GetScheduledReportDTO {
    val id: Int
    val repoUri: String
    val nextRunAt: Instant?
    val lastRunAt: Instant?
    val dataFrom: Instant
    val isCancelled: Boolean
    val cancellationReason: String?
}

data class GetOneTimeScheduledReportDTO(
    override val id: Int,
    override val repoUri: String,
    override val nextRunAt: Instant?,
    override val lastRunAt: Instant?,
    override val dataFrom: Instant,
    override val isCancelled: Boolean,
    override val cancellationReason: String?,
) : GetScheduledReportDTO

data class GetPeriodicScheduledReportDTO(
    override val id: Int,
    override val repoUri: String,
    override val nextRunAt: Instant?,
    override val lastRunAt: Instant?,
    override val dataFrom: Instant,
    override val isCancelled: Boolean,
    override val cancellationReason: String?,

    val active: Boolean,
    val timeZone: String,
) : GetScheduledReportDTO