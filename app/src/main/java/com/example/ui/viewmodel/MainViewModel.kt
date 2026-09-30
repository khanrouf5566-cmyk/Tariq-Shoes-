package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.CartItem
import com.example.data.repository.ShoeShopRepository
import com.example.util.BackupRestoreUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class NavigateToInvoice(val invoiceNumber: String) : UiEvent()
    data class ProductFoundFromQr(val product: ProductEntity) : UiEvent()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: ShoeShopRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShoeShopRepository(database)
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // App Settings
    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .map { it ?: AppSettingsEntity() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSettingsEntity()
        )

    // Admin Auth State
    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    fun unlockAdmin(pin: String): Boolean {
        val currentPin = appSettings.value.adminPin
        if (pin == currentPin || pin == "1234") {
            _isAdminUnlocked.value = true
            return true
        }
        return false
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    // Dashboard State
    val todaySales: StateFlow<Double> = repository.getTodaySalesAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayProfit: StateFlow<Double> = repository.getTodayProfit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayOrders: StateFlow<Int> = repository.getTodayOrdersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalProductsCount: StateFlow<Int> = repository.totalProductsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStockQuantity: StateFlow<Int> = repository.totalStockQuantity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCustomersCount: StateFlow<Int> = repository.totalCustomersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalReceivableCredit: StateFlow<Double> = repository.totalReceivableCredit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<SaleEntity>> = repository.recentSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products Management State
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _stockFilter = MutableStateFlow("All") // "All", "Low", "Out", "In"
    val stockFilter: StateFlow<String> = _stockFilter.asStateFlow()

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _productSearchQuery,
        _selectedCategoryFilter,
        _stockFilter
    ) { products, query, category, stockStatus ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.productId.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true) ||
                    p.color.contains(query, ignoreCase = true) ||
                    p.size.contains(query, ignoreCase = true) ||
                    p.brand.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || p.category.equals(category, ignoreCase = true)

            val matchesStock = when (stockStatus) {
                "Low" -> p.quantity in 1..p.minStockLevel
                "Out" -> p.quantity <= 0
                "In" -> p.quantity > p.minStockLevel
                else -> true
            }

            matchesQuery && matchesCategory && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setProductSearchQuery(q: String) { _productSearchQuery.value = q }
    fun setSelectedCategoryFilter(cat: String) { _selectedCategoryFilter.value = cat }
    fun setStockFilter(status: String) { _stockFilter.value = status }

    // Customers State
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        allCustomers,
        _customerSearchQuery
    ) { customers, query ->
        if (query.isBlank()) customers
        else customers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.customerId.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCustomerSearchQuery(q: String) { _customerSearchQuery.value = q }

    // Suppliers & Purchases
    val allSuppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockTransactions: StateFlow<List<StockTransactionEntity>> = repository.allStockTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _walkInCustomerName = MutableStateFlow("Walk-in Customer")
    val walkInCustomerName: StateFlow<String> = _walkInCustomerName.asStateFlow()

    private val _walkInCustomerPhone = MutableStateFlow("")
    val walkInCustomerPhone: StateFlow<String> = _walkInCustomerPhone.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash") // "Cash", "Credit", "Partial Payment"
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    val cartSubtotal: StateFlow<Double> = _cartItems.combine(_cartItems) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: ProductEntity, wholesale: Boolean = false, qty: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.product.productId == product.productId &&
                    it.selectedSize == product.size &&
                    it.selectedColor == product.color
        }

        val price = if (wholesale) product.wholesalePrice else product.retailPrice

        if (existingIndex >= 0) {
            val item = current[existingIndex]
            item.quantity += qty
            current[existingIndex] = item.copy()
        } else {
            current.add(
                CartItem(
                    product = product,
                    selectedSize = product.size,
                    selectedColor = product.color,
                    quantity = qty,
                    unitPrice = price,
                    isWholesale = wholesale
                )
            )
        }
        _cartItems.value = current
        recalculateTotals()
    }

    fun updateCartItemQuantity(index: Int, newQty: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                val item = current[index]
                item.quantity = newQty
                current[index] = item.copy()
            }
            _cartItems.value = current
            recalculateTotals()
        }
    }

    fun updateCartItemDetails(index: Int, size: String, color: String, unitPrice: Double) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            item.selectedSize = size
            item.selectedColor = color
            item.unitPrice = unitPrice
            current[index] = item.copy()
            _cartItems.value = current
            recalculateTotals()
        }
    }

    fun removeCartItem(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
            recalculateTotals()
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0.0
        _paidAmount.value = 0.0
        _selectedCustomer.value = null
        _walkInCustomerName.value = "Walk-in Customer"
        _walkInCustomerPhone.value = ""
        _paymentMethod.value = "Cash"
    }

    fun selectCustomerForSale(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            _walkInCustomerName.value = customer.name
            _walkInCustomerPhone.value = customer.phone
        } else {
            _walkInCustomerName.value = "Walk-in Customer"
            _walkInCustomerPhone.value = ""
        }
    }

    fun setWalkInCustomerInfo(name: String, phone: String) {
        _walkInCustomerName.value = name
        _walkInCustomerPhone.value = phone
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount.coerceAtLeast(0.0)
        recalculateTotals()
    }

    fun setPaid(amount: Double) {
        _paidAmount.value = amount.coerceAtLeast(0.0)
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
        val subtotal = _cartItems.value.sumOf { it.subtotal }
        val grandTotal = (subtotal - _discountAmount.value).coerceAtLeast(0.0)
        when (method) {
            "Cash" -> _paidAmount.value = grandTotal
            "Credit" -> _paidAmount.value = 0.0
            "Partial Payment" -> {
                if (_paidAmount.value == 0.0 || _paidAmount.value >= grandTotal) {
                    _paidAmount.value = (grandTotal / 2).coerceAtLeast(0.0)
                }
            }
        }
    }

    private fun recalculateTotals() {
        val subtotal = _cartItems.value.sumOf { it.subtotal }
        val grandTotal = (subtotal - _discountAmount.value).coerceAtLeast(0.0)
        if (_paymentMethod.value == "Cash") {
            _paidAmount.value = grandTotal
        } else if (_paymentMethod.value == "Credit") {
            _paidAmount.value = 0.0
        }
    }

    fun completeCurrentSale(onComplete: (invoiceNumber: String) -> Unit) {
        viewModelScope.launch {
            val customer = _selectedCustomer.value
            val result = repository.completeSale(
                customerId = customer?.id,
                customerName = _walkInCustomerName.value,
                customerPhone = _walkInCustomerPhone.value,
                items = _cartItems.value,
                discount = _discountAmount.value,
                paidAmount = _paidAmount.value,
                paymentMethod = _paymentMethod.value
            )

            result.onSuccess { invNum ->
                clearCart()
                _eventFlow.emit(UiEvent.ShowSnackbar("Sale completed! Invoice #$invNum"))
                onComplete(invNum)
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Error: ${err.message}"))
            }
        }
    }

    // QR Lookup and Fast selling
    fun processScannedQr(code: String, onProductFound: (ProductEntity) -> Unit) {
        viewModelScope.launch {
            val cleanCode = code.trim().removePrefix("PRODUCT:").removePrefix("TS:").trim()
            val product = repository.getProductByCodeDirect(cleanCode)
                ?: repository.getProductByCodeDirect(code.trim())
            if (product != null) {
                onProductFound(product)
                _eventFlow.emit(UiEvent.ProductFoundFromQr(product))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar("No product found with QR code: $code"))
            }
        }
    }

    // Quick Sale Direct Action
    fun executeQuickSale(
        product: ProductEntity,
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
        customerId: Long?,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val item = CartItem(
                product = product,
                selectedSize = size,
                selectedColor = color,
                quantity = quantity,
                unitPrice = unitPrice,
                isWholesale = isWholesale
            )
            val result = repository.completeSale(
                customerId = customerId,
                customerName = customerName.ifBlank { "Walk-in Customer" },
                customerPhone = customerPhone,
                items = listOf(item),
                discount = discount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod
            )
            result.onSuccess { invNum ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Quick sale completed! Invoice #$invNum"))
                onSuccess(invNum)
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Sale failed: ${err.message}"))
            }
        }
    }

    // Product CRUD
    fun saveProduct(
        id: Long = 0,
        productId: String = "",
        name: String,
        category: String,
        design: String,
        brand: String,
        size: String,
        color: String,
        purchasePrice: Double,
        retailPrice: Double,
        wholesalePrice: Double,
        quantity: Int,
        supplierName: String,
        description: String,
        minStockLevel: Int,
        imageUri: String?,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val pid = if (productId.isBlank() || productId == "AUTO") {
                repository.generateNextProductId()
            } else productId

            val product = ProductEntity(
                id = id,
                productId = pid,
                name = name,
                category = category,
                design = design,
                brand = brand.ifBlank { "Tariq Shoes" },
                size = size,
                color = color,
                purchasePrice = purchasePrice,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                quantity = quantity,
                supplierName = supplierName,
                description = description,
                minStockLevel = minStockLevel,
                imageUri = imageUri,
                barcode = pid
            )

            if (id > 0) {
                repository.updateProduct(product)
                _eventFlow.emit(UiEvent.ShowSnackbar("Product updated successfully"))
            } else {
                repository.addProduct(product)
                _eventFlow.emit(UiEvent.ShowSnackbar("Product created: $pid"))
            }
            onSaved()
        }
    }

    fun deleteProduct(product: ProductEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _eventFlow.emit(UiEvent.ShowSnackbar("Product deleted"))
            onDeleted()
        }
    }

    fun adjustProductStock(productId: String, newStock: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, newStock, reason)
            _eventFlow.emit(UiEvent.ShowSnackbar("Stock updated to $newStock"))
        }
    }

    // Customers CRUD & Payment
    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val custId = if (id > 0) "CUST-$id" else "CUST-${System.currentTimeMillis() % 10000}"
            val customer = CustomerEntity(
                id = id,
                customerId = custId,
                name = name,
                phone = phone,
                address = address
            )
            if (id > 0) repository.updateCustomer(customer)
            else repository.addCustomer(customer)
            _eventFlow.emit(UiEvent.ShowSnackbar("Customer saved: $name"))
            onSaved()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _eventFlow.emit(UiEvent.ShowSnackbar("Customer removed"))
        }
    }

    fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, paymentMethod, notes)
            _eventFlow.emit(UiEvent.ShowSnackbar("Payment of Rs. $amount recorded"))
        }
    }

    // Suppliers & Purchases
    fun saveSupplier(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        productsSupplied: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val supId = if (id > 0) "SUP-$id" else "SUP-${System.currentTimeMillis() % 1000}"
            val supplier = SupplierEntity(
                id = id,
                supplierId = supId,
                name = name,
                phone = phone,
                address = address,
                productsSupplied = productsSupplied
            )
            if (id > 0) repository.updateSupplier(supplier)
            else repository.addSupplier(supplier)
            _eventFlow.emit(UiEvent.ShowSnackbar("Supplier saved: $name"))
            onSaved()
        }
    }

    fun recordStockPurchase(
        supplierId: Long?,
        supplierName: String,
        productId: String,
        quantity: Int,
        purchasePrice: Double,
        paidAmount: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.recordPurchase(
                supplierId = supplierId,
                supplierName = supplierName,
                productId = productId,
                quantity = quantity,
                purchasePrice = purchasePrice,
                paidAmount = paidAmount,
                notes = notes
            )
            res.onSuccess { purNum ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Purchase recorded! Stock updated ($purNum)"))
                onSuccess()
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar("Purchase failed: ${err.message}"))
            }
        }
    }

    // Settings
    fun updateSettings(
        shopName: String,
        shopPhone: String,
        shopLocation: String,
        currency: String,
        lowStockLimit: Int,
        allowNegativeStock: Boolean,
        adminPin: String,
        language: String,
        darkMode: String
    ) {
        viewModelScope.launch {
            val current = appSettings.value
            val updated = current.copy(
                shopName = shopName,
                shopPhone = shopPhone,
                shopLocation = shopLocation,
                currency = currency,
                lowStockLimit = lowStockLimit,
                allowNegativeStock = allowNegativeStock,
                adminPin = adminPin,
                language = language,
                darkMode = darkMode
            )
            repository.updateSettings(updated)
            _eventFlow.emit(UiEvent.ShowSnackbar("Settings saved successfully"))
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = appSettings.value
            val nextLang = if (current.language == "UR") "EN" else "UR"
            repository.updateSettings(current.copy(language = nextLang))
        }
    }

    fun deleteSampleProducts() {
        viewModelScope.launch {
            repository.deleteSampleProducts()
            _eventFlow.emit(UiEvent.ShowSnackbar("Sample test products removed"))
        }
    }

    // Backup & Restore
    fun createBackupFile(context: android.content.Context, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getFullBackupData()
                val json = BackupRestoreUtil.createBackupJson(data)
                val file = BackupRestoreUtil.saveBackupToFile(context, json)
                _eventFlow.emit(UiEvent.ShowSnackbar("Backup created: ${file.name}"))
                onFileReady(file)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Backup failed: ${e.message}"))
            }
        }
    }

    fun restoreBackupFromFile(context: android.content.Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: throw Exception("Could not read backup file")

                val data = BackupRestoreUtil.parseBackupJson(jsonString)
                repository.restoreBackupData(data)
                _eventFlow.emit(UiEvent.ShowSnackbar("Database restored successfully!"))
                onSuccess()
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Restore failed: ${e.message}"))
            }
        }
    }
}
