package pt.isel.model

import pt.isel.domain.account.User

data class UserDTO(val id: Int, val email: String, val userName: String){
    companion object {
        fun create(user: User) = UserDTO(user.id, user.email, user.userName)
    }
}