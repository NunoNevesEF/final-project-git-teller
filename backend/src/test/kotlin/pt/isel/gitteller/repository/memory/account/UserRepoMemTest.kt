package pt.isel.gitteller.repository.memory.account

import pt.isel.entity.account.model.Role
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.account.UserRepoTest
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.memory.account.UserRepoMem

class UserRepoMemTest: UserRepoTest(){
    override fun repo(): IUserRepository = UserRepoMem()
}