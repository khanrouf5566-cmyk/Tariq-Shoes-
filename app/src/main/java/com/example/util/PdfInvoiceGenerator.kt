package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

    fun generateInvoicePdf(
        context: Context,
        settings: AppSettingsEntity,
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): File {
        val invoiceDir = File(context.filesDir, "invoices")
        if (!invoiceDir.exists()) {
            invoiceDir.mkdirs()
        }
        val file = File(invoiceDir, "${sale.invoiceNumber}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoiceLayout(canvas, settings, sale, items)

        document.finishPage(page)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    private fun drawInvoiceLayout(
        canvas: Canvas,
        settings: AppSettingsEntity,
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ) {
        val width = 595f
        val height = 842f
        val margin = 36f

        // Background
        canvas.drawColor(Color.WHITE)

        // Top Header Accent Bar
        val accentPaint = Paint().apply {
            color = Color.parseColor("#1E1B18")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width, 110f, accentPaint)

        // Golden stripe
        val goldPaint = Paint().apply {
            color = Color.parseColor("#D49339")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 105f, width, 110f, goldPaint)

        // Header Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(settings.shopName, margin, 42f, titlePaint)

        val headerDetailsPaint = Paint().apply {
            color = Color.parseColor("#DCD3CB")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("Phone: ${settings.shopPhone}", margin, 65f, headerDetailsPaint)
        canvas.drawText("Location: ${settings.shopLocation}", margin, 85f, headerDetailsPaint)

        // INVOICE Tag top right
        val invBadgePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", width - margin, 50f, invBadgePaint)

        val invNumHeaderPaint = Paint().apply {
            color = Color.parseColor("#E6B15C")
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(sale.invoiceNumber, width - margin, 75f, invNumHeaderPaint)

        // Invoice Meta Block
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.date))

        var currentY = 145f

        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val valueBoldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Bill To box (Left)
        canvas.drawText("BILL TO / CUSTOMER:", margin, currentY, labelPaint)
        canvas.drawText(sale.customerName, margin, currentY + 16f, valueBoldPaint)
        if (sale.customerPhone.isNotBlank()) {
            canvas.drawText("Phone: ${sale.customerPhone}", margin, currentY + 32f, labelPaint)
        }

        // Invoice Details (Right)
        val rightX = width - margin
        val rightLabelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val rightValuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("DATE & TIME:", rightX, currentY, rightLabelPaint)
        canvas.drawText(dateStr, rightX, currentY + 16f, rightValuePaint)
        canvas.drawText("PAYMENT METHOD: ${sale.paymentMethod}", rightX, currentY + 32f, rightLabelPaint)

        // Draw Table Header
        currentY = 210f
        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#F4EFEA")
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, currentY - 14f, width - margin, currentY + 14f, tableHeaderPaint)

        val tableHeaderFont = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val tableHeaderFontRight = Paint(tableHeaderFont).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawText("#", margin + 6f, currentY + 4f, tableHeaderFont)
        canvas.drawText("ITEM DESCRIPTION", margin + 28f, currentY + 4f, tableHeaderFont)
        canvas.drawText("CODE", margin + 210f, currentY + 4f, tableHeaderFont)
        canvas.drawText("SIZE/COLOR", margin + 280f, currentY + 4f, tableHeaderFont)
        canvas.drawText("QTY", margin + 375f, currentY + 4f, tableHeaderFontRight)
        canvas.drawText("PRICE", margin + 445f, currentY + 4f, tableHeaderFontRight)
        canvas.drawText("TOTAL", width - margin - 6f, currentY + 4f, tableHeaderFontRight)

        // Draw Items
        val itemFont = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val itemFontRight = Paint(itemFont).apply { textAlign = Paint.Align.RIGHT }
        val itemSubFont = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.8f
        }

        currentY += 28f
        for ((idx, item) in items.withIndex()) {
            val num = (idx + 1).toString()
            canvas.drawText(num, margin + 6f, currentY, itemFont)
            val name = if (item.productName.length > 28) item.productName.take(26) + ".." else item.productName
            canvas.drawText(name, margin + 28f, currentY, itemFont)
            canvas.drawText(item.productId, margin + 210f, currentY, itemSubFont)
            canvas.drawText("${item.size} / ${item.color}", margin + 280f, currentY, itemSubFont)
            canvas.drawText(item.quantity.toString(), margin + 375f, currentY, itemFontRight)
            canvas.drawText("%,.0f".format(item.unitPrice), margin + 445f, currentY, itemFontRight)
            canvas.drawText("%,.0f".format(item.subtotal), width - margin - 6f, currentY, itemFontRight)

            currentY += 8f
            canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
            currentY += 18f
        }

        // Totals Box
        currentY += 10f
        val totalBoxTop = currentY
        val totalBoxLeft = width - margin - 220f

        val totalBoxBg = Paint().apply {
            color = Color.parseColor("#FAFAFA")
            style = Paint.Style.FILL
        }
        val totalBorder = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(totalBoxLeft, totalBoxTop, width - margin, totalBoxTop + 130f, totalBoxBg)
        canvas.drawRect(totalBoxLeft, totalBoxTop, width - margin, totalBoxTop + 130f, totalBorder)

        var tY = totalBoxTop + 22f
        fun drawTotalRow(label: String, amount: Double, isBold: Boolean = false, colorHex: String = "#000000") {
            val labelP = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
                if (isBold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val valP = Paint().apply {
                color = Color.parseColor(colorHex)
                textSize = if (isBold) 13f else 11f
                typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText(label, totalBoxLeft + 12f, tY, labelP)
            canvas.drawText("${settings.currency} ${"%,.0f".format(amount)}", width - margin - 12f, tY, valP)
            tY += 22f
        }

        drawTotalRow("Subtotal:", sale.subtotal)
        if (sale.discount > 0) {
            drawTotalRow("Discount:", sale.discount, colorHex = "#BA1A1A")
        }
        drawTotalRow("Grand Total:", sale.grandTotal, isBold = true, colorHex = "#794320")
        drawTotalRow("Paid Amount:", sale.paidAmount, colorHex = "#1B873F")
        if (sale.remainingAmount > 0) {
            drawTotalRow("Remaining Balance:", sale.remainingAmount, isBold = true, colorHex = "#C62828")
        }

        // Draw QR Code on bottom left of invoice
        val qrSize = 110
        val qrBitmap = QrCodeUtil.generateQrBitmap("INVOICE:${sale.invoiceNumber}|TOTAL:${sale.grandTotal}", qrSize)
        val qrX = margin + 10f
        val qrY = totalBoxTop + 5f
        canvas.drawBitmap(qrBitmap, qrX, qrY, null)

        val qrHintPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Scan to verify invoice", qrX + (qrSize / 2), qrY + qrSize + 14f, qrHintPaint)

        // Terms and Signature at bottom
        val bottomY = height - 70f
        val termsPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Thank you for your business! Authentic Shoes & Wholesale Dealer.", margin, bottomY, termsPaint)
        canvas.drawText("Goods once sold can only be exchanged within 7 days with valid invoice.", margin, bottomY + 14f, termsPaint)

        // Signature line
        val signLinePaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(width - margin - 140f, bottomY + 10f, width - margin, bottomY + 10f, signLinePaint)
        val signTextPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Authorized Signature", width - margin - 70f, bottomY + 24f, signTextPaint)
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    }

    fun printPdf(context: Context, file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = PdfPrintDocumentAdapter(file)
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
        printManager.print("Invoice_${file.nameWithoutExtension}", printAdapter, printAttributes)
    }

    fun sendToWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (cleanPhone.startsWith("0")) "92" + cleanPhone.substring(1) else cleanPhone
        val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to generic share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }
}

class PdfPrintDocumentAdapter(private val pdfFile: File) : android.print.PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val pdi = android.print.PrintDocumentInfo.Builder(pdfFile.name)
            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()
        callback?.onLayoutFinished(pdi, true)
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: android.os.ParcelFileDescriptor?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            java.io.FileInputStream(pdfFile).use { input ->
                java.io.FileOutputStream(destination?.fileDescriptor).use { output ->
                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        output.write(buf, 0, bytesRead)
                    }
                }
            }
            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        }
    }
}
