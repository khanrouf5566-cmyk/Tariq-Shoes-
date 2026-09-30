package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.util.Localization

@Composable
fun CustomersScreen(
    customers: List<CustomerEntity>,
    searchQuery: String,
    settings: AppSettingsEntity,
    lang: String,
    onSearchChanged: (String) -> Unit,
    onSaveCustomer: (id: Long, name: String, phone: String, address: String) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onRecordPayment: (customerId: Long, amount: Double, method: String, notes: String) -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedCustomerForDetails by remember { mutableStateOf<CustomerEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("customers_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search customer by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("customer_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (customers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No customer records found", fontWeight = FontWeight.Bold)
                    Text("Tap the + button to add wholesale or retail customers.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomerForDetails = customer }
                                .testTag("customer_item_${customer.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        if (customer.phone.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (customer.remainingBalance > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = if (customer.remainingBalance > 0) "Credit: ${settings.currency} ${"%,.0f".format(customer.remainingBalance)}" else "All Paid",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (customer.remainingBalance > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (customer.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(customer.address, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Purchases: ${settings.currency} ${"%,.0f".format(customer.totalPurchases)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (customer.remainingBalance > 0) {
                                            OutlinedButton(
                                                onClick = { customerForPayment = customer },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pay", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                customerToEdit = customer
                                                showAddEditDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (customerToEdit == null) "Add Customer" else "Edit Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_phone")
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / City") },
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
                        if (name.isBlank()) {
                            error = "Customer name is required"
                            return@Button
                        }
                        onSaveCustomer(customerToEdit?.id ?: 0L, name.trim(), phone.trim(), address.trim())
                        showAddEditDialog = false
                    },
                    modifier = Modifier.testTag("button_save_customer")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Record Payment Dialog
    if (customerForPayment != null) {
        val cust = customerForPayment!!
        var payAmountStr by remember { mutableStateOf(cust.remainingBalance.toInt().toString()) }
        var payMethod by remember { mutableStateOf("Cash") }
        var payNotes by remember { mutableStateOf("Balance clearance") }

        AlertDialog(
            onDismissRequest = { customerForPayment = null },
            title = { Text("Record Customer Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Customer: ${cust.name}", fontWeight = FontWeight.Bold)
                    Text("Outstanding Balance: ${settings.currency} ${"%,.0f".format(cust.remainingBalance)}", color = Color(0xFFBA1A1A))
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("Amount Received (${settings.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cash", "Online", "Cheque").forEach { m ->
                            Button(
                                onClick = { payMethod = m },
                                colors = if (payMethod == m) androidx.compose.material3.ButtonDefaults.buttonColors()
                                else androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(m, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = payNotes,
                        onValueChange = { payNotes = it },
                        label = { Text("Notes / Reference") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmountStr.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onRecordPayment(cust.id, amount, payMethod, payNotes)
                            customerForPayment = null
                        }
                    }
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Customer Detail / Ledger Dialog
    if (selectedCustomerForDetails != null) {
        val cust = selectedCustomerForDetails!!
        AlertDialog(
            onDismissRequest = { selectedCustomerForDetails = null },
            title = {
                Column {
                    Text(cust.name, fontWeight = FontWeight.Bold)
                    Text(cust.customerId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Phone: ${cust.phone.ifBlank { "N/A" }}")
                    Text("Address: ${cust.address.ifBlank { "N/A" }}")
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Purchases:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(cust.totalPurchases)}", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(cust.totalPaid)}", color = Color(0xFF1B873F), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Balance:", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                        Text("${settings.currency} ${"%,.0f".format(cust.remainingBalance)}", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                if (cust.remainingBalance > 0) {
                    Button(
                        onClick = {
                            val target = cust
                            selectedCustomerForDetails = null
                            customerForPayment = target
                        }
                    ) {
                        Text("Record Payment")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
