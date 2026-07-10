package pt.isel.gitteller.repository.memory.report

import pt.isel.gitteller.repository.interfaces.report.ReportRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.interfaces.report.IReportRepository
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.repository.memory.report.ReportRepoMem

class ReportRepoMemTest: ReportRepoTest(){
    override fun repo(): IReportRepository = ReportRepoMem()

    override fun userRepo(): IUserRepository = UserRepoMem()
}