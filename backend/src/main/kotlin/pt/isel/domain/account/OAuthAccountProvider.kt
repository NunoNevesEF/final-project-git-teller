package pt.isel.domain.account

enum class OAuthAccountProvider(val type: String, val max: Int?){
    GOOGLE("google",1),
    GITHUB("github",null),
    GITLAB("gitlab",null),
    UNDEFINED("undefined", 0);

    companion object {
        fun fromString(type: String): OAuthAccountProvider =
            when (type.lowercase()) {
                GOOGLE.type -> GOOGLE
                GITHUB.type -> GITHUB
                GITLAB.type -> GITLAB
                else -> throw IllegalArgumentException("Invalid account type: $type")
            }

        val gitAccounts = listOf(GITHUB, GITLAB)
    }
}