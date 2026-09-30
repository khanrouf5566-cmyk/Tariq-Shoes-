package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.util.Localization
import com.example.util.PdfInvoiceGenerator
import com.example.util.QrCodeUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    sale: SaleEntity?,
    items: List<SaleItemEntity>,
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit
) {
    if (sale == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Invoice not found")
        }
        return
    }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())

    val qrBitmap = remember(sale.invoiceNumber) {
        try {
            QrCodeUtil.generateQrBitmap("INVOICE:${sale.invoiceNumber}|DATE:${sale.date}|TOTAL:${sale.grandTotal}", 300)
        } catch (_: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sale.invoiceNumber) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("invoice_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Invoice Bill Sheet Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Shop Branding Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Phone: ${settings.shopPhone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = settings.shopLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Invoice & Customer Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Customer Name:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                Text(sale.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (sale.customerPhone.isNotBlank()) {
                                    Text("Phone: ${sale.customerPhone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Invoice Number:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Text(sdf.format(Date(sale.date)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Payment: ${sale.paymentMethod}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.5f))
                            Text("Size/Clr", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            Text("Qty", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
                            Text("Rate", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                            Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                        }

                        // Items rows
                        items.forEach { itm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(itm.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(itm.productId, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Text("${itm.size}/${itm.color}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("${itm.quantity}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.5f))
                                Text("%,.0f".format(itm.unitPrice), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
                                Text("%,.0f".format(itm.subtotal), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Totals Summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", color = Color.Gray)
                                Text("${settings.currency} ${"%,.0f".format(sale.subtotal)}")
                            }
                            if (sale.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", color = Color.Gray)
                                    Text("- ${settings.currency} ${"%,.0f".format(sale.discount)}", color = Color(0xFFBA1A1A))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold)
                                Text("${settings.currency} ${"%,.0f".format(sale.grandTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Paid Amount:", color = Color.Gray)
                                Text("${settings.currency} ${"%,.0f".format(sale.paidAmount)}", color = Color(0xFF1B873F), fontWeight = FontWeight.Bold)
                            }
                            if (sale.remainingAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Remaining Balance (Credit):", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                                    Text("${settings.currency} ${"%,.0f".format(sale.remainingAmount)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR Code Verification Box
                        if (qrBitmap != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Invoice QR",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons: SAVE PDF, PRINT, SHARE, WHATSAPP
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, settings, sale, items)
                            PdfInvoiceGenerator.sharePdf(context, pdfFile)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("button_invoice_save_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.tr("save_pdf", lang))
                    }

                    FilledTonalButton(
                        onClick = {
                            val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, settings, sale, items)
                            PdfInvoiceGenerator.printPdf(context, pdfFile)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.tr("print", lang))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, settings, sale, items)
                            PdfInvoiceGenerator.sharePdf(context, pdfFile)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.tr("share", lang))
                    }

                    FilledTonalButton(
                        onClick = {
                            val whatsappMsg = buildWhatsAppMessage(settings, sale, items)
                            PdfInvoiceGenerator.sendToWhatsApp(context, sale.customerPhone, whatsappMsg)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🟢 WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

private fun buildWhatsAppMessage(settings: AppSettingsEntity, sale: SaleEntity, items: List<SaleItemEntity>): String {
    val sb = StringBuilder()
    sb.appendLine("*${settings.shopName}*")
    sb.appendLine("📍 ${settings.shopLocation}")
    sb.appendLine("📞 ${settings.shopPhone}")
    sb.appendLine("---------------------------")
    sb.appendLine("🧾 *INVOICE: ${sale.invoiceNumber}*")
    sb.appendLine("👤 Customer: ${sale.customerName}")
    sb.appendLine("📅 Date: ${SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(sale.date))}")
    sb.appendLine("---------------------------")
    items.forEachIndexed { i, itm ->
        sb.appendLine("${i + 1}. ${itm.productName} (${itm.size}/${itm.color}) x ${itm.quantity} = Rs. ${"%,.0f".format(itm.subtotal)}")
    }
    sb.appendLine("---------------------------")
    sb.appendLine("Subtotal: Rs. ${"%,.0f".format(sale.subtotal)}")
    if (sale.discount > 0) sb.appendLine("Discount: - Rs. ${"%,.0f".format(sale.discount)}")
    sb.appendLine("*Grand Total: Rs. ${"%,.0f".format(sale.grandTotal)}*")
    sb.appendLine("Paid Amount: Rs. ${"%,.0f".format(sale.paidAmount)}")
    if (sale.remainingAmount > 0) {
        sb.appendLine("⚠️ *Remaining Balance: Rs. ${"%,.0f".format(sale.remainingAmount)}*")
    }
    sb.appendLine("Payment Method: ${sale.paymentMethod}")
    sb.appendLine("Thank you for your business!")
    return sb.toString()
}
