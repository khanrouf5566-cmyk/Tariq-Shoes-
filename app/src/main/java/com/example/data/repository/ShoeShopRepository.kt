package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class CartItem(
    val product: ProductEntity,
    var selectedSize: String,
    var selectedColor: String,
    var quantity: Int,
    var unitPrice: Double,
    var isWholesale: Boolean = false
) {
    val subtotal: Double get() = quantity * unitPrice
    val totalCost: Double get() = quantity * product.purchasePrice
    val grossProfit: Double get() = subtotal - totalCost
}

class ShoeShopRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val supplierDao = database.supplierDao()
    private val saleDao = database.saleDao()
    private val purchaseDao = database.purchaseDao()
    private val paymentDao = database.paymentDao()
    private val stockDao = database.stockTransactionDao()
    private val invoiceDao = database.invoiceDao()
    private val settingsDao = database.appSettingsDao()

    // Flow streams
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val totalProductsCount: Flow<Int> = productDao.getTotalProductsCount()
    val totalStockQuantity: Flow<Int> = productDao.getTotalStockQuantity()

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalCustomersCount: Flow<Int> = customerDao.getTotalCustomersCount()
    val totalReceivableCredit: Flow<Double> = customerDao.getTotalReceivableCredit()

    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val totalSuppliersCount: Flow<Int> = supplierDao.getTotalSuppliersCount()

    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val recentSales: Flow<List<SaleEntity>> = saleDao.getRecentSales(15)
    val totalSalesAmount: Flow<Double> = saleDao.getTotalSalesAmount()
    val totalProfitAmount: Flow<Double> = saleDao.getTotalProfitAmount()

    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allStockTransactions: Flow<List<StockTransactionEntity>> = stockDao.getAllTransactions()
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val appSettings: Flow<AppSettingsEntity?> = settingsDao.getSettings()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)
    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)
    fun getProductByCode(code: String): Flow<ProductEntity?> = productDao.getProductByCode(code)
    suspend fun getProductByCodeDirect(code: String): ProductEntity? = productDao.getProductByCodeDirect(code)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>> = saleDao.getSalesByCustomer(customerId)
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsByCustomer(customerId)

    fun getItemsForInvoice(invoiceNumber: String): Flow<List<SaleItemEntity>> = saleDao.getItemsForInvoice(invoiceNumber)
    suspend fun getItemsForInvoiceDirect(invoiceNumber: String): List<SaleItemEntity> = saleDao.getItemsForInvoiceDirect(invoiceNumber)
    fun getInvoiceByNumber(invoiceNumber: String): Flow<InvoiceEntity?> = invoiceDao.getInvoiceByNumber(invoiceNumber)
    suspend fun getInvoiceByNumberDirect(invoiceNumber: String): InvoiceEntity? = invoiceDao.getInvoiceByNumberDirect(invoiceNumber)

    // Today's statistics helper
    fun getTodayStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    fun getTodaySalesAmount(): Flow<Double> {
        val (start, end) = getTodayStartAndEnd()
        return saleDao.getSalesAmountForPeriod(start, end)
    }

    fun getTodayProfit(): Flow<Double> {
        val (start, end) = getTodayStartAndEnd()
        return saleDao.getProfitForPeriod(start, end)
    }

    fun getTodayOrdersCount(): Flow<Int> {
        val (start, end) = getTodayStartAndEnd()
        return saleDao.getOrdersCountForPeriod(start, end)
    }

    fun getSalesForPeriod(start: Long, end: Long): Flow<List<SaleEntity>> = saleDao.getSalesForPeriod(start, end)

    suspend fun getSettingsDirect(): AppSettingsEntity {
        return settingsDao.getSettingsDirect() ?: AppSettingsEntity()
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun generateNextProductId(): String {
        val settings = getSettingsDirect()
        val seq = settings.nextProductSeq
        val formatted = "${settings.productPrefix}%04d".format(seq)
        settingsDao.incrementProductSeq()
        return formatted
    }

    suspend fun generateNextInvoiceNumber(): String {
        val settings = getSettingsDirect()
        val seq = settings.nextInvoiceSeq
        val formatted = "${settings.invoicePrefix}%04d".format(seq)
        settingsDao.incrementInvoiceSeq()
        return formatted
    }

    suspend fun addProduct(product: ProductEntity): Long {
        val id = productDao.insertProduct(product)
        stockDao.insertTransaction(
            StockTransactionEntity(
                productId = product.productId,
                productName = product.name,
                size = product.size,
                color = product.color,
                quantityChange = product.quantity,
                remainingStockAfter = product.quantity,
                type = "INITIAL",
                referenceId = product.productId,
                reason = "Initial product stock entry"
            )
        )
        return id
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun adjustStock(productId: String, newStock: Int, reason: String) {
        val product = productDao.getProductByCodeDirect(productId) ?: return
        val diff = newStock - product.quantity
        productDao.updateStock(productId, newStock)
        stockDao.insertTransaction(
            StockTransactionEntity(
                productId = productId,
                productName = product.name,
                size = product.size,
                color = product.color,
                quantityChange = diff,
                remainingStockAfter = newStock,
                type = "ADJUSTMENT",
                referenceId = "ADJ",
                reason = reason.ifBlank { "Manual stock adjustment" }
            )
        )
    }

    suspend fun addCustomer(customer: CustomerEntity): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String
    ): Long {
        val customer = customerDao.getCustomerByIdDirect(customerId) ?: return -1
        val settings = getSettingsDirect()
        val receiptNumber = "REC-${System.currentTimeMillis() % 100000}"

        val payment = PaymentEntity(
            receiptNumber = receiptNumber,
            customerId = customerId,
            customerName = customer.name,
            amount = amount,
            paymentMethod = paymentMethod,
            notes = notes
        )
        val paymentId = paymentDao.insertPayment(payment)
        customerDao.applyPaymentToCustomer(customerId, amount)
        return paymentId
    }

    suspend fun addSupplier(supplier: SupplierEntity): Long {
        return supplierDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: SupplierEntity) {
        supplierDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: SupplierEntity) {
        supplierDao.deleteSupplier(supplier)
    }

    // Execute Purchase / Stock In
    suspend fun recordPurchase(
        supplierId: Long?,
        supplierName: String,
        productId: String,
        quantity: Int,
        purchasePrice: Double,
        paidAmount: Double,
        notes: String
    ): Result<String> {
        val product = productDao.getProductByCodeDirect(productId)
            ?: return Result.failure(Exception("Product with ID $productId not found"))

        val totalCost = quantity * purchasePrice
        val remaining = totalCost - paidAmount
        val purchaseNumber = "PUR-${System.currentTimeMillis() % 100000}"

        val purchase = PurchaseEntity(
            purchaseNumber = purchaseNumber,
            supplierId = supplierId,
            supplierName = supplierName.ifBlank { "Direct Supplier" },
            productId = productId,
            productName = product.name,
            size = product.size,
            color = product.color,
            quantity = quantity,
            purchasePrice = purchasePrice,
            totalCost = totalCost,
            paidAmount = paidAmount,
            remainingAmount = remaining,
            notes = notes
        )

        val newStock = product.quantity + quantity
        productDao.updateStock(productId, newStock)
        purchaseDao.insertPurchase(purchase)

        stockDao.insertTransaction(
            StockTransactionEntity(
                productId = productId,
                productName = product.name,
                size = product.size,
                color = product.color,
                quantityChange = quantity,
                remainingStockAfter = newStock,
                type = "PURCHASE",
                referenceId = purchaseNumber,
                reason = "Purchased from $supplierName"
            )
        )

        if (supplierId != null && supplierId > 0) {
            supplierDao.addPurchaseToSupplier(supplierId, totalCost, remaining)
        }

        return Result.success(purchaseNumber)
    }

    // Execute Complete POS Sale
    suspend fun completeSale(
        customerId: Long?,
        customerName: String,
        customerPhone: String,
        items: List<CartItem>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String = ""
    ): Result<String> {
        if (items.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        val settings = getSettingsDirect()

        // Validate stock
        for (item in items) {
            val currentProduct = productDao.getProductByCodeDirect(item.product.productId)
                ?: return Result.failure(Exception("Product ${item.product.name} not found"))
            if (!settings.allowNegativeStock && currentProduct.quantity < item.quantity) {
                return Result.failure(
                    Exception("Insufficient stock for ${item.product.name}. Available: ${currentProduct.quantity}, Requested: ${item.quantity}")
                )
            }
        }

        val subtotal = items.sumOf { it.subtotal }
        val grandTotal = (subtotal - discount).coerceAtLeast(0.0)
        val remaining = (grandTotal - paidAmount).coerceAtLeast(0.0)
        val totalCost = items.sumOf { it.totalCost }
        val totalProfit = (grandTotal - totalCost)

        val invoiceNumber = generateNextInvoiceNumber()

        // Execute inside Room transaction
        val sale = SaleEntity(
            invoiceNumber = invoiceNumber,
            customerId = customerId,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone,
            subtotal = subtotal,
            discount = discount,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            remainingAmount = remaining,
            profit = totalProfit,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val saleId = saleDao.insertSale(sale)

        val saleItems = items.map { item ->
            // Distribute discount proportionally across items for profit calculation
            val itemPortion = if (subtotal > 0) item.subtotal / subtotal else 0.0
            val itemDiscount = discount * itemPortion
            val itemEffectiveTotal = item.subtotal - itemDiscount
            val itemProfit = itemEffectiveTotal - item.totalCost

            SaleItemEntity(
                saleId = saleId,
                invoiceNumber = invoiceNumber,
                productId = item.product.productId,
                productName = item.product.name,
                size = item.selectedSize,
                color = item.selectedColor,
                quantity = item.quantity,
                purchasePrice = item.product.purchasePrice,
                unitPrice = item.unitPrice,
                subtotal = item.subtotal,
                profit = itemProfit
            )
        }

        saleDao.insertSaleItems(saleItems)

        // Deduct stock for each item & log transaction
        for (item in items) {
            val prod = productDao.getProductByCodeDirect(item.product.productId)!!
            val newQty = prod.quantity - item.quantity
            productDao.updateStock(item.product.productId, newQty)

            stockDao.insertTransaction(
                StockTransactionEntity(
                    productId = item.product.productId,
                    productName = item.product.name,
                    size = item.selectedSize,
                    color = item.selectedColor,
                    quantityChange = -item.quantity,
                    remainingStockAfter = newQty,
                    type = "SALE",
                    referenceId = invoiceNumber,
                    reason = "Sale to ${sale.customerName}"
                )
            )
        }

        // Customer Ledger Update
        if (customerId != null && customerId > 0) {
            customerDao.addPurchaseToCustomer(customerId, grandTotal, remaining)
            if (paidAmount > 0) {
                paymentDao.insertPayment(
                    PaymentEntity(
                        receiptNumber = "PAY-$invoiceNumber",
                        customerId = customerId,
                        customerName = sale.customerName,
                        saleId = saleId,
                        invoiceNumber = invoiceNumber,
                        amount = paidAmount,
                        paymentMethod = paymentMethod,
                        notes = "Payment for $invoiceNumber"
                    )
                )
            }
        }

        // Invoice record
        invoiceDao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = invoiceNumber,
                saleId = saleId,
                customerName = sale.customerName,
                customerPhone = sale.customerPhone,
                subtotal = subtotal,
                discount = discount,
                grandTotal = grandTotal,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                paymentMethod = paymentMethod
            )
        )

        return Result.success(invoiceNumber)
    }

    suspend fun deleteSampleProducts() {
        productDao.deleteSampleProducts()
    }

    // Direct data export for backup
    suspend fun getFullBackupData(): BackupData {
        return BackupData(
            products = productDao.getAllProductsDirect(),
            customers = customerDao.getAllCustomersDirect(),
            suppliers = supplierDao.getAllSuppliersDirect(),
            sales = saleDao.getAllSalesDirect(),
            saleItems = saleDao.getAllSaleItemsDirect(),
            purchases = purchaseDao.getAllPurchasesDirect(),
            payments = paymentDao.getAllPaymentsDirect(),
            stockTransactions = stockDao.getAllTransactionsDirect(),
            invoices = invoiceDao.getAllInvoicesDirect(),
            settings = settingsDao.getSettingsDirect() ?: AppSettingsEntity()
        )
    }

    suspend fun restoreBackupData(data: BackupData) {
        productDao.insertProducts(data.products)
        customerDao.insertCustomers(data.customers)
        supplierDao.insertSuppliers(data.suppliers)
        data.sales.forEach { saleDao.insertSale(it) }
        saleDao.insertSaleItems(data.saleItems)
        data.purchases.forEach { purchaseDao.insertPurchase(it) }
        data.payments.forEach { paymentDao.insertPayment(it) }
        data.stockTransactions.forEach { stockDao.insertTransaction(it) }
        data.invoices.forEach { invoiceDao.insertInvoice(it) }
        settingsDao.insertOrUpdate(data.settings)
    }
}

data class BackupData(
    val products: List<ProductEntity>,
    val customers: List<CustomerEntity>,
    val suppliers: List<SupplierEntity>,
    val sales: List<SaleEntity>,
    val saleItems: List<SaleItemEntity>,
    val purchases: List<PurchaseEntity>,
    val payments: List<PaymentEntity>,
    val stockTransactions: List<StockTransactionEntity>,
    val invoices: List<InvoiceEntity>,
    val settings: AppSettingsEntity
)
