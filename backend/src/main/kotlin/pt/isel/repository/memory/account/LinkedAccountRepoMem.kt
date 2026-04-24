package pt.isel.repository.memory.account

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import pt.isel.domain.account.LinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
@ConditionalOnProperty(prefix = "app.repository", name = ["mode"], havingValue = "memory", matchIfMissing = true)
class LinkedAccountRepoMem : ILinkedAccountRepository {
    private val idCounter = AtomicInteger(0)
    private val linkedAccounts = mutableMapOf<Int, LinkedAccount>()
    private val usersLinkedAccounts = //UserId -> Provider -> Key -> Account
        mutableMapOf<Int, MutableMap<String, MutableMap<String?, LinkedAccount>>>()

    override fun create(entity: LinkedAccount): LinkedAccount =
        entity.accountCopy(id = nextId()).also{ account ->
            linkedAccounts[account.id] = account

            val userAccounts = usersLinkedAccounts.getOrPut(account.userId){ mutableMapOf() }
            val providerAccounts = userAccounts.getOrPut(account.getType().type){ mutableMapOf() }

            providerAccounts[account.uniqueKey()] = account
        }

    override fun read(id: Int): LinkedAccount? = linkedAccounts[id]

    override fun readByUser(userId: Int) = usersLinkedAccounts[userId]?.values?.flatMap { it.values }

    override fun readByUserAndType(userId: Int, type: String) =
        usersLinkedAccounts[userId]?.get(type)?.values?.toList()

    override fun readByUserTypeAndKey(userId: Int, type: String, key: String?) =
        usersLinkedAccounts[userId]?.get(type)?.get(key)

    override fun update(entity: LinkedAccount): LinkedAccount? {
        linkedAccounts[entity.id] ?: return null

        val userAccounts = usersLinkedAccounts[entity.userId] ?: return null
        val providerAccounts = userAccounts[entity.getType().type] ?: return null

        linkedAccounts[entity.id] = entity
        providerAccounts[entity.uniqueKey()] = entity

        return entity
    }

    override fun delete(id: Int): LinkedAccount? {
        val removedAccount = linkedAccounts.remove(id) ?: return null

        val provider = removedAccount.getType().type

        val userAccounts = usersLinkedAccounts[removedAccount.userId]!!
        val providerAccounts = userAccounts[provider]!!

        providerAccounts.remove(removedAccount.uniqueKey())

        if (providerAccounts.isEmpty()) { userAccounts.remove(provider) }
        if (userAccounts.isEmpty()) { usersLinkedAccounts.remove(removedAccount.id) }

        return removedAccount
    }

    override fun deleteByUserTypeAndKey(
        userId: Int,
        type: String,
        key: String?
    ): LinkedAccount? {
        val userAccounts = usersLinkedAccounts[userId] ?: return null
        val providerAccounts = userAccounts[type] ?: return null

        val removedAccount = providerAccounts.remove(key) ?: return null
        linkedAccounts.remove(removedAccount.id)

        return removedAccount
    }

    fun nextId(): Int = idCounter.getAndIncrement()
    fun currId(): Int = idCounter.get()
}