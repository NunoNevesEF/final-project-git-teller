package pt.isel.gitteller.service.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus
import pt.isel.service.error.CouldNotDetermineAuthentication
import pt.isel.service.error.GitConnectionTimeout
import pt.isel.service.error.InvalidFilter
import pt.isel.service.error.InvalidRepoUri
import pt.isel.service.error.RepoNotFoundOrPrivate
import pt.isel.service.error.UnknownGitAnalysisError
import pt.isel.service.error.toHttpStatus
import kotlin.test.Test

class GitAnalysisServiceErrorTest{
    @Test
    fun `GitAnalysisServiceError maps to correct HttpStatus`(){
        assertEquals(HttpStatus.NOT_FOUND, CouldNotDetermineAuthentication.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidFilter.toHttpStatus())
        assertEquals(HttpStatus.BAD_REQUEST, InvalidRepoUri.toHttpStatus())
        assertEquals(HttpStatus.NOT_FOUND, RepoNotFoundOrPrivate.toHttpStatus())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, UnknownGitAnalysisError.toHttpStatus())
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, GitConnectionTimeout.toHttpStatus())
    }
}