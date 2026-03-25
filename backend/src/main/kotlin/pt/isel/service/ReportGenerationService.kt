package pt.isel.service

import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import org.springframework.stereotype.Service
import pt.isel.domain.GitAnalysis

@Service
class ReportGenerationService {

    fun createPdf(gitAnalysis: GitAnalysis) : ByteArray  {
        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Git Analysis Report"))

        gitAnalysis.commitsByUser.forEach { (user, commits) ->
            document.add(Paragraph("User: $user"))

            commits.forEach { commit ->
                document.add(Paragraph(" - ${commit.message}"))
            }
        }

        document.close()

        return outputStream.toByteArray()
    }
}