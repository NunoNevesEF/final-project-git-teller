package pt.isel.gitteller.controller.dto

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.controller.account.dto.OAuthLinkedAccountListItemDTO
import pt.isel.domain.account.OAuthProvider
import pt.isel.entity.account.OAuthLinkedAccount
import pt.isel.entity.account.User
import kotlin.test.Test

class OAuthLinkedAccountDTOTest {
    private val validUser = User(
        id = 0,
        email = "test@email.com",
        username = "test"
    )

    @Test
    fun `OAuthLinkedAccount correctly maps to OAuthLinkedAccountListItemDTO`(){
        val expected = OAuthLinkedAccountListItemDTO(0, "0")
        val testAccount = OAuthLinkedAccount(
            0, validUser, "", "", OAuthProvider.GITHUB, "0"
        )

        assertEquals(expected, OAuthLinkedAccountListItemDTO(testAccount))
    }
}