package pt.isel.gitteller.service.error

import org.junit.jupiter.api.Assertions
import org.springframework.http.HttpStatus
import pt.isel.service.error.InvalidCredentialsError
import pt.isel.service.error.InvalidTokenError
import pt.isel.service.error.toHttpStatus
import kotlin.test.Test

class AuthServiceErrorTest{
    @Test
    fun `AuthServiceError maps to correct HTTP status`(){
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, InvalidTokenError.toHttpStatus())
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, InvalidCredentialsError.toHttpStatus())
    }
}