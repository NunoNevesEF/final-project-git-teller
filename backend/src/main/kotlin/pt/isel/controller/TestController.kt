package pt.isel.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/test/properties")
class TestController(
    @Value("\${spring.application.name}") private val appName: String,
    @Value("\${spring.security.oauth2.client.registration.github.client-id}") private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.github.client-secret}") private val clientSecret: String,
){
    @GetMapping("/name")
    fun testPropertyName(): String{
        return "name: $appName\n"
    }
    /*@GetMapping("/gitHubId")
    fun testClientId(): String{
        return "id: $clientId"
    }
    @GetMapping("/gitHubSecret")
    fun testClientSecret(): String{
        return "secret: $clientSecret"
    }*/
}
