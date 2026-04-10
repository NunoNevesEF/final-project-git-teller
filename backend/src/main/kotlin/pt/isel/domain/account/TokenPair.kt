package pt.isel.domain.account

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)