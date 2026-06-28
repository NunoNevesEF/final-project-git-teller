package pt.isel.controller.account

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pt.isel.model.account.UserDTO
import pt.isel.security.principal.UserPrincipal
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.service.account.AccountService
import pt.isel.service.auth.JwtService

@CrossOrigin(origins = ["http://localhost:8081"])
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
    private val jwtService: JwtService
){
    @GetMapping("/link/{provider}")
    fun link(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable provider: String,
        response: HttpServletResponse,
    ): ResponseEntity<Map<String, String>> {
        val state = jwtService.generateLinkState(
            userId = principal.getUserId(),
            provider = provider
        )

        val url = "oauth2/authorization/$provider?state=$state"

        return ResponseEntity.ok(mapOf("url" to url))
    }
}