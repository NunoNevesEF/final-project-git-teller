package pt.isel.gitteller.repository.memory.account

import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.account.OAuthLinkedAccountRepoTest
import pt.isel.repository.interfaces.account.IOAuthLinkedAccountRepository
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.repository.memory.account.OAuthLinkedAccountRepoMem

class OAuthLinkedAccountRepoMemTest: OAuthLinkedAccountRepoTest() {
    override fun repo(): IOAuthLinkedAccountRepository = OAuthLinkedAccountRepoMem()
    override fun userRepo(): IUserRepository = UserRepoMem()
}