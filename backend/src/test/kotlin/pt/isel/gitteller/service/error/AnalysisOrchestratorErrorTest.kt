package pt.isel.gitteller.service.error

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import pt.isel.service.error.MissingGitLinkedAccountId
import pt.isel.service.error.toHttpStatus
import kotlin.test.assertEquals

class AnalysisOrchestratorErrorTest {
    @Test
    fun `AnalysisOrchestratorError maps to correct HttpStatus`(){
        assertEquals(HttpStatus.BAD_REQUEST, MissingGitLinkedAccountId.toHttpStatus())
    }
}