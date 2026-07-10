package pt.isel.gitteller.service.account.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus
import pt.isel.entity.account.User
import pt.isel.service.account.model.CreatedNewUserAccount
import pt.isel.service.account.model.LinkedNewAccount
import pt.isel.service.account.model.LoggedIntoUserAccount
import pt.isel.service.account.model.toHttpStatus
import kotlin.test.Test

class SignUpResultTest{
    val validUser = User(0, "test@email.com", "test")

    @Test
    fun `SignUpResult maps to correct HTTP status`(){
        assertEquals(HttpStatus.CREATED, CreatedNewUserAccount(validUser).toHttpStatus())
        assertEquals(HttpStatus.CREATED, LinkedNewAccount(validUser).toHttpStatus())
        assertEquals(HttpStatus.OK, LoggedIntoUserAccount(validUser).toHttpStatus())
    }
}