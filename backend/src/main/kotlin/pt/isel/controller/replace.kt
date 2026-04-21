package pt.isel.controller

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
class replace {
    @GetMapping("/")
    fun test(
    ): String {
        return "Temp Starting Page For Testing"
    }
}