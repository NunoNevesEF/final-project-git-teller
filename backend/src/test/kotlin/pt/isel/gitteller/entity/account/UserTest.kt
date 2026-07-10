package pt.isel.gitteller.entity.account

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pt.isel.entity.account.BlankEmailException
import pt.isel.entity.account.BlankUsernameException
import pt.isel.entity.account.User
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UserTest {
    val validId = 333
    val validEmail = "test@email.com"
    val validUsername = "test"

    @Test
    fun `User creation succeeds if valid parameters`(){
        assertDoesNotThrow { User(validId, validEmail, validUsername) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `User creation fails with BlankEmailException if email is empty or blank`(email: String) {
        assertFailsWith<BlankEmailException> { User(validId, email, validUsername) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `User creation fails with BlankUsernameException if username is empty blank`(username: String) {
        assertFailsWith<BlankUsernameException> { User(validId, validEmail, username) }
    }

    @Test
    fun `User creation succeeds if username is null`(){
        assertDoesNotThrow { User(validId, validEmail, null) }
    }
}