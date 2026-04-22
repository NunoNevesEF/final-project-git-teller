package pt.isel.repository

import pt.isel.domain.account.LinkedAccount

interface ILinkedAccountRepository : IRepository<LinkedAccount>{
    fun readByUser(userId: Int): List<LinkedAccount>?
    fun readByUserAndType(userId: Int, type: String): List<LinkedAccount>?
    fun readByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount?
    fun deleteByUserTypeAndKey(userId: Int, type: String, key: String?): LinkedAccount?
}