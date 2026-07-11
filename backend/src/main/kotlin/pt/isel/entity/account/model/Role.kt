package pt.isel.entity.account.model

/**
 *  `Role`
 *
 * Represents an [pt.isel.entity.account.User]'s role within the application
 *
 * @property [USER] the default role for any [pt.isel.entity.account.User] registered in the application
 * @property [ADMIN] the role that grants admin only permissions
 * */
enum class Role {
    USER,
    ADMIN
}