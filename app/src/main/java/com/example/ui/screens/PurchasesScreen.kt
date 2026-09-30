package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.SupplierEntity
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    purchases: List<PurchaseEntity>,
    products: List<ProductEntity>,
    suppliers: List<SupplierEntity>,
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit,
    onRecordPurchase: (
        supplierId: Long?,
        supplierName: String,
        productId: String,
        quantity: Int,
        purchasePrice: Double,
        paidAmount: Double,
        notes: String
    ) -> Unit
) {
    var showNewPurchaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.tr("purchase_stock", lang)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPurchaseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_new_purchase")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Purchase")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("purchases_screen")
        ) {
            if (purchases.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No stock purchases recorded yet", fontWeight = FontWeight.Bold)
                    Text("Click the + button to purchase shoe stock from suppliers.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(purchase.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("${purchase.productId} • ${purchase.supplierName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "+${purchase.quantity} pairs",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total Cost: ${settings.currency} ${"%,.0f".format(purchase.totalCost)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = sdf.format(Date(purchase.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    // New Purchase Dialog
    if (showNewPurchaseDialog) {
        var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
        var selectedSupplier by remember { mutableStateOf<SupplierEntity?>(suppliers.firstOrNull()) }
        var productExpanded by remember { mutableStateOf(false) }
        var supplierExpanded by remember { mutableStateOf(false) }

        var quantityStr by remember { mutableStateOf("10") }
        var purchasePriceStr by remember { mutableStateOf(selectedProduct?.purchasePrice?.toInt()?.toString() ?: "1500") }
        var paidAmountStr by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("Wholesale shipment") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showNewPurchaseDialog = false },
            title = { Text("Record Stock Purchase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Product Dropdown
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = !productExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (${it.productId})" } ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product to Restock") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.productId}) • Curr Stock: ${p.quantity}") },
                                    onClick = {
                                        selectedProduct = p
                                        purchasePriceStr = p.purchasePrice.toInt().toString()
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Supplier Dropdown
                    ExposedDropdownMenuBox(
                        expanded = supplierExpanded,
                        onExpandedChange = { supplierExpanded = !supplierExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSupplier?.name ?: "Direct Supplier",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Supplier") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = supplierExpanded,
                            onDismissRequest = { supplierExpanded = false }
                        ) {
                            suppliers.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSupplier = s
                                        supplierExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = purchasePriceStr,
                            onValueChange = { purchasePriceStr = it },
                            label = { Text("Unit Cost (${settings.currency})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val qty = quantityStr.toIntOrNull() ?: 0
                    val cost = purchasePriceStr.toDoubleOrNull() ?: 0.0
                    val totalCost = qty * cost

                    Text(
                        text = "Total Purchase Cost: ${settings.currency} ${"%,.0f".format(totalCost)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("Paid to Supplier (Leave empty for full)") },
                        placeholder = { Text("%,.0f".format(totalCost)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Invoice Ref") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = selectedProduct
                        if (prod == null) {
                            error = "Please select a product"
                            return@Button
                        }
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val cost = purchasePriceStr.toDoubleOrNull() ?: 0.0
                        if (qty <= 0) {
                            error = "Quantity must be greater than 0"
                            return@Button
                        }
                        val totalCost = qty * cost
                        val paid = paidAmountStr.toDoubleOrNull() ?: totalCost

                        onRecordPurchase(
                            selectedSupplier?.id,
                            selectedSupplier?.name ?: "Direct Supplier",
                            prod.productId,
                            qty,
                            cost,
                            paid,
                            notes
                        )
                        showNewPurchaseDialog = false
                    }
                ) {
                    Text("Add Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPurchaseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
