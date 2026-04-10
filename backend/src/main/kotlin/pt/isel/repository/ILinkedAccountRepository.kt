package pt.isel.repository

import pt.isel.domain.account.LinkedAccount

interface ILinkedAccountRepository : IRepository<LinkedAccount>{
    fun readByUser(userId: Int): List<LinkedAccount>?
    fun readByUserAndType(userId: Int, type: String): LinkedAccount?
    fun deleteByUserAndType(userId: Int, type: String): LinkedAccount?
}