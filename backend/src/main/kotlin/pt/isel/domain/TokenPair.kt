package pt.isel.domain

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)