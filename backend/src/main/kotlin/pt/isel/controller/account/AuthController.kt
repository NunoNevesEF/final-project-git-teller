package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.domain.account.TokenPair
import pt.isel.service.Failure
import pt.isel.service.Success
import pt.isel.service.account.auth.AuthService
import pt.isel.service.rightOrNull

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<TokenPair> {
        return ResponseEntity.ok(authService.login(email, password))
    }

    @PostMapping("/refresh-token")
    fun refresh(
        @RequestParam refreshToken: String
    ): ResponseEntity<TokenPair> {
        return when(val token = authService.refreshToken(refreshToken)) {
            is Success -> ResponseEntity.ok(token.rightOrNull())
            is Failure -> ResponseEntity.badRequest().build()
        }
    }
}