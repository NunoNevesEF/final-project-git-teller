package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.LinkedAccountDTO
import pt.isel.model.UserDTO
import pt.isel.service.Failure
import pt.isel.service.Success
import pt.isel.service.account.LinkedAccountService
import pt.isel.service.account.UserService

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/linkedAccount")
class LinkedAccountController(
    private val linkedAccountService: LinkedAccountService,
){
    @GetMapping("/userId")
    fun read(
        @RequestParam userId: Int
    ): ResponseEntity<List<LinkedAccountDTO>> {
        return when (val linkedAccount = linkedAccountService.readByUser(userId)) {
            is Success -> ResponseEntity.ok(linkedAccount.right.map{ LinkedAccountDTO.create(it) })
            is Failure -> ResponseEntity.notFound().build()
        }
    }
}