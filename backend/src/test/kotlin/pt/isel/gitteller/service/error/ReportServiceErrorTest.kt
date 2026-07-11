package pt.isel.gitteller.service.error

import org.springframework.http.HttpStatus
import pt.isel.service.error.ReportNotFound
import pt.isel.service.error.ReportPDFNotFound
import pt.isel.service.error.toHttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportServiceErrorTest {
    @Test
    fun `ReportServiceError maps to correct HttpStatus`(){
        assertEquals(HttpStatus.NOT_FOUND, ReportNotFound.toHttpStatus())
        assertEquals(HttpStatus.NOT_FOUND, ReportPDFNotFound.toHttpStatus())
    }
}