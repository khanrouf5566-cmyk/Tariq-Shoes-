package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(
    product: ProductEntity,
    customers: List<CustomerEntity>,
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit,
    onCompleteSale: (
        size: String,
        color: String,
        quantity: Int,
        isWholesale: Boolean,
        unitPrice: Double,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        customerName: String,
        customerPhone: String,
        customerId: Long?
    ) -> Unit
) {
    val shoeSizes = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "Other")
    val shoeColors = listOf("Brown", "Black", "White", "Blue", "Red", "Grey", "Green", "Other")

    var selectedSize by remember { mutableStateOf(product.size) }
    var selectedColor by remember { mutableStateOf(product.color) }
    var quantityStr by remember { mutableStateOf("1") }
    var isWholesale by remember { mutableStateOf(false) }

    val basePrice = if (isWholesale) product.wholesalePrice else product.retailPrice
    var unitPriceStr by remember(isWholesale) { mutableStateOf(basePrice.toInt().toString()) }

    var discountStr by remember { mutableStateOf("0") }

    // Customer
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var walkInName by remember { mutableStateOf("Walk-in Customer") }
    var walkInPhone by remember { mutableStateOf("") }

    // Payment method
    var paymentMethod by remember { mutableStateOf("Cash") } // "Cash", "Credit", "Partial Payment"
    var paidAmountStr by remember { mutableStateOf("") }

    val quantity = quantityStr.toIntOrNull() ?: 1
    val unitPrice = unitPriceStr.toDoubleOrNull() ?: basePrice
    val discount = discountStr.toDoubleOrNull() ?: 0.0

    val subtotal = quantity * unitPrice
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    val actualPaid = when (paymentMethod) {
        "Cash" -> grandTotal
        "Credit" -> 0.0
        else -> paidAmountStr.toDoubleOrNull() ?: (grandTotal / 2)
    }
    val remaining = (grandTotal - actualPaid).coerceAtLeast(0.0)

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.tr("quick_sale", lang)) },
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
                .testTag("quick_sale_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Product Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.productId,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (product.quantity <= product.minStockLevel) Color(0xFFBA1A1A) else Color(0xFF1B873F)
                            ) {
                                Text(
                                    text = "In Stock: ${product.quantity}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${product.brand} • ${product.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Size Selector
            item {
                Text("Select Shoe Size", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
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

            // Color Selector
            item {
                Text("Select Shoe Color", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
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

            // Quantity & Wholesale Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity (Pairs) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_quick_sale_quantity")
                    )

                    Card(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWholesale) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wholesale Rate", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Text(if (isWholesale) "Bulk Price" else "Retail Price", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(
                                checked = isWholesale,
                                onCheckedChange = { isWholesale = it }
                            )
                        }
                    }
                }
            }

            // Price & Discount Inputs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Unit Price (${settings.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount (${settings.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Customer Selection
            item {
                Text("Customer Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: walkInName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Customer") },
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
                                selectedCustomer = null
                                walkInName = "Walk-in Customer"
                                customerDropdownExpanded = false
                            }
                        )
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} • ${cust.phone} • Bal: Rs. ${cust.remainingBalance.toInt()}") },
                                onClick = {
                                    selectedCustomer = cust
                                    walkInName = cust.name
                                    walkInPhone = cust.phone
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCustomer == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = walkInName,
                            onValueChange = { walkInName = it },
                            label = { Text("Walk-in Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = walkInPhone,
                            onValueChange = { walkInPhone = it },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Payment Types: Cash, Credit, Partial Payment
            item {
                Text("Payment Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Credit", "Partial Payment").forEach { type ->
                        FilterChip(
                            selected = paymentMethod == type,
                            onClick = {
                                paymentMethod = type
                                if (type == "Partial Payment" && paidAmountStr.isBlank()) {
                                    paidAmountStr = (grandTotal / 2).toInt().toString()
                                }
                            },
                            label = { Text(type) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                if (paymentMethod == "Partial Payment") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("Received / Paid Amount (${settings.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal ($quantity pcs):", color = Color.Gray)
                            Text("${settings.currency} ${"%,.0f".format(subtotal)}", fontWeight = FontWeight.SemiBold)
                        }
                        if (discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:", color = Color.Gray)
                                Text("- ${settings.currency} ${"%,.0f".format(discount)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("${settings.currency} ${"%,.0f".format(grandTotal)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid Amount:", color = Color.Gray)
                            Text("${settings.currency} ${"%,.0f".format(actualPaid)}", color = Color(0xFF1B873F), fontWeight = FontWeight.Bold)
                        }
                        if (remaining > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Remaining (Credit):", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                                Text("${settings.currency} ${"%,.0f".format(remaining)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Complete Sale Button
            item {
                Button(
                    onClick = {
                        if (quantity <= 0) {
                            errorMessage = "Quantity must be at least 1"
                            return@Button
                        }
                        if (!settings.allowNegativeStock && quantity > product.quantity) {
                            errorMessage = "Cannot sell $quantity pieces. Only ${product.quantity} pairs in stock!"
                            return@Button
                        }

                        errorMessage = null
                        onCompleteSale(
                            selectedSize,
                            selectedColor,
                            quantity,
                            isWholesale,
                            unitPrice,
                            discount,
                            actualPaid,
                            paymentMethod,
                            selectedCustomer?.name ?: walkInName,
                            selectedCustomer?.phone ?: walkInPhone,
                            selectedCustomer?.id
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_complete_quick_sale"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMPLETE SALE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
