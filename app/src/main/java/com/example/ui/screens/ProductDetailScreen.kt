package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.ProductEntity
import com.example.util.Localization
import com.example.util.QrCodeUtil
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity?,
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onQuickSell: (ProductEntity) -> Unit,
    onAdjustStock: (productId: String, newStock: Int, reason: String) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    val context = LocalContext.current
    var showAdjustStockDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showQrLabelDialog by remember { mutableStateOf(false) }

    val qrBitmap = remember(product.productId) {
        try {
            QrCodeUtil.generateQrBitmap(product.productId, 400)
        } catch (_: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(product) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("product_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shoe Image or Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (product.imageUri != null) {
                            AsyncImage(
                                model = product.imageUri,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = product.productId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    product.quantity <= 0 -> Color(0xFFFFDAD6)
                                    product.quantity <= product.minStockLevel -> Color(0xFFFFF1EB)
                                    else -> Color(0xFFE8F5E9)
                                }
                            ) {
                                Text(
                                    text = when {
                                        product.quantity <= 0 -> Localization.tr("out_of_stock", lang)
                                        product.quantity <= product.minStockLevel -> "LOW STOCK: ${product.quantity}"
                                        else -> "Stock: ${product.quantity} pairs"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        product.quantity <= 0 -> Color(0xFFBA1A1A)
                                        product.quantity <= product.minStockLevel -> Color(0xFFB87800)
                                        else -> Color(0xFF1B873F)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.design.isNotBlank()) {
                            Text(
                                text = "${product.brand} • ${product.design} • ${product.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Specs Card: Size, Color, Category, Brand
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Footwear Specifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Shoe Size:", color = Color.Gray)
                            Text(product.size, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Color:", color = Color.Gray)
                            Text(product.color, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Category:", color = Color.Gray)
                            Text(product.category, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Supplier / Source:", color = Color.Gray)
                            Text(product.supplierName ?: "Direct", fontWeight = FontWeight.SemiBold)
                        }
                        if (product.description.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Description:", color = Color.Gray)
                                Text(product.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Pricing Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Pricing Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Retail Selling Price:", color = Color.Gray)
                            Text("${settings.currency} ${"%,.0f".format(product.retailPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wholesale Dealer Price:", color = Color.Gray)
                            Text("${settings.currency} ${"%,.0f".format(product.wholesalePrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Purchase Cost (Private):", color = Color.Gray)
                            Text("${settings.currency} ${"%,.0f".format(product.purchasePrice)}", fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Unit Profit Margin (Retail):", color = Color.Gray)
                            val profit = product.retailPrice - product.purchasePrice
                            Text("${settings.currency} ${"%,.0f".format(profit)}", fontWeight = FontWeight.Bold, color = Color(0xFF1B873F))
                        }
                    }
                }
            }

            // QR Code System Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Unique Product QR Code", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CODE: ${product.productId}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { showQrLabelDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.tr("print_qr_label", lang))
                            }
                        }
                    }
                }
            }

            // Major Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onQuickSell(product) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("button_detail_sell_now"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELL NOW", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { showAdjustStockDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD STOCK", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Adjust Stock Dialog
    if (showAdjustStockDialog) {
        var newStockStr by remember { mutableStateOf(product.quantity.toString()) }
        var reason by remember { mutableStateOf("Stock recount") }

        AlertDialog(
            onDismissRequest = { showAdjustStockDialog = false },
            title = { Text("Adjust Stock Quantity") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current stock: ${product.quantity} pairs")
                    OutlinedTextField(
                        value = newStockStr,
                        onValueChange = { newStockStr = it },
                        label = { Text("New Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stock = newStockStr.toIntOrNull() ?: product.quantity
                        onAdjustStock(product.productId, stock, reason)
                        showAdjustStockDialog = false
                    }
                ) {
                    Text("Update Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustStockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(Localization.tr("delete_product", lang)) },
            text = { Text(Localization.tr("confirm_delete", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(product)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Printable QR Label Dialog
    if (showQrLabelDialog) {
        val labelBitmap = remember(product) {
            QrCodeUtil.createPrintableQrLabel(
                shopName = settings.shopName,
                productName = product.name,
                productId = product.productId,
                size = product.size,
                shoeColor = product.color,
                price = product.retailPrice,
                currency = settings.currency
            )
        }

        AlertDialog(
            onDismissRequest = { showQrLabelDialog = false },
            title = { Text("Printable Shoe Box QR Label") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = labelBitmap.asImageBitmap(),
                        contentDescription = "Printable Label",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "High resolution label ready for packaging & printing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareQrLabel(context, labelBitmap, product.productId)
                        showQrLabelDialog = false
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Label")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrLabelDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun shareQrLabel(context: Context, bitmap: Bitmap, productId: String) {
    try {
        val file = File(context.cacheDir, "label_${productId}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Label"))
    } catch (_: Exception) {}
}
