package pt.isel.repository.memory.account

import org.springframework.stereotype.Repository
import pt.isel.domain.account.LinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
class LinkedAccountRepoMem : ILinkedAccountRepository {
    private val idCounter = AtomicInteger(0)
    private val linkedAccounts = mutableMapOf<Int, LinkedAccount>()
    private val usersLinkedAccounts = mutableMapOf<Int, MutableMap<String, LinkedAccount>>()

    override fun create(entity: LinkedAccount): LinkedAccount =
        entity.accountCopy(id = nextId()).also{ account ->
            val userAccounts = usersLinkedAccounts.getOrPut(account.userId){ mutableMapOf() }

            require(!userAccounts.containsKey(account.getType())) {
                "User already has an account of type ${account.getType()}"
            }

            linkedAccounts[account.id] = account
            userAccounts[account.getType()] = account
        }

    override fun read(id: Int): LinkedAccount? = linkedAccounts[id]

    override fun readByUser(userId: Int) = usersLinkedAccounts[userId]?.values?.toList()

    override fun readByUserAndType(userId: Int, type: String) = usersLinkedAccounts[userId]?.get(type)

    override fun update(entity: LinkedAccount): LinkedAccount? {
        val oldAccount = linkedAccounts[entity.id] ?: return null

        require(oldAccount.userId == entity.userId) { "User must own the account" }
        require(oldAccount.getType() == entity.getType()){ "Account provider cannot be replaced" }

        linkedAccounts[entity.id] = entity
        usersLinkedAccounts[entity.userId]!![entity.getType()] = entity

        return entity
    }

    override fun delete(id: Int): LinkedAccount? {
        val removedAccount = linkedAccounts.remove(id) ?: return null

        val userAccounts = usersLinkedAccounts[removedAccount.userId]!!
        userAccounts.remove(removedAccount.getType())

        if (userAccounts.isEmpty()) { usersLinkedAccounts.remove(removedAccount.userId) }

        return removedAccount
    }

    override fun deleteByUserAndType(userId: Int, type: String): LinkedAccount? {
        val userAccounts = usersLinkedAccounts[userId] ?: return null
        val removedAccount = userAccounts.remove(type) ?: return null

        linkedAccounts.remove(removedAccount.id)

        return removedAccount
    }

    fun nextId(): Int = idCounter.getAndIncrement()
    fun currId(): Int = idCounter.get()
}