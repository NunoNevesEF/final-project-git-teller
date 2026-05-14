package pt.isel.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.isel.entity.UserReport

@Repository
interface UserReportRepoJpa : JpaRepository<UserReport, Int> {
    fun findByUserId(userId: Int): List<UserReport>
    fun findByIdAndUserId(id: Int, userId: Int): UserReport?
}