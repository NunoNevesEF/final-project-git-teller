package pt.isel.model.account

import pt.isel.domain.account.OAuthLinkedAccount

data class OAuthLinkedAccountListItemDTO(
    val id: Int,
    val providerId: String, //TODO: ADD ACCOUNT USERNAME TO LINKED ACCOUNT AND RETURN THAT INSTEAD
){
    companion object{
        fun create(oAuthLinkedAccount: OAuthLinkedAccount) =
            OAuthLinkedAccountListItemDTO(oAuthLinkedAccount.id, oAuthLinkedAccount.providerId)
    }
}
