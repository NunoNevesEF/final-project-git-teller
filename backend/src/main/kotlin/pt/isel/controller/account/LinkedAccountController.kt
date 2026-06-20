package pt.isel.controller.account

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.account.LinkedAccountDTO
import pt.isel.utils.Failure
import pt.isel.utils.Success
import pt.isel.service.account.LinkedAccountService

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