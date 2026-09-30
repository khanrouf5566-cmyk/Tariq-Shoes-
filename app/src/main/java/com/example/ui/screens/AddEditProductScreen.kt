package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.ProductEntity
import com.example.util.Localization
import com.example.util.QrCodeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productToEdit: ProductEntity?,
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit,
    onSave: (
        id: Long,
        productId: String,
        name: String,
        category: String,
        design: String,
        brand: String,
        size: String,
        color: String,
        purchasePrice: Double,
        retailPrice: Double,
        wholesalePrice: Double,
        quantity: Int,
        supplierName: String,
        description: String,
        minStockLevel: Int,
        imageUri: String?
    ) -> Unit
) {
    val shoeSizes = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "Other")
    val shoeColors = listOf("Brown", "Black", "White", "Blue", "Red", "Grey", "Green", "Other")
    val shoeCategories = listOf("Peshawari Chappal", "Casual", "Formal", "Sandals", "Sneakers", "Slippers", "Traditional", "Other")

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "Peshawari Chappal") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var design by remember { mutableStateOf(productToEdit?.design ?: "") }
    var brand by remember { mutableStateOf(productToEdit?.brand ?: "Tariq Shoes") }

    var selectedSize by remember { mutableStateOf(productToEdit?.size ?: "42") }
    var selectedColor by remember { mutableStateOf(productToEdit?.color ?: "Brown") }

    var purchasePriceStr by remember { mutableStateOf(productToEdit?.purchasePrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var retailPriceStr by remember { mutableStateOf(productToEdit?.retailPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var wholesalePriceStr by remember { mutableStateOf(productToEdit?.wholesalePrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var quantityStr by remember { mutableStateOf(productToEdit?.quantity?.toString() ?: "10") }
    var minStockStr by remember { mutableStateOf(productToEdit?.minStockLevel?.toString() ?: settings.lowStockLimit.toString()) }

    var supplierName by remember { mutableStateOf(productToEdit?.supplierName ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var imageUri by remember { mutableStateOf(productToEdit?.imageUri) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Media selector launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    val displayCode = remember(productToEdit) {
        productToEdit?.productId ?: "TS-NEW"
    }

    val qrBitmap: Bitmap? by remember(displayCode) {
        derivedStateOf {
            try {
                QrCodeUtil.generateQrBitmap(displayCode, 260)
            } catch (_: Exception) {
                null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (productToEdit == null) Localization.tr("add_product", lang)
                        else Localization.tr("edit_product", lang)
                    )
                },
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
                .testTag("add_product_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card with QR Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Product QR",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(androidx.compose.ui.graphics.Color.White)
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Product Identifier: $displayCode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Unique QR code is automatically linked to this shoe record.",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.Gray
                            )
                        }
                    }
                }
            }

            // Image Picker Section
            item {
                Text("Product Image (Optional)", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected shoe",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Photo")
                        }
                        if (imageUri != null) {
                            OutlinedButton(
                                onClick = { imageUri = null },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            // Name field
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("${Localization.tr("product_name", lang)} *") },
                    placeholder = { Text("e.g. Peshawari Chappal / Leather Oxford") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )
            }

            // Category Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.tr("category", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        shoeCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Design / Model & Brand
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = design,
                        onValueChange = { design = it },
                        label = { Text(Localization.tr("design", lang)) },
                        placeholder = { Text("e.g. Zalmi Cut / Classic") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text(Localization.tr("brand", lang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Shoe Sizes Section (36 to 45 + Other)
            item {
                Text(
                    text = "${Localization.tr("size", lang)} (Shoe Size)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shoeSizes) { sz ->
                        FilterChip(
                            selected = selectedSize == sz,
                            onClick = { selectedSize = sz },
                            label = { Text(sz) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Shoe Colors Section
            item {
                Text(
                    text = Localization.tr("color", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shoeColors) { clr ->
                        FilterChip(
                            selected = selectedColor == clr,
                            onClick = { selectedColor = clr },
                            label = { Text(clr) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Pricing Row: Purchase, Retail, Wholesale
            item {
                Text(
                    text = "Pricing (${settings.currency})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text(Localization.tr("purchase_price", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_purchase_price")
                    )
                    OutlinedTextField(
                        value = retailPriceStr,
                        onValueChange = { retailPriceStr = it },
                        label = { Text(Localization.tr("retail_price", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_retail_price")
                    )
                    OutlinedTextField(
                        value = wholesalePriceStr,
                        onValueChange = { wholesalePriceStr = it },
                        label = { Text(Localization.tr("wholesale_price", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_wholesale_price")
                    )
                }
            }

            // Stock Quantities: Initial Quantity & Minimum Stock Level
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text(Localization.tr("quantity", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_quantity")
                    )
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text(Localization.tr("min_stock", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Supplier Name
            item {
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(Localization.tr("supplier", lang)) },
                    placeholder = { Text("e.g. Charsadda Tannery / Wholesale Distributor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Localization.tr("description", lang)) },
                    placeholder = { Text("Leather grade, sole material, warranty info...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error display
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Save Product Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isBlank()) {
                            errorMessage = "Product name is required"
                            return@Button
                        }
                        val purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0
                        val retailPrice = retailPriceStr.toDoubleOrNull() ?: 0.0
                        val wholesalePrice = wholesalePriceStr.toDoubleOrNull() ?: retailPrice
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val minStock = minStockStr.toIntOrNull() ?: 5

                        if (purchasePrice < 0 || retailPrice < 0 || wholesalePrice < 0) {
                            errorMessage = "Prices cannot be negative"
                            return@Button
                        }
                        if (qty < 0) {
                            errorMessage = "Quantity cannot be negative"
                            return@Button
                        }

                        errorMessage = null
                        onSave(
                            productToEdit?.id ?: 0L,
                            productToEdit?.productId ?: "",
                            trimmedName,
                            category,
                            design,
                            brand,
                            selectedSize,
                            selectedColor,
                            purchasePrice,
                            retailPrice,
                            wholesalePrice,
                            qty,
                            supplierName,
                            description,
                            minStock,
                            imageUri
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_save_product"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.tr("save_product", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
