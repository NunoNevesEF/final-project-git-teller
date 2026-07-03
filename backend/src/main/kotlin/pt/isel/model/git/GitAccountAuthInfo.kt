package pt.isel.model.git

import pt.isel.domain.account.OAuthAccountProvider

data class GitAccountAuthInfo(val accessToken: String, val provider: OAuthAccountProvider)
