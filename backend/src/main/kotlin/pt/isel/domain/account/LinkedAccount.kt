package pt.isel.domain.account

sealed class LinkedAccount(val id: Int, val userId: Int) {
    init{
        require(id >= 0) { "id must be >= 0" }
        require(userId >= 0) { "userId must be >= 0" }
    }
    abstract fun getType(): String
    abstract fun accountCopy(id: Int): LinkedAccount
}

data class OAuthLinkedAccount(
    private val _id: Int, private val _userId: Int, val provider: String
) : LinkedAccount(_id, _userId) {
    init{
        require(!provider.isBlank()) { "provider cannot be blank" }
    }

    companion object {
        fun create(id: Int = 0, userId: Int, provider: String) =
            OAuthLinkedAccount(id, userId, provider)
    }

    override fun getType(): String = provider
    override fun accountCopy(id: Int) = copy(_id = id)
}

data class FormLinkedAccount(
    private val _id: Int, private val _userId: Int, val passwordHash: String
) : LinkedAccount(_id, _userId) {
    init{
        //TODO: CONSIDER MORE THINGS LIKE SIZE CHECK
        require(!passwordHash.isBlank()) { "password hash cannot be blank" }
    }
    companion object{
        fun create(id: Int = 0, userId: Int, passwordHash: String) =
            FormLinkedAccount(id, userId, passwordHash)
        fun getType() = "form"
    }

    override fun getType(): String = "form"
    override fun accountCopy(id: Int) = copy(_id = id)
}