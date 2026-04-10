package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.domain.account.TokenPair
import pt.isel.service.account.AuthService

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
}