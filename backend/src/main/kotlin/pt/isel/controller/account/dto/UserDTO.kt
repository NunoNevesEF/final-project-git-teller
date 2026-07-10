package pt.isel.controller.account.dto

import pt.isel.entity.account.User

/**
 * `UserDTO`
 *
 * Represents the user information returned by controller endpoints.
 *
 * This DTO defines the user data exposed in API responses.
 *
 * @property id the unique identifier of the user.
 * @property email the user's unique email address.
 * @property username the username associated with the user.
 */
data class UserDTO(val id: Int, val email: String, val username: String){
    /**
     * Creates a [UserDTO] from a [User] entity.
     *
     * @param user the [User] entity to convert.
     * @return a [UserDTO] containing the user information exposed by the API.
     */
    constructor(
        user: User,
    ) : this(user.id, user.email, user.username ?: "")
}