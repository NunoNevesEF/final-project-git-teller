package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.UserDTO
import pt.isel.service.Failure
import pt.isel.service.Success
import pt.isel.service.account.UserService

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/users")
class UserController(
    private val userService: UserService,
){
    @GetMapping("/id")
    fun read(
        @RequestParam id: Int
    ): ResponseEntity<UserDTO> {
        return when (val user = userService.read(id)) {
            is Success -> ResponseEntity.ok(UserDTO.create(user.right))
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/email")
    fun read(
        @RequestParam email: String
    ): ResponseEntity<UserDTO> {
        return when (val user = userService.read(email)) {
            is Success -> ResponseEntity.ok(UserDTO.create(user.right))
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}