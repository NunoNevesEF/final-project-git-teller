package pt.isel.model.report

import java.time.Instant

data class UserReportDTO(
    val id: Int,
    val createdAt: Instant,
    val repoURI: String,
)