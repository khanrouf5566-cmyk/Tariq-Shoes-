package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StockTransactionDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        ProductVariantEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PaymentEntity::class,
        StockTransactionEntity::class,
        InvoiceEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tariq_shoes_pos.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val settingsDao = database.appSettingsDao()
            val productDao = database.productDao()
            val customerDao = database.customerDao()
            val supplierDao = database.supplierDao()
            val stockDao = database.stockTransactionDao()

            if (settingsDao.getSettingsDirect() == null) {
                settingsDao.insertOrUpdate(
                    AppSettingsEntity(
                        id = 1,
                        shopName = "Tariq Shoes & Wholesale Dealer",
                        shopPhone = "03121726620",
                        shopLocation = "Motorway site, Khyber Gate, Karachi",
                        currency = "Rs.",
                        invoicePrefix = "INV-",
                        productPrefix = "TS-",
                        nextInvoiceSeq = 1001,
                        nextProductSeq = 4,
                        nextCustomerSeq = 102,
                        nextSupplierSeq = 102,
                        lowStockLimit = 5,
                        adminPin = "1234",
                        allowNegativeStock = false,
                        language = "EN",
                        darkMode = "SYSTEM"
                    )
                )
            }

            // Seed initial sample products if empty
            if (productDao.getAllProductsDirect().isEmpty()) {
                val p1 = ProductEntity(
                    productId = "TS-0001",
                    name = "Peshawari Chappal",
                    category = "Peshawari Chappal",
                    design = "Khyber Special",
                    brand = "Tariq Shoes",
                    size = "42",
                    color = "Brown",
                    purchasePrice = 1500.0,
                    retailPrice = 2500.0,
                    wholesalePrice = 2200.0,
                    quantity = 20,
                    supplierName = "Charsadda Master Craftsman",
                    description = "Handcrafted genuine leather traditional Peshawari Chappal with tire sole.",
                    minStockLevel = 5,
                    barcode = "TS-0001",
                    isSample = true
                )
                val p2 = ProductEntity(
                    productId = "TS-0002",
                    name = "Black Peshawari Chappal",
                    category = "Peshawari Chappal",
                    design = "Zalmi Cut",
                    brand = "Tariq Shoes",
                    size = "41",
                    color = "Black",
                    purchasePrice = 1400.0,
                    retailPrice = 2400.0,
                    wholesalePrice = 2100.0,
                    quantity = 15,
                    supplierName = "Charsadda Master Craftsman",
                    description = "Premium matte black Zalmi Peshawari Chappal, high durability.",
                    minStockLevel = 5,
                    barcode = "TS-0002",
                    isSample = true
                )
                val p3 = ProductEntity(
                    productId = "TS-0003",
                    name = "Men's Casual Shoe",
                    category = "Casual",
                    design = "Classic Oxford Loafer",
                    brand = "Tariq Shoes",
                    size = "43",
                    color = "Black",
                    purchasePrice = 2000.0,
                    retailPrice = 3200.0,
                    wholesalePrice = 2800.0,
                    quantity = 10,
                    supplierName = "Lahore Footwear Industry",
                    description = "Comfortable daily wear casual shoe with cushioned soft insole.",
                    minStockLevel = 5,
                    barcode = "TS-0003",
                    isSample = true
                )

                productDao.insertProduct(p1)
                productDao.insertProduct(p2)
                productDao.insertProduct(p3)

                stockDao.insertTransaction(
                    StockTransactionEntity(
                        productId = "TS-0001",
                        productName = "Peshawari Chappal",
                        size = "42",
                        color = "Brown",
                        quantityChange = 20,
                        remainingStockAfter = 20,
                        type = "INITIAL",
                        referenceId = "INIT",
                        reason = "Opening stock sample"
                    )
                )
                stockDao.insertTransaction(
                    StockTransactionEntity(
                        productId = "TS-0002",
                        productName = "Black Peshawari Chappal",
                        size = "41",
                        color = "Black",
                        quantityChange = 15,
                        remainingStockAfter = 15,
                        type = "INITIAL",
                        referenceId = "INIT",
                        reason = "Opening stock sample"
                    )
                )
                stockDao.insertTransaction(
                    StockTransactionEntity(
                        productId = "TS-0003",
                        productName = "Men's Casual Shoe",
                        size = "43",
                        color = "Black",
                        quantityChange = 10,
                        remainingStockAfter = 10,
                        type = "INITIAL",
                        referenceId = "INIT",
                        reason = "Opening stock sample"
                    )
                )

                // Add default walk-in customer and wholesale customer
                customerDao.insertCustomer(
                    CustomerEntity(
                        customerId = "CUST-1001",
                        name = "Walk-in Customer",
                        phone = "03000000000",
                        address = "Counter Sale, Karachi",
                        totalPurchases = 0.0,
                        totalPaid = 0.0,
                        remainingBalance = 0.0
                    )
                )

                // Add sample supplier
                supplierDao.insertSupplier(
                    SupplierEntity(
                        supplierId = "SUP-101",
                        name = "Charsadda Crafts & Tannery",
                        phone = "03120000000",
                        address = "Bazaar Charsadda, KPK",
                        productsSupplied = "Peshawari Chappals, Leather footwear"
                    )
                )
            }
        }
    }
}
