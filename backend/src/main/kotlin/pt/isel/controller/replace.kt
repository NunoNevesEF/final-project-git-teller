package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.GitAnalysis
import pt.isel.service.Success

@CrossOrigin(origins = ["http://localhost:8080"])
@RestController
class replace {
    @GetMapping("/")
    fun test(
    ): String {
        return "Temp Starting Page For Testing"
    }
}