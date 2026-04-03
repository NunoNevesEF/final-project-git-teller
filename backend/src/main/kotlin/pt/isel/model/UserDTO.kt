package pt.isel.model

import pt.isel.domain.User

data class UserDTO(val id: Int, val email: String, val userName: String){
    companion object {
        fun create(user: User) = UserDTO(user.data.id, user.data.email, user.data.userName)
    }
}