package pt.isel.controller.report.dto

import pt.isel.controller.account.dto.OAuthLinkedAccountListItemDTO
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.report.Report
import java.time.Instant

/**
 * `ReportListItemDTO`
 *
 * Represents the minimal data used to represent a report list item in the frontend.
 *
 *
 * @property id the unique identifier of this [OAuthLinkedAccount]. Used so that the client can choose an account in a request.
 * @property createdAt when the report was created.
 * @property repoUri the uri of the repository whose data was used to generate this report.
 */
data class ReportListItemDTO(
    val id: Int,
    val createdAt: Instant,
    val repoUri: String,
){
    /**
     * Creates a [ReportListItemDTO] from a [Report] entity.
     *
     * @param report the [Report] entity to convert.
     * @return a [ReportListItemDTO] containing the minimal [Report] information to be exposed by the API for a list view.
     */
    constructor(
        report: Report,
    ) : this(
        report.id,
        report.createdAt,
        report.gitAnalysis.searchInfo.repositoryUrl
    )
}