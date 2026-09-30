package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["productId"], unique = true),
        Index(value = ["category"]),
        Index(value = ["brand"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String, // e.g. "TS-0001"
    val name: String,
    val category: String, // e.g. "Peshawari Chappal", "Casual", "Formal", "Sandals", "Sneakers"
    val design: String = "",
    val brand: String = "Tariq Shoes",
    val size: String, // "42", "41", etc.
    val color: String, // "Brown", "Black", etc.
    val purchasePrice: Double,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val quantity: Int,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val imageUri: String? = null,
    val description: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val minStockLevel: Int = 5,
    val barcode: String = productId,
    val isSample: Boolean = false
)
