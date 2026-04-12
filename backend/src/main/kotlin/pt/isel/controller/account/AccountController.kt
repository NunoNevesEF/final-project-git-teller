package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.domain.account.TokenPair
import pt.isel.model.UserDTO
import pt.isel.service.Failure
import pt.isel.service.Success
import pt.isel.service.account.AccountService

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/accounts")
class AccountController(
    private val accountService: AccountService,
){
    @PostMapping("/signup")
    fun signup(
        @RequestParam email: String,
        @RequestParam username: String,
        @RequestParam password: String
    ): ResponseEntity<UserDTO> {
        return when(val user = accountService.formSignUp(email,username,password)){
            is Success -> ResponseEntity.ok(UserDTO.create(user.right))
            is Failure -> ResponseEntity.badRequest().build()
        }
    }
}