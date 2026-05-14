package pt.isel.domain

import java.time.Instant

data class UserReportDto(
    val id: Int,
    val createdAt: Instant,
)