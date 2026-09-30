package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.CartItem
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSaleScreen(
    cartItems: List<CartItem>,
    allProducts: List<ProductEntity>,
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    walkInName: String,
    walkInPhone: String,
    discountAmount: Double,
    paidAmount: Double,
    paymentMethod: String,
    settings: AppSettingsEntity,
    lang: String,
    onAddToCart: (ProductEntity, Boolean, Int) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (CustomerEntity?) -> Unit,
    onSetWalkInInfo: (String, String) -> Unit,
    onSetDiscount: (Double) -> Unit,
    onSetPaid: (Double) -> Unit,
    onSetPaymentMethod: (String) -> Unit,
    onScanQrClick: () -> Unit,
    onCompleteSale: () -> Unit
) {
    var searchProductQuery by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var discountInput by remember(discountAmount) { mutableStateOf(if (discountAmount > 0) discountAmount.toInt().toString() else "") }
    var paidInput by remember(paidAmount) { mutableStateOf(if (paidAmount > 0) paidAmount.toInt().toString() else "") }

    val filteredProducts = remember(searchProductQuery, allProducts) {
        if (searchProductQuery.isBlank()) allProducts.take(8)
        else allProducts.filter {
            it.name.contains(searchProductQuery, ignoreCase = true) ||
                    it.productId.contains(searchProductQuery, ignoreCase = true) ||
                    it.category.contains(searchProductQuery, ignoreCase = true)
        }
    }

    val subtotal = cartItems.sumOf { it.subtotal }
    val grandTotal = (subtotal - discountAmount).coerceAtLeast(0.0)
    val remaining = (grandTotal - paidAmount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_sale_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Row: Search product + Scan QR button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = !productDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = searchProductQuery,
                        onValueChange = {
                            searchProductQuery = it
                            productDropdownExpanded = true
                        },
                        placeholder = { Text("Search shoes to add to cart...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchProductQuery.isNotEmpty()) {
                                IconButton(onClick = { searchProductQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("pos_product_search")
                    )
                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded && filteredProducts.isNotEmpty(),
                        onDismissRequest = { productDropdownExpanded = false }
                    ) {
                        filteredProducts.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold)
                                            Text("${prod.productId} • Sz: ${prod.size} • ${prod.color}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${settings.currency} ${"%,.0f".format(prod.retailPrice)}", fontWeight = FontWeight.Bold)
                                            Text("Stock: ${prod.quantity}", style = MaterialTheme.typography.labelSmall, color = if (prod.quantity <= 0) Color.Red else Color(0xFF1B873F))
                                        }
                                    }
                                },
                                onClick = {
                                    onAddToCart(prod, false, 1)
                                    searchProductQuery = ""
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onScanQrClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp).testTag("pos_scan_qr_button")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR")
                }
            }
        }

        // Cart Items List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cart (${cartItems.sumOf { it.quantity }} items)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Cart", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cart is currently empty", fontWeight = FontWeight.Bold)
                        Text("Select shoes above or scan QR codes to add products.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            itemsIndexed(cartItems) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${item.product.productId} • Size: ${item.selectedSize} • Color: ${item.selectedColor}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { onRemoveItem(index) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quantity Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onUpdateQuantity(index, item.quantity - 1) }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.padding(6.dp))
                                }

                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onUpdateQuantity(index, item.quantity + 1) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.padding(6.dp))
                                }
                            }

                            // Price and subtotal
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "@ ${settings.currency} ${"%,.0f".format(item.unitPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${settings.currency} ${"%,.0f".format(item.subtotal)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Customer Selection
        item {
            Text("Customer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: walkInName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Customer") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Walk-in Customer (Cash Counter)") },
                        onClick = {
                            onSelectCustomer(null)
                            customerDropdownExpanded = false
                        }
                    )
                    customers.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} • ${c.phone} (Credit Bal: Rs. ${c.remainingBalance.toInt()})") },
                            onClick = {
                                onSelectCustomer(c)
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Discount & Payment Methods
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = {
                        discountInput = it
                        onSetDiscount(it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Discount (${settings.currency})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = paidInput,
                    onValueChange = {
                        paidInput = it
                        onSetPaid(it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Paid (${settings.currency})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Payment Method Toggle: Cash, Credit, Partial Payment
        item {
            Text("Payment Method", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cash", "Credit", "Partial Payment").forEach { m ->
                    FilterChip(
                        selected = paymentMethod == m,
                        onClick = { onSetPaymentMethod(m) },
                        label = { Text(m) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Calculation Totals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(subtotal)}", fontWeight = FontWeight.SemiBold)
                    }
                    if (discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", color = Color.Gray)
                            Text("- ${settings.currency} ${"%,.0f".format(discountAmount)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${settings.currency} ${"%,.0f".format(grandTotal)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paid Amount:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(paidAmount)}", color = Color(0xFF1B873F), fontWeight = FontWeight.Bold)
                    }
                    if (remaining > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer Balance (Credit):", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                            Text("${settings.currency} ${"%,.0f".format(remaining)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Complete Sale Button
        item {
            Button(
                onClick = onCompleteSale,
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pos_complete_sale_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "COMPLETE SALE & INVOICE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
