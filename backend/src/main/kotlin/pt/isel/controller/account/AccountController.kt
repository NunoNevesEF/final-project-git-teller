package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pt.isel.controller.account.dto.OAuthLinkedAccountListItemDTO
import pt.isel.controller.account.dto.UserDTO
import pt.isel.entity.account.FormLinkedAccount
import pt.isel.entity.account.User
import pt.isel.infraestructure.security.principal.UserPrincipal
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.service.account.AccountOrchestrator
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.service.error.toHttpStatus
import pt.isel.service.account.model.toHttpStatus
import pt.isel.infraestructure.security.jwt.JwtService
import pt.isel.infraestructure.oauth.CustomOAuth2UserService

/**
 *  `PublicAccountController`
 *
 * Exposes account related presentation-layer endpoints of the application that do not require authorization.
 * */
@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/accounts")
class PublicAccountController(
    private val accountService: AccountOrchestrator,
) {
    /**
     * Registers a new [User] and/or it's [FormLinkedAccount] using email and password registration.
     *
     * Returns:
     * - `200 ok` with the corresponding [UserDTO] if the [email] was already registered and has a form account associated.
     * - `201 Created` with the created [UserDTO] if it created a user or a form account.
     * - `400 Bad Request` if the email, username, or password is invalid.
     * - `409 Conflict` if the username already exists or the linked account type limit has been reached.
     *
     * @param email The email address used to identify the new [User].
     * @param username The username chosen by the user.
     * @param password The password provided during registration.
     *
     * @return A [ResponseEntity] containing the created [UserDTO] on success, or an empty response with the appropriate HTTP status on failure.
     *
     * @throws IllegalStateException If the password encoder unexpectedly returns `null`.
     */
    @PostMapping("/signup")
    fun signup(
        @RequestParam email: String, @RequestParam username: String, @RequestParam password: String
    ): ResponseEntity<UserDTO> {
        return when (val user = accountService.formSignUp(email, username, password)) {
            is Success -> ResponseEntity.status(user.right.toHttpStatus()).body(UserDTO(user.right.user))
            is Failure -> ResponseEntity.status(user.left.toHttpStatus()).build()
        }
    }
}

/**
 *  `PrivateAccountController`
 *
 * Exposes account related presentation-layer endpoints of the application that require authorization.
 * */
@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/accounts")
class PrivateAccountController(
    private val jwtService: JwtService,
    private val linkedAccountService: LinkedAccountService,
    private val userService: UserService,
) {
    /**
     * Updates the username of a [User] registered in the application.
     *
     * Returns:
     * - `200 Ok` with the updated [UserDTO] on success.
     * - `400 Bad Request` if the username is invalid.
     * - `404 Not found` if the [User] being updated was not found.
     * - `409 Conflict` if the username already exists.
     *
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     * @param username The new username chosen by the user.
     *
     * @return A [ResponseEntity] containing the created [UserDTO] on success, or an empty response with the appropriate HTTP status on failure.
     */
    @PutMapping("/username")
    fun setUsername(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam username: String,
    ): ResponseEntity<UserDTO> {
        return when (val result = userService.updateUsername(principal.getUserId(), username)) {
            is Success -> ResponseEntity.ok(UserDTO(result.right))
            is Failure -> ResponseEntity.status(result.left.toHttpStatus()).build()
        }
    }

    /**
     * Generates a temporary [JwtService.OAuthLinkState] to safely store a [User]'s intent to link a new [provider].
     *
     * Returns:`200 Ok` with the url with the correct requests params that store the user's account link intent of a
     * specified [provider] to be handled by [CustomOAuth2UserService]
     *
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     * @param provider The provider whose account the user wants to create a corresponding linked account for.
     *
     * @return A [ResponseEntity] containing the created url.
     */
    @GetMapping("/link/{provider}")
    fun linkNewProviderAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable provider: String,
    ): ResponseEntity<Map<String, String>> {
        val state = jwtService.generateLinkState(
            userId = principal.getUserId(), provider = provider
        )

        val url = "oauth2/authorization/$provider?state=$state"

        return ResponseEntity.ok(mapOf("url" to url))
    }

    /**
     * Returns:`200 Ok` with the list of git service linked accounts of the user.
     *
     * @param principal The authentication user ([UserPrincipal]) held in the application's security context for this request.
     *
     * @return A [ResponseEntity] containing a list of [OAuthLinkedAccountListItemDTO] from the git service accounts of the user.
     */
    @GetMapping("/linked-account/git")
    fun listGitAccounts(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<List<OAuthLinkedAccountListItemDTO>> {
        return ResponseEntity.ok(
            linkedAccountService.findUserGitAccounts(principal.getUserId())
                .map { OAuthLinkedAccountListItemDTO(it) }
        )
    }
}