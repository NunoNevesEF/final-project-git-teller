package pt.isel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.Media
import org.springframework.stereotype.Service
import pt.isel.domain.GitAnalysis

@Service
class ReportGenerationService {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun createPdf(gitAnalysis: GitAnalysis): ByteArray {
        val byte = generatePdfFromFrontend(gitAnalysis)
        return byte;
    }

    fun generatePdfFromFrontend(gitAnalysis: GitAnalysis): ByteArray {

        Playwright.create().use { pw ->

            val browser = pw.chromium().launch()
            val context = browser.newContext()
            val page = context.newPage()

            val json = objectMapper.writeValueAsString(gitAnalysis)

            page.emulateMedia(Page.EmulateMediaOptions().setMedia(Media.PRINT))
            page.addInitScript("window.__GIT_ANALYSIS__ = $json")
            page.navigate("http://localhost:8081/Info")

            page.waitForLoadState(LoadState.NETWORKIDLE)

            val pdf = page.pdf(
                Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true)
            )

            browser.close()

            return pdf
        }
    }
}