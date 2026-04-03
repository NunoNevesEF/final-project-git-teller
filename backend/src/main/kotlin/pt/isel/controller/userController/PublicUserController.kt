package pt.isel.controller.userController

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.UserDTO
import pt.isel.service.Failure
import pt.isel.service.Success
import pt.isel.service.userServices.FormUserService

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/users")
class PublicUserController(
    private val userService: FormUserService,
){
    @GetMapping("/signup")
    fun signUp(
        @RequestParam email: String,
        @RequestParam userName: String,
        @RequestParam password: String
    ): ResponseEntity<UserDTO> {
        return when (val user = userService.create(email, userName, password)) {
            is Success -> ResponseEntity.ok(user.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/id")
    fun read(
        @RequestParam id: Int
    ): ResponseEntity<UserDTO> {
        return when (val user = userService.read(id)) {
            is Success -> ResponseEntity.ok(user.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/email")
    fun read(
        @RequestParam email: String
    ): ResponseEntity<UserDTO> {
        return when (val user = userService.read(email)) {
            is Success -> ResponseEntity.ok(user.right)
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}