package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtil {

    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Generates a complete, beautiful printable shoe label with Tariq Shoes header,
     * product details, price in Rs., and QR code.
     */
    fun createPrintableQrLabel(
        shopName: String = "Tariq Shoes & Wholesale",
        productName: String,
        productId: String,
        size: String,
        shoeColor: String,
        price: Double,
        currency: String = "Rs."
    ): Bitmap {
        val labelWidth = 600
        val labelHeight = 850
        val bitmap = Bitmap.createBitmap(labelWidth, labelHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            this.color = Color.DKGRAY
            this.style = Paint.Style.STROKE
            this.strokeWidth = 4f
            this.isAntiAlias = true
        }
        canvas.drawRect(Rect(12, 12, labelWidth - 12, labelHeight - 12), borderPaint)

        // Header Background
        val headerPaint = Paint().apply {
            this.color = Color.parseColor("#1E1B18")
            this.style = Paint.Style.FILL
            this.isAntiAlias = true
        }
        canvas.drawRect(Rect(14, 14, labelWidth - 14, 110), headerPaint)

        // Shop Title
        val titlePaint = Paint().apply {
            this.color = Color.WHITE
            this.textSize = 28f
            this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText(shopName, (labelWidth / 2).toFloat(), 65f, titlePaint)

        val subtitlePaint = Paint().apply {
            this.color = Color.parseColor("#E6B15C")
            this.textSize = 18f
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("AUTHENTIC QUALITY FOOTWEAR", (labelWidth / 2).toFloat(), 95f, subtitlePaint)

        // Product Name
        val prodNamePaint = Paint().apply {
            this.color = Color.BLACK
            this.textSize = 30f
            this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        val safeName = if (productName.length > 25) productName.take(23) + "..." else productName
        canvas.drawText(safeName, (labelWidth / 2).toFloat(), 160f, prodNamePaint)

        // Product ID Badge
        val idPaint = Paint().apply {
            this.color = Color.DKGRAY
            this.textSize = 22f
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("CODE: $productId", (labelWidth / 2).toFloat(), 195f, idPaint)

        // Generate QR code and draw in center
        val qrBitmap = generateQrBitmap(productId, 360)
        canvas.drawBitmap(qrBitmap, ((labelWidth - 360) / 2).toFloat(), 215f, null)

        // Size and Color Row
        val metaPaint = Paint().apply {
            this.color = Color.BLACK
            this.textSize = 24f
            this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("SIZE: $size   |   COLOR: $shoeColor", (labelWidth / 2).toFloat(), 620f, metaPaint)

        // Price Section
        val priceBoxPaint = Paint().apply {
            this.color = Color.parseColor("#F4EFEA")
            this.style = Paint.Style.FILL
            this.isAntiAlias = true
        }
        canvas.drawRoundRect(40f, 650f, (labelWidth - 40).toFloat(), 760f, 16f, 16f, priceBoxPaint)

        val priceLabelPaint = Paint().apply {
            this.color = Color.GRAY
            this.textSize = 18f
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("RETAIL PRICE", (labelWidth / 2).toFloat(), 685f, priceLabelPaint)

        val priceValuePaint = Paint().apply {
            this.color = Color.parseColor("#794320")
            this.textSize = 40f
            this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("$currency ${"%,.0f".format(price)}", (labelWidth / 2).toFloat(), 735f, priceValuePaint)

        // Bottom Footer
        val footerPaint = Paint().apply {
            this.color = Color.GRAY
            this.textSize = 16f
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
        canvas.drawText("Scan with Tariq Shoes App for Instant Checkout", (labelWidth / 2).toFloat(), 805f, footerPaint)

        return bitmap
    }
}
