package pt.isel.controller.account.dto

import pt.isel.entity.account.OAuthLinkedAccount

/**
 * `OAuthLinkedAccountListItemDTO`
 *
 * Represents the minimal data used to represent a oauth account list item in the frontend.
 *
 *
 * @property id the unique identifier of this [OAuthLinkedAccount]. Used so that the client can choose an account in a request.
 * @property providerId used to label the [OAuthLinkedAccount] in the frontend. TODO: REPLACE WITH A PROVIDER ACCOUNT USERNAME
 *
 */
data class OAuthLinkedAccountListItemDTO(
    val id: Int,
    val providerId: String,
){
    /**
     * Creates a [OAuthLinkedAccountListItemDTO] from a [OAuthLinkedAccount] entity.
     *
     * @param oAuthLinkedAccount the [OAuthLinkedAccount] entity to convert.
     * @return a [OAuthLinkedAccountListItemDTO] containing the minimal [OAuthLinkedAccount] information to be exposed by the API for a list view.
     */
    constructor(
        oAuthLinkedAccount: OAuthLinkedAccount,
    ) : this(oAuthLinkedAccount.id, oAuthLinkedAccount.providerId)
}