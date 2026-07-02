package pt.isel.domain

import java.time.Instant

data class DateInterval(
    val beginDate: Instant,
    val endDate: Instant
)