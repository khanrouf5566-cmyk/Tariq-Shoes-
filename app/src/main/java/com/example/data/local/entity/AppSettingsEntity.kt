package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "Tariq Shoes & Wholesale Dealer",
    val shopPhone: String = "03121726620",
    val shopLocation: String = "Motorway site, Khyber Gate, Karachi",
    val currency: String = "Rs.",
    val invoicePrefix: String = "INV-",
    val productPrefix: String = "TS-",
    val nextInvoiceSeq: Int = 1001,
    val nextProductSeq: Int = 4, // 1 to 3 used by sample products
    val nextCustomerSeq: Int = 101,
    val nextSupplierSeq: Int = 101,
    val lowStockLimit: Int = 5,
    val adminPin: String = "1234",
    val allowNegativeStock: Boolean = false,
    val language: String = "EN", // "EN" or "UR"
    val darkMode: String = "SYSTEM" // "LIGHT", "DARK", "SYSTEM"
)
