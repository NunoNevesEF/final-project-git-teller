package pt.isel.service

import org.springframework.stereotype.Service
import pt.isel.domain.UserReportDto
import pt.isel.entity.UserReport
import pt.isel.repository.IUserReportRepository
import pt.isel.repository.IUserRepository

@Service
class UserReportService(
    private val userReportRepo: IUserReportRepository,
    private val userRepo: IUserRepository,
    ) {
    fun create(userId: Int, pdf: ByteArray) {
        val user = requireNotNull(userRepo.findById(userId))

        val report = UserReport(
            user = user,
            data = pdf
        )

        userReportRepo.create(report)
    }

    fun getByUserId(userId: Int): List<UserReportDto> {
        return userReportRepo.findByUserId(userId).map { report ->
            UserReportDto(
                id = report.id,
                createdAt = report.createdAt
            )
        }
    }

    fun getReportPdf(reportId: Int,userId: Int): ByteArray {
        val report = userReportRepo.findByIdAndUserId(reportId, userId)
        require(report != null) { "Report $report not found" }
        return report.data
    }
}