package pt.isel.controller.account

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pt.isel.model.account.OAuthLinkedAccountListItemDTO
import pt.isel.model.account.UserDTO
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.service.account.AccountService
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService
import pt.isel.service.account.UsernameAlreadyExists
import pt.isel.service.auth.JwtService

@CrossOrigin(origins = ["https://frontend-production-fc0c.up.railway.app"])
@RestController
@RequestMapping("/api/public/accounts")
class PublicAccountController(
    private val accountService: AccountService,
){
    @PostMapping("/signup")
    fun signup(
        @RequestParam email: String,
        @RequestParam username: String,
        @RequestParam password: String
    ): ResponseEntity<UserDTO> {
        return when(val user = accountService.formSignUp(email, username, password)){
            is Success -> ResponseEntity.ok(UserDTO.create(user.right.user))
            is Failure -> ResponseEntity.badRequest().build()
        }
    }
}

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/accounts")
class PrivateAccountController(
    private val jwtService: JwtService,
    private val linkedAccountService: LinkedAccountService,
    private val userService: UserService,
){
    @PutMapping("/username")
    fun setUsername(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam username: String,
    ): ResponseEntity<UserDTO> {
        return when (val result = userService.update(principal.getUserId(), username)) {
            is Success -> ResponseEntity.ok(UserDTO.create(result.right))
            is Failure -> when (result.left) {
                is UsernameAlreadyExists -> ResponseEntity.status(HttpStatus.CONFLICT).build()
                else -> ResponseEntity.badRequest().build()
            }
        }
    }

    @GetMapping("/link/{provider}")
    fun linkNewProviderAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable provider: String,
    ): ResponseEntity<Map<String, String>> {
        val state = jwtService.generateLinkState(
            userId = principal.getUserId(),
            provider = provider
        )

        val url = "oauth2/authorization/$provider?state=$state"

        return ResponseEntity.ok(mapOf("url" to url))
    }

    @GetMapping("/linked-account/git")
    fun listGitAccounts(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ResponseEntity<List<OAuthLinkedAccountListItemDTO>>{
        return when(val gitAccountsResult = linkedAccountService.findUserGitAccounts(principal.getUserId())){
            is Success -> ResponseEntity.ok(gitAccountsResult.right.map{OAuthLinkedAccountListItemDTO.create(it)})
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}