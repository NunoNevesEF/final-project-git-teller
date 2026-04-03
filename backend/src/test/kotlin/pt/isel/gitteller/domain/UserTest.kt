package pt.isel.gitteller.domain

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.domain.UserData
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UserTest {
    val validId = 333
    val validEmail = "test@email.com"
    val validUserName = "test"

    //
    // Testing Init
    //

    @Test
    fun `UserData creation succeeds if valid parameters`(){
        assertDoesNotThrow{ UserData(validId, validEmail, validUserName) }
    }

    @Test
    fun `UserData creation fails if id less than 0`(){
        assertFailsWith<IllegalArgumentException> { UserData(-1, validEmail, validUserName) }
    }

    @Test
    fun `UserData creation succeeds if id is 0`(){
        assertDoesNotThrow{ UserData(0, validEmail, validUserName) }
    }

    @Test
    fun `UserData creation fails if email is blank`() {
        assertFailsWith<IllegalArgumentException> { UserData(validId, "  ", validUserName) }
    }

    @Test
    fun `UserData creation fails if userName is blank`() {
        assertFailsWith<IllegalArgumentException> { UserData(validId, validEmail, "  ") }
    }

    //
    // Testing Companion
    //

    @Test
    fun `UserData companion method create defaults id to 0 if not passed`(){
        val actual = UserData.create(email = validEmail, userName = validUserName)
        val expected = UserData(0, validEmail, validUserName)
        assertEquals(expected, actual)
    }
}