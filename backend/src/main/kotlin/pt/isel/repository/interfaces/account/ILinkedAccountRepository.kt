package pt.isel.repository.interfaces.account

import pt.isel.domain.account.LinkedAccount
import pt.isel.repository.interfaces.IRepository

interface ILinkedAccountRepository : IRepository<LinkedAccount> {
    fun readByUser(userId: Int): List<LinkedAccount>?
    fun readByUserAndType(userId: Int, type: String): List<LinkedAccount>?
    fun readByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount?
    fun deleteByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount?
    fun readByUserIdAndId(id: Int, userId: Int): LinkedAccount?
}