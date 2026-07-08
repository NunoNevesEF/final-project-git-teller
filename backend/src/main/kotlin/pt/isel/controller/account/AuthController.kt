package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.domain.TokenPair
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.service.auth.AuthService
import pt.isel.utils.rightOrNull

@CrossOrigin(origins = ["https://frontend-production-fc0c.up.railway.app"])
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