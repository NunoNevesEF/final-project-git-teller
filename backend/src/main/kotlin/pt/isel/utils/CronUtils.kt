package pt.isel.utils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.scheduling.support.CronExpression
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

//TODO: DOCUMENT

object CronUtils{
    private const val SEC = 0

    fun calculateNext(
        cronExpression: String,
        timeZone: String,
        from: Instant = Instant.now(),
    ): Instant {
        val cron = CronExpression.parse(cronExpression)
        val zoneId = ZoneId.of(timeZone)
        val next = cron.next(ZonedDateTime.ofInstant(from, zoneId))
        requireNotNull(next) { "Could not calculate next run" }
        return next.toInstant()
        //TODO: FIX FOR INCONSISTENT MONTH SIZE (larger month to smaller) and FEB 29th (29th year x -> 28th year y)
    }

    fun calculatePrev(freqMode: FrequencyMode, timeZone: String, before: Instant): Instant {
        val zoneId = ZoneId.of(timeZone)
        val zonedNextRun = ZonedDateTime.ofInstant(before, zoneId)

        return when (freqMode) {
            is DailyMode -> zonedNextRun.minusDays(1)
            is WeeklyMode -> zonedNextRun.minusWeeks(1)
            is DaysOfWeekMode -> freqMode.daysOfWeek.map {
                zonedNextRun.with(TemporalAdjusters.previous(it))
            }.maxBy { it.toInstant() } //TODO: RECHECK LOGIC. PROBABLY FASTER WITH BINARY SEARCH
            is MonthlyModeDayOfMonth -> zonedNextRun.minusMonths(1)
            is MonthlyModeLastDay -> {
                val previousMonth = zonedNextRun.minusMonths(1)
                previousMonth.with(TemporalAdjusters.lastDayOfMonth())
            }
            is MonthlyModeDayOfNWeek -> {
                val previousMonth = zonedNextRun.minusMonths(1)
                when (freqMode.weekOrdinal) {
                    WeekOrdinal.LAST -> {
                        previousMonth.with(TemporalAdjusters.lastInMonth(freqMode.dayOfWeek))
                    }
                    else -> {
                        previousMonth.with(
                            TemporalAdjusters.dayOfWeekInMonth(
                                freqMode.weekOrdinal.value, freqMode.dayOfWeek
                            )
                        )
                    }
                }
            }
            is YearlyMode -> zonedNextRun.minusYears(1)
        }.toInstant()
    }

    fun build(input: CronInput): String{
        val minute = input.minute
        val hour = input.hour

        return when(val mode = input.mode){
            is DailyMode -> "$SEC $minute $hour * * *"

            is WeeklyMode -> "$SEC $minute $hour * * ${mode.dayOfWeek.toCron()}"

            is DaysOfWeekMode -> "$SEC $minute $hour * * ${mode.daysOfWeek.toCron()}"

            is MonthlyModeDayOfMonth -> "$SEC $minute $hour ${mode.dayOfMonth} * *"

            is MonthlyModeDayOfNWeek ->
                "$SEC $minute $hour * * ${dayOfWeekNWeekRepresentation(mode.dayOfWeek, mode.weekOrdinal)}"

            is MonthlyModeLastDay -> "$SEC $minute $hour L * *"

            is YearlyMode -> "$SEC $minute $hour ${mode.dayOfMonth} ${mode.month} *"
        }
    }

    private fun dayOfWeekNWeekRepresentation(dayOfWeek: DayOfWeek, weekOrdinal: WeekOrdinal): String{
        val day = dayOfWeek.toCron()
        return when(weekOrdinal){
            WeekOrdinal.LAST -> "${day}L"
            else ->  "${day}#${weekOrdinal.value}"
        }
    }

    private fun Set<DayOfWeek>.toCron(): String =
        this.sortedBy { it.value }.joinToString(","){ it.toCron() }

    private fun DayOfWeek.toCron(): String = this.name.take(3)
}

data class CronInput(val minute: Int, val hour: Int, val mode: FrequencyMode)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DailyMode::class, name = "DAILY"),

    JsonSubTypes.Type(value = WeeklyMode::class, name = "WEEKLY"),

    JsonSubTypes.Type(value = DaysOfWeekMode::class, name = "DAYS_OF_WEEK"),

    JsonSubTypes.Type(value = MonthlyModeDayOfMonth::class, name = "MONTHLY_DAY_OF_MONTH"),

    JsonSubTypes.Type(value = MonthlyModeDayOfNWeek::class, name = "MONTHLY_DAY_OF_N_WEEK"),

    JsonSubTypes.Type(value = MonthlyModeLastDay::class, name = "MONTHLY_LAST_DAY"),

    JsonSubTypes.Type(value = YearlyMode::class, name = "YEARLY")
)
sealed interface FrequencyMode

data object DailyMode : FrequencyMode

data class WeeklyMode(val dayOfWeek: DayOfWeek) : FrequencyMode

data class DaysOfWeekMode(val daysOfWeek: Set<DayOfWeek>) : FrequencyMode

sealed interface MonthlyMode: FrequencyMode
data class MonthlyModeDayOfMonth(val dayOfMonth: Int) : MonthlyMode
data class MonthlyModeDayOfNWeek(val dayOfWeek: DayOfWeek, val weekOrdinal: WeekOrdinal) : MonthlyMode
object MonthlyModeLastDay: MonthlyMode
enum class WeekOrdinal(val value: Int){
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    LAST(-1)
}

data class YearlyMode(val dayOfMonth: Int, val month: Int) : FrequencyMode

