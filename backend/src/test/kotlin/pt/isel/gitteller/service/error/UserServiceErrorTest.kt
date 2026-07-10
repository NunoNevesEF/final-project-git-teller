package pt.isel.gitteller.service.error

import org.springframework.http.HttpStatus
import pt.isel.entity.account.BlankEmailException
import pt.isel.entity.account.BlankUsernameException
import pt.isel.service.error.EmailAlreadyExists
import pt.isel.service.error.InvalidEmail
import pt.isel.service.error.InvalidUsername
import pt.isel.service.error.UserNotFound
import pt.isel.service.error.UsernameAlreadyExists
import pt.isel.service.error.toHttpStatus
import pt.isel.service.error.toServiceError
import kotlin.test.Test
import kotlin.test.assertEquals

class UserServiceErrorTest {
    @Test
    fun `UserServiceError maps to correct HTTP status`() {
        assertEquals(HttpStatus.NOT_FOUND, UserNotFound.toHttpStatus())
        assertEquals(HttpStatus.CONFLICT, EmailAlreadyExists.toHttpStatus())
        assertEquals(HttpStatus.CONFLICT, UsernameAlreadyExists.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidEmail.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidUsername.toHttpStatus())
    }

    @Test
    fun `BlankEmailException maps to InvalidEmail`() {
        assertEquals(InvalidEmail, BlankEmailException().toServiceError())
    }

    @Test
    fun `BlankUsernameException maps to InvalidUsername`() {
        assertEquals(InvalidUsername, BlankUsernameException().toServiceError())
    }
}