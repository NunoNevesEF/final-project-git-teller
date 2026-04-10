package pt.isel.gitteller.domain.account

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import pt.isel.domain.account.User
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
    fun `User creation succeeds if valid parameters`(){
        assertDoesNotThrow { User(validId, validEmail, validUserName) }
    }

    @Test
    fun `User creation fails if id less than 0`(){
        assertFailsWith<IllegalArgumentException> { User(-1, validEmail, validUserName) }
    }

    @Test
    fun `User creation succeeds if id is 0`(){
        assertDoesNotThrow { User(0, validEmail, validUserName) }
    }

    @Test
    fun `User creation fails if email is blank`() {
        assertFailsWith<IllegalArgumentException> { User(validId, "  ", validUserName) }
    }

    @Test
    fun `User creation fails if userName is blank`() {
        assertFailsWith<IllegalArgumentException> { User(validId, validEmail, "  ") }
    }

    //
    // Testing Companion
    //

    @Test
    fun `User companion method create defaults id to 0 if not passed`(){
        val actual = User.create(email = validEmail, userName = validUserName)
        val expected = User(0, validEmail, validUserName)
        assertEquals(expected, actual)
    }
}