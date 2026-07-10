package pt.isel.service.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.Media
import org.springframework.stereotype.Service
import pt.isel.model.report.GitAnalysis

/**
 *  `ReportPDFGenerationService`
 *
 * The service layer report-related operations to generate a pdf from the report presentation.
 * */
@Service
class PDFGenerationService {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    /**
     * Generates a PDF representation of the supplied Git analysis.
     *
     * @param gitAnalysis The Git analysis to render as a PDF.
     * @return The generated PDF as a byte array.
     */
    fun createPdf(gitAnalysis: GitAnalysis): ByteArray {
        val byte = generatePdfFromFrontend(gitAnalysis)
        return byte
    }

    /**
     * Generates a PDF by rendering the frontend report using Playwright.
     *
     * The supplied [GitAnalysis] is serialized to JSON and injected into the
     * report page before it is rendered in print mode. The resulting page is
     * exported as an A4 PDF.
     *
     * @param gitAnalysis The Git analysis to render.
     * @return The generated PDF as a byte array.
     */
    fun generatePdfFromFrontend(gitAnalysis: GitAnalysis): ByteArray {

        Playwright.create().use { pw ->

            val browser = pw.chromium().launch()
            val context = browser.newContext()
            val page = context.newPage()

            val json = objectMapper.writeValueAsString(gitAnalysis)

            page.emulateMedia(Page.EmulateMediaOptions().setMedia(Media.PRINT))
            page.addInitScript("window.__GIT_ANALYSIS__ = $json")
            page.navigate("http://localhost:8081/Info")

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