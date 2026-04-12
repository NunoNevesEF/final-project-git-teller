package pt.isel.service

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import org.springframework.stereotype.Service

@Service
class ReportGenerationService {

    fun createPdf(componentImageBytes: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Git Analysis Report").setBold().setFontSize(18f))
        document.add(Paragraph("\n"))

        val imageData = ImageDataFactory.create(componentImageBytes)
        val image = Image(imageData)

        val pageWidth = pdf.defaultPageSize.width - document.leftMargin - document.rightMargin
        val scale = pageWidth / image.imageWidth
        image.scale(scale, scale)

        document.add(image)

        document.close()
        return outputStream.toByteArray()
    }
}