package pt.isel.gitteller.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.utils.CronInput
import pt.isel.utils.CronUtils
import pt.isel.utils.DailyMode
import pt.isel.utils.DaysOfWeekMode
import pt.isel.utils.FrequencyMode
import pt.isel.utils.MonthlyModeDayOfMonth
import pt.isel.utils.MonthlyModeDayOfNWeek
import pt.isel.utils.MonthlyModeLastDay
import pt.isel.utils.WeekOrdinal
import pt.isel.utils.WeeklyMode
import pt.isel.utils.YearlyMode
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Stream

class CronUtilsTest {
    companion object {
        const val MIN = 30
        const val HOUR = 12
        const val DOM = 1
        const val MONTH = 3
        const val TIMEZONE = "UTC"
        const val YEAR = 2026

        @JvmStatic
        fun buildCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CronInput(minute = MIN, hour = HOUR, mode = DailyMode),
                "0 $MIN $HOUR * * *"
            ), Arguments.of(
                CronInput(minute = MIN, hour = HOUR, mode = WeeklyMode(DayOfWeek.MONDAY)),
                "0 $MIN $HOUR * * MON"
            ), Arguments.of(
                CronInput(
                    minute = MIN, hour = HOUR, mode = DaysOfWeekMode(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                ), "0 $MIN $HOUR * * WED,FRI"
            ), Arguments.of(
                CronInput(minute = MIN, hour = HOUR, mode = MonthlyModeDayOfMonth(DOM)),
                "0 $MIN $HOUR $DOM * *"
            ), Arguments.of(
                CronInput(
                    minute = MIN, hour = HOUR, mode = MonthlyModeDayOfNWeek(DayOfWeek.THURSDAY, WeekOrdinal.THIRD)
                ),
                "0 $MIN $HOUR * * THU#3"
            ), Arguments.of(
                CronInput(
                    minute = MIN, hour = HOUR, mode = MonthlyModeDayOfNWeek(DayOfWeek.SATURDAY, WeekOrdinal.LAST)
                ),
                "0 $MIN $HOUR * * SATL"
            ), Arguments.of(
                CronInput(minute = MIN, hour = HOUR, mode = MonthlyModeLastDay),
                "0 $MIN $HOUR L * *"
            ),
            Arguments.of(
                CronInput(minute = MIN, hour = HOUR, mode = YearlyMode(DOM, MONTH)),
                "0 $MIN $HOUR $DOM $MONTH *"
            )
        )

        @JvmStatic
        fun calculateNextCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "0 $MIN 12 * * *",
                createZonedDateTimeInstant(hour  = 10),
                createZonedDateTimeInstant(hour = 12),
            ),
            Arguments.of(
                "0 $MIN $HOUR 13 * *",
                createZonedDateTimeInstant(dom = 14, month = 5),
                createZonedDateTimeInstant(dom = 13, month = 6),
            ),
            Arguments.of(
                "0 $MIN $HOUR 31 * *",
                createZonedDateTimeInstant(dom = 31, month = 5),
                createZonedDateTimeInstant(dom = 30, month = 6),
            ),
            Arguments.of(
                "0 $MIN $HOUR * * WED,FRI",
                createZonedDateTimeInstant(dom = 13, month = 5),
                createZonedDateTimeInstant(dom = 15, month = 5),
            ),
            Arguments.of(
                "0 $MIN $HOUR * * WED,FRI",
                createZonedDateTimeInstant(dom = 15, month = 5),
                createZonedDateTimeInstant(dom = 20, month = 5),
            ),
            Arguments.of(
                "0 $MIN $HOUR L * * ",
                createZonedDateTimeInstant(dom = 15, month = 5),
                createZonedDateTimeInstant(dom = 31, month = 5),
            ),
            Arguments.of(
                "0 $MIN $HOUR L * * ",
                createZonedDateTimeInstant(year = 2024, dom = 15, month = 2),
                createZonedDateTimeInstant(year = 2024, dom = 29, month = 2),
            ),
            Arguments.of(
                "0 $MIN $HOUR 13 5 *",
                createZonedDateTimeInstant(year = 2026, dom = 13, month = 5),
                createZonedDateTimeInstant(year = 2027, dom = 13, month = 5),
            ),
            Arguments.of(
                "0 $MIN $HOUR 29 2 *",
                createZonedDateTimeInstant(year = 2024, dom = 29, month = 2),
                createZonedDateTimeInstant(year = 2025, dom = 28, month = 2),
            )
        )

        @JvmStatic
        fun calculatePrevCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                DailyMode,
                createZonedDateTimeInstant(dom = 13),
                createZonedDateTimeInstant(dom = 12),
            ),
            Arguments.of(
                WeeklyMode(DayOfWeek.WEDNESDAY),
                createZonedDateTimeInstant(dom = 13),
                createZonedDateTimeInstant(dom = 6),
            ),
            Arguments.of(
                DaysOfWeekMode(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                createZonedDateTimeInstant(year = 2026, dom = 13, month = 5),
                createZonedDateTimeInstant(year = 2026, dom = 8, month = 5),
            ),
            Arguments.of(
                MonthlyModeDayOfMonth(13),
                createZonedDateTimeInstant(dom = 13, month = 5),
                createZonedDateTimeInstant(dom = 13, month = 4),
            ),
            Arguments.of(
                MonthlyModeDayOfMonth(13),
                createZonedDateTimeInstant(dom = 31, month = 5),
                createZonedDateTimeInstant(dom = 30, month = 4),
            ),
            Arguments.of(
                MonthlyModeLastDay,
                createZonedDateTimeInstant(month = 5),
                createZonedDateTimeInstant(dom = 30, month = 4),
            ),
            Arguments.of(
                MonthlyModeLastDay,
                createZonedDateTimeInstant(year = 2024, month = 3),
                createZonedDateTimeInstant(year = 2024, dom = 29, month = 2),
            ),
            Arguments.of(
                MonthlyModeDayOfNWeek(DayOfWeek.FRIDAY, WeekOrdinal.LAST),
                createZonedDateTimeInstant(year = 2026, month = 5),
                createZonedDateTimeInstant(year = 2026, month = 4, dom = 24),
            ),
            Arguments.of(
                MonthlyModeDayOfNWeek(DayOfWeek.WEDNESDAY, WeekOrdinal.SECOND),
                createZonedDateTimeInstant(year = 2026, month = 5),
                createZonedDateTimeInstant(year = 2026, month = 4, dom = 8),
            ),
            Arguments.of(
                YearlyMode(13, 5),
                createZonedDateTimeInstant(year = 2026, month = 5, dom = 13),
                createZonedDateTimeInstant(year = 2025, month = 5, dom = 13),
            ),
            Arguments.of(
                YearlyMode(29, 2),
                createZonedDateTimeInstant(year = 2024, month = 2, dom = 29),
                createZonedDateTimeInstant(year = 2023, month = 2, dom = 28),
            )
        )

        private fun createZonedDateTimeInstant(year: Int = YEAR, month: Int = MONTH, dom: Int = DOM, hour: Int = HOUR) =
            ZonedDateTime.of(
                year, month, dom,
                hour, MIN, 0, 0,
                ZoneId.of(TIMEZONE)
            ).toInstant()
    }

    @ParameterizedTest
    @MethodSource("buildCases")
    fun `build should generate correct cron expression`(
        input: CronInput, expected: String
    ) {
        val result = CronUtils.build(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("calculateNextCases")
    fun `calculateNext should calculate next run correctly`(
        cron: String, from: Instant, expected: Instant
    ) {
        val result = CronUtils.calculateNext(
            cronExpression = cron, timeZone = "UTC", from = from
        )
        //Currently failing for inconsistent month sizes (month to month or year to year for Feb case)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("calculatePrevCases")
    fun `calculatePrev should calculate previous run correctly`(
        mode: FrequencyMode, before: Instant, expected: Instant
    ) {
        val result = CronUtils.calculatePrev(
            freqMode = mode, timeZone = "UTC", before = before
        )

        assertEquals(expected, result)
    }
}
