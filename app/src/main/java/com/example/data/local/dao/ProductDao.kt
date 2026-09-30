package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE productId = :code OR barcode = :code LIMIT 1")
    fun getProductByCode(code: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE productId = :code OR barcode = :code LIMIT 1")
    suspend fun getProductByCodeDirect(code: String): ProductEntity?

    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%' 
           OR productId LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR brand LIKE '%' || :query || '%' 
           OR color LIKE '%' || :query || '%' 
           OR size LIKE '%' || :query || '%' 
           OR supplierName LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE quantity <= minStockLevel ORDER BY quantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM products")
    fun getTotalStockQuantity(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE products SET quantity = :newQuantity WHERE productId = :productId")
    suspend fun updateStock(productId: String, newQuantity: Int)

    @Query("DELETE FROM products WHERE isSample = 1")
    suspend fun deleteSampleProducts()

    @Query("SELECT * FROM products")
    suspend fun getAllProductsDirect(): List<ProductEntity>
}
