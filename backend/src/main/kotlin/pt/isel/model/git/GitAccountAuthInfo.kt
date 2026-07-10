package pt.isel.model.git

import pt.isel.domain.account.OAuthProvider

//TODO: DOCUMENT

data class GitAccountAuthInfo(val accessToken: String, val provider: OAuthProvider)
