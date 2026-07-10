package pt.isel.gitteller.repository.memory.account

import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.gitteller.repository.interfaces.account.FormLinkedAccountRepoTest
import pt.isel.repository.interfaces.account.IFormLinkedAccountRepository
import pt.isel.repository.interfaces.account.IUserRepository
import pt.isel.repository.memory.account.UserRepoMem
import pt.isel.repository.memory.account.FormLinkedAccountRepoMem

class FormLinkedAccountRepoMemTest: FormLinkedAccountRepoTest() {
    override fun repo(): IFormLinkedAccountRepository = FormLinkedAccountRepoMem()

    override fun userRepo(): IUserRepository = UserRepoMem()
}