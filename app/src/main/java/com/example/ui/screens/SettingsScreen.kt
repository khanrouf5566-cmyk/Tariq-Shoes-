package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AppSettingsEntity
import com.example.util.BackupRestoreUtil
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsEntity,
    lang: String,
    onSaveSettings: (
        shopName: String,
        shopPhone: String,
        shopLocation: String,
        currency: String,
        lowStockLimit: Int,
        allowNegativeStock: Boolean,
        adminPin: String,
        language: String,
        darkMode: String
    ) -> Unit,
    onBackupClick: (Context, onReady: (java.io.File) -> Unit) -> Unit,
    onRestoreFilePicked: (Context, Uri) -> Unit,
    onDeleteSampleProducts: () -> Unit
) {
    val context = LocalContext.current

    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var shopPhone by remember(settings) { mutableStateOf(settings.shopPhone) }
    var shopLocation by remember(settings) { mutableStateOf(settings.shopLocation) }
    var currency by remember(settings) { mutableStateOf(settings.currency) }
    var lowStockLimitStr by remember(settings) { mutableStateOf(settings.lowStockLimit.toString()) }
    var allowNegativeStock by remember(settings) { mutableStateOf(settings.allowNegativeStock) }
    var adminPin by remember(settings) { mutableStateOf(settings.adminPin) }
    var activeLang by remember(settings) { mutableStateOf(settings.language) }
    var activeTheme by remember(settings) { mutableStateOf(settings.darkMode) }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pickedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteSampleConfirmDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pickedRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Business Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text("Shop Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopLocation,
                        onValueChange = { shopLocation = it },
                        label = { Text("Shop Location / Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Currency Symbol") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lowStockLimitStr,
                            onValueChange = { lowStockLimitStr = it },
                            label = { Text("Low Stock Alert Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Language & Display Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.tr("language", lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = activeLang == "EN",
                            onClick = { activeLang = "EN" },
                            label = { Text("English") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = activeLang == "UR",
                            onClick = { activeLang = "UR" },
                            label = { Text("اردو (Urdu)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appearance Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                            FilterChip(
                                selected = activeTheme == mode,
                                onClick = { activeTheme = mode },
                                label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Security & POS Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.tr("admin_security", lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = adminPin,
                        onValueChange = { adminPin = it },
                        label = { Text("Admin PIN Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Negative Stock", fontWeight = FontWeight.Bold)
                            Text("If enabled, sales proceed even when stock is zero.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = allowNegativeStock,
                            onCheckedChange = { allowNegativeStock = it }
                        )
                    }
                }
            }
        }

        // Backup and Restore (Section 18)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Local Data Backup & Restore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Because this app works primarily offline, you can export and share complete database backups anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onBackupClick(context) { file ->
                                    BackupRestoreUtil.shareBackupFile(context, file)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("button_create_backup")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.tr("backup", lang))
                        }

                        FilledTonalButton(
                            onClick = {
                                filePickerLauncher.launch("application/json")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("button_restore_backup")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.tr("restore", lang))
                        }
                    }
                }
            }
        }

        // Sample Data Management (Section 33)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sample Test Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = Localization.tr("sample_data_info", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedButton(
                        onClick = { showDeleteSampleConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("delete_samples", lang))
                    }
                }
            }
        }

        // Save Settings Button
        item {
            Button(
                onClick = {
                    onSaveSettings(
                        shopName.trim(),
                        shopPhone.trim(),
                        shopLocation.trim(),
                        currency.trim(),
                        lowStockLimitStr.toIntOrNull() ?: 5,
                        allowNegativeStock,
                        adminPin.trim(),
                        activeLang,
                        activeTheme
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_save_settings"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // Confirmation for Data Restore
    if (showRestoreConfirmDialog && pickedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pickedRestoreUri = null
            },
            title = { Text(Localization.tr("restore", lang)) },
            text = { Text(Localization.tr("confirm_restore", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pickedRestoreUri!!
                        showRestoreConfirmDialog = false
                        pickedRestoreUri = null
                        onRestoreFilePicked(context, uri)
                    }
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pickedRestoreUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Sample Data Confirmation
    if (showDeleteSampleConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSampleConfirmDialog = false },
            title = { Text(Localization.tr("delete_samples", lang)) },
            text = { Text("Are you sure you want to remove the 3 sample test footwear items from database?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteSampleConfirmDialog = false
                        onDeleteSampleProducts()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove Samples")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSampleConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
