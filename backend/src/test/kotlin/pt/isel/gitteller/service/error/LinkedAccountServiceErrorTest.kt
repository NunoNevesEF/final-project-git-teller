package pt.isel.gitteller.service.error

import org.springframework.http.HttpStatus
import pt.isel.domain.account.InvalidOAuthProviderException
import pt.isel.entity.account.BlankProviderIdException
import pt.isel.service.error.InvalidPassword
import pt.isel.service.error.InvalidProvider
import pt.isel.service.error.InvalidProviderID
import pt.isel.service.error.LinkedAccountNotFound
import pt.isel.service.error.LinkedAccountTypeMaxed
import pt.isel.service.error.toHttpStatus
import pt.isel.service.error.toServiceError
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkedAccountServiceErrorTest {

    @Test
    fun `LinkedAccountServiceError maps to correct HTTP status`() {
        assertEquals(HttpStatus.NOT_FOUND, LinkedAccountNotFound.toHttpStatus())
        assertEquals(HttpStatus.CONFLICT, LinkedAccountTypeMaxed.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidProviderID.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidPassword.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidProvider.toHttpStatus())
    }

    @Test
    fun `BlankProviderIdException maps to InvalidProviderID`() {
        assertEquals(
            InvalidProviderID,
            BlankProviderIdException().toServiceError()
        )
    }

    @Test
    fun `InvalidOAuthProviderException maps to InvalidProvider`() {
        assertEquals(
            InvalidProvider,
            InvalidOAuthProviderException().toServiceError()
        )
    }
}