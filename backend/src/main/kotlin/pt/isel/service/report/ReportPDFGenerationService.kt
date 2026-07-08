package pt.isel.service.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.Media
import org.springframework.stereotype.Service
import pt.isel.model.report.GitAnalysis

@Service
class ReportPDFGenerationService {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun createPdf(gitAnalysis: GitAnalysis): ByteArray {
        val byte = generatePdfFromFrontend(gitAnalysis)
        return byte
    }

    fun generatePdfFromFrontend(gitAnalysis: GitAnalysis): ByteArray {

        Playwright.create().use { pw ->

            val browser = pw.chromium().launch()
            val context = browser.newContext()
            val page = context.newPage()

            val json = objectMapper.writeValueAsString(gitAnalysis)

            page.onConsoleMessage { msg ->
                println("CONSOLE: ${msg.type()} ${msg.text()}")
            }

            page.onPageError { error ->
                println("PAGE ERROR: $error")
            }

            page.onRequestFailed { request ->
                println("REQUEST FAILED: ${request.url()} ${request.failure()}")
            }

            page.emulateMedia(Page.EmulateMediaOptions().setMedia(Media.PRINT))
            page.addInitScript("window.__GIT_ANALYSIS__ = $json")
            page.navigate("https://frontend-production-fc0c.up.railway.app/Info")

            println("URL: ${page.url()}")
            println(page.content().take(2000))

            page.setViewportSize(1920, 1080)

            page.waitForLoadState(LoadState.LOAD)
            page.waitForFunction(
                "window.__REPORT_READY__ === true"
            )

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