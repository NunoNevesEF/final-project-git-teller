package pt.isel.model

import pt.isel.domain.account.FormLinkedAccount
import pt.isel.domain.account.LinkedAccount
import pt.isel.domain.account.OAuthLinkedAccount

sealed class LinkedAccountDTO(
    val id: Int,
    val userId: Int,
){
    companion object {
        fun create(linkedAccount: LinkedAccount): LinkedAccountDTO {
            return when(linkedAccount) {
                is FormLinkedAccount -> FormLinkedAccountDTO.create(linkedAccount)
                is OAuthLinkedAccount -> OAuthLinkedAccountDTO.create(linkedAccount)
            }
        }
    }
}

data class OAuthLinkedAccountDTO(
    private val _id: Int,
    private val _userId: Int,
    val provider: String
): LinkedAccountDTO(_id, _userId){
    companion object{
        fun create(oAuthLinkedAccount: OAuthLinkedAccount) = OAuthLinkedAccountDTO(
            oAuthLinkedAccount.id,
            oAuthLinkedAccount.userId,
            oAuthLinkedAccount.provider
        )
    }
}

data class FormLinkedAccountDTO(
    private val _id: Int,
    private val _userId: Int,
): LinkedAccountDTO(_id, _userId){
    companion object{
        fun create(formLinkedAccount: FormLinkedAccount) = FormLinkedAccountDTO(
            formLinkedAccount.id,
            formLinkedAccount.userId,
        )
    }
}
