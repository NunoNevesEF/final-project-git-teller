package pt.isel.domain

import java.time.Instant

//TODO: DOCUMENT

data class DateInterval(
    val beginDate: Instant,
    val endDate: Instant
)