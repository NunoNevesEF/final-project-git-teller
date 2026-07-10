package pt.isel.service.account

import org.springframework.stereotype.Service
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.service.error.ServiceError
import pt.isel.service.error.InvalidEmail
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.InvalidProvider
import pt.isel.service.error.InvalidProviderID
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.service.account.model.CreatedNewUserAccount
import pt.isel.service.account.model.LinkedNewAccount
import pt.isel.service.account.model.LoggedIntoUserAccount
import pt.isel.service.account.model.SignUpResult
import pt.isel.utils.Either
import pt.isel.utils.Success
import pt.isel.utils.failure
import pt.isel.utils.flatMap
import pt.isel.utils.map
import pt.isel.utils.onFailure
import pt.isel.utils.rightOrNull
import pt.isel.utils.success

/**
 *  `AccountOrchestrator`
 *
 * The service layer orchestrator that manages sequenced actions of [userService] and [linkedAccountService]
 *
 * @property [userService] The service layer user-related operations.
 * @property [linkedAccountService] The service layer linked-account-related operations
 * */
@Service
class AccountOrchestrator(
    private val userService: UserService,
    private val linkedAccountService: LinkedAccountService
) {
    /**TODO: CURRENT METHOD IS UNSAFE. ANYONE CAN LINK A NEW FORM ACCOUNT TO ANYBODY'S REGISTERED EMAIL AND THEN LOG IN THROUGH IT.
     * THIS NEEDS A EMAIL CONFIRMATION SERVICE IN THE MIDDLE.
     *
     * @param [email] The [email] that is used to identify the [User].
     * @param [username] the [username] used by the application to refer to the [User].
     * @param [password] the [password] used in the registration.
     * @return
     * - [pt.isel.service.account.model.CreatedNewUserAccount] success in case of user + linked account creation.
     * - [pt.isel.service.account.model.LoggedIntoUserAccount] success in case of no data creation, both linked account and user already exist.
     * - [pt.isel.service.account.model.LinkedNewAccount] success in case of linked account insertion into existing user.
     * - [UsernameAlreadyExists] error in case there already exists a user with that username. Only applies to not null usernames.
     * - [InvalidEmail] error if provided email does not satisfy data object restrictions
     * - [InvalidUsername] error if provided username does not satisfy data object restrictions.
     * - [InvalidPassword] error in case the password does not fulfill application restrictions.
     * - [LinkedAccountTypeMaxed] error in case there already is a [FormLinkedAccount] with the given [User].
     * @throws [IllegalStateException] if the password encoder unexpectedly returns null.
     * **/
    fun formSignUp(
        email: String, username: String?, password: String
    ): Either<ServiceError, SignUpResult> {
        val userExistsResult = userService.findByEmail(email)

        if(userExistsResult !is Success){   //Creates a new user and links a form account to it
            return userService.create(email, username).flatMap{ newUser ->
                 linkedAccountService.createFormAccount(newUser, password)
                     .map{ CreatedNewUserAccount(newUser) }
            }
        }

        val user = userExistsResult.right

        if(linkedAccountService.findUserFormAccount(user.id) is Success){ //Both user and linked account exist, return user
            return success(LoggedIntoUserAccount(user))
        }

        return linkedAccountService.createFormAccount(user, password) //Linked account does not exist, create it and return existing user
            .map{ LinkedNewAccount(user) }
    }

    /** @param [email] The [email] that is used to identify the [User].
     * @param providerName the name of the provider to map to [OAuthProvider].
     * @param providerId the provider unique identifier for the user provider account.
     * @return
     * - [CreatedNewUserAccount] success in case of user + linked account creation.
     * - [LoggedIntoUserAccount] success in case of no data creation, both linked account and user already exist.
     * - [LinkedNewAccount] success in case of linked account insertion into existing user.
     * - [UsernameAlreadyExists] error in case there already exists a user with that username. Only applies to not null usernames.
     * - [InvalidEmail] error if provided email does not satisfy data object restrictions
     * - [InvalidUsername] error if provided username does not satisfy data object restrictions.
     * - [InvalidProvider] if [providerName] does not match any [OAuthProvider]
     * - [InvalidProviderID] error in case the given provider id does not fulfill application restrictions.
     * - [LinkedAccountTypeMaxed] error in case there already is a [FormLinkedAccount] with the given [User].
     * **/
    fun oAuthSignUp(
        email: String, providerName: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val userExistsResult = userService.findByEmail(email)

        if(userExistsResult !is Success){ //Creates a new user and links provider oauth account to it
            return userService.create(email).flatMap{ newUser ->
                linkedAccountService.createOAuthAccount(newUser, providerName, providerId)
                    .map{ CreatedNewUserAccount(newUser) }
            }
        }

        val user = userExistsResult.right

        if(
            linkedAccountService.findUserOAuthAccount( //Both user and linked account exist, return user
                user.id,
                providerName,
                providerId
            ) is Success
        ){
            return success(LoggedIntoUserAccount(user))
        }

        return linkedAccountService.createOAuthAccount(user, providerName, providerId) //Linked account does not exist, create it and return existing user
            .map{ LinkedNewAccount(user) }
    }

    /** @param [userId] The unique identifier of [User].
     * @param providerName the name of the provider to map to [OAuthProvider].
     * @param providerId the provider unique identifier for the user provider account.
     * @return
     * - [LoggedIntoUserAccount] success in case of no data creation, both linked account and user already exist.
     * - [LinkedNewAccount] success in case of linked account insertion into existing user.
     * - [InvalidProvider] if [providerName] does not match any [OAuthProvider]
     * - [InvalidProviderID] error in case the given provider id does not fulfill application restrictions.
     * - [LinkedAccountTypeMaxed] error in case there already is a [FormLinkedAccount] with the given [User].
     * **/
    fun oAuthLink(
        userId: Int, providerName: String, providerId: String
    ): Either<ServiceError, SignUpResult> {
        val userResult = userService.findById(userId).onFailure { return failure(it) }
        val user = userResult.rightOrNull()!!

        if(
            linkedAccountService.findUserOAuthAccount( //Linked account exists, return user
                userId, providerName, providerId
            ) is Success
        ) { return success(LoggedIntoUserAccount(user)) }

        return linkedAccountService.createOAuthAccount(user, providerName, providerId) //Linked account does not exist, create it and return existing user
            .map { LinkedNewAccount(user) }
    }
}
