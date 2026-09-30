package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["productId", "size", "color"], unique = true)
    ]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val purchasePrice: Double,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val barcode: String = "$productId-$size-$color"
)
