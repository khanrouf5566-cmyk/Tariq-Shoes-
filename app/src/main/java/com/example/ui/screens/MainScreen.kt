package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ProductEntity
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.Localization
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ScreenNav {
    object Dashboard : ScreenNav()
    object Products : ScreenNav()
    object Sales : ScreenNav()
    object Customers : ScreenNav()
    object Reports : ScreenNav()
    object Settings : ScreenNav()
    object AddProduct : ScreenNav()
    data class EditProduct(val product: ProductEntity) : ScreenNav()
    data class ProductDetail(val productId: Long) : ScreenNav()
    object QrScanner : ScreenNav()
    data class QuickSale(val product: ProductEntity) : ScreenNav()
    data class Invoice(val invoiceNumber: String) : ScreenNav()
    object Purchases : ScreenNav()
    object StockManagement : ScreenNav()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf<ScreenNav>(ScreenNav.Dashboard) }
    var showAdminPinDialog by remember { mutableStateOf(false) }

    val settings by viewModel.appSettings.collectAsState()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val lang = settings.language

    // Collect UI Events
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.NavigateToInvoice -> {
                    currentScreen = ScreenNav.Invoice(event.invoiceNumber)
                }
                is UiEvent.ProductFoundFromQr -> {}
            }
        }
    }

    // Handle back button for secondary screens
    BackHandler(enabled = currentScreen !is ScreenNav.Dashboard) {
        currentScreen = when (currentScreen) {
            is ScreenNav.AddProduct, is ScreenNav.EditProduct, is ScreenNav.ProductDetail -> ScreenNav.Products
            is ScreenNav.QrScanner -> ScreenNav.Dashboard
            is ScreenNav.QuickSale -> ScreenNav.Products
            is ScreenNav.Invoice -> ScreenNav.Sales
            is ScreenNav.Purchases, is ScreenNav.StockManagement -> ScreenNav.Dashboard
            else -> ScreenNav.Dashboard
        }
    }

    // Live StateFlow collections
    val todaySales by viewModel.todaySales.collectAsState()
    val todayProfit by viewModel.todayProfit.collectAsState()
    val todayOrders by viewModel.todayOrders.collectAsState()
    val totalProducts by viewModel.totalProductsCount.collectAsState()
    val totalStock by viewModel.totalStockQuantity.collectAsState()
    val totalCustomers by viewModel.totalCustomersCount.collectAsState()
    val totalReceivable by viewModel.totalReceivableCredit.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val recentSalesList by viewModel.recentSales.collectAsState()

    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val productSearchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStockFilter by viewModel.stockFilter.collectAsState()

    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsState()

    val allPurchases by viewModel.allPurchases.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val allStockTransactions by viewModel.allStockTransactions.collectAsState()
    val allSales by viewModel.allSales.collectAsState()

    // POS Cart
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val walkInName by viewModel.walkInCustomerName.collectAsState()
    val walkInPhone by viewModel.walkInCustomerPhone.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()

    val isTopLevelScreen = currentScreen in listOf(
        ScreenNav.Dashboard,
        ScreenNav.Products,
        ScreenNav.Sales,
        ScreenNav.Customers,
        ScreenNav.Reports,
        ScreenNav.Settings
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isTopLevelScreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = Localization.tr("app_title", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Wholesale & Offline POS • ${settings.currency}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Language Toggle button (EN / UR)
                        IconButton(onClick = { viewModel.toggleLanguage() }) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (lang == "UR") "اردو" else "EN",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Admin Security Lock / Unlock
                        IconButton(
                            onClick = {
                                if (isAdminUnlocked) {
                                    viewModel.lockAdmin()
                                } else {
                                    showAdminPinDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isAdminUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Admin Security",
                                tint = if (isAdminUnlocked) Color(0xFF1B873F) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Dashboard,
                        onClick = { currentScreen = ScreenNav.Dashboard },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(Localization.tr("dashboard", lang)) },
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Products,
                        onClick = { currentScreen = ScreenNav.Products },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Products") },
                        label = { Text(Localization.tr("products", lang)) },
                        modifier = Modifier.testTag("nav_products")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Sales,
                        onClick = { currentScreen = ScreenNav.Sales },
                        icon = {
                            if (cartItems.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${cartItems.sumOf { it.quantity }}") } }) {
                                    Icon(Icons.Default.PointOfSale, contentDescription = "Sales")
                                }
                            } else {
                                Icon(Icons.Default.PointOfSale, contentDescription = "Sales")
                            }
                        },
                        label = { Text(Localization.tr("sales", lang)) },
                        modifier = Modifier.testTag("nav_sales")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Customers,
                        onClick = { currentScreen = ScreenNav.Customers },
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { Text(Localization.tr("customers", lang)) },
                        modifier = Modifier.testTag("nav_customers")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Reports,
                        onClick = { currentScreen = ScreenNav.Reports },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text(Localization.tr("reports", lang)) },
                        modifier = Modifier.testTag("nav_reports")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Settings,
                        onClick = { currentScreen = ScreenNav.Settings },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text(Localization.tr("settings", lang)) },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val nav = currentScreen) {
                is ScreenNav.Dashboard -> {
                    DashboardScreen(
                        settings = settings,
                        todaySales = todaySales,
                        todayProfit = todayProfit,
                        todayOrders = todayOrders,
                        totalProducts = totalProducts,
                        totalStock = totalStock,
                        totalCustomers = totalCustomers,
                        totalReceivable = totalReceivable,
                        lowStockList = lowStockList,
                        recentSalesList = recentSalesList,
                        lang = lang,
                        onNavigateToAddProduct = { currentScreen = ScreenNav.AddProduct },
                        onNavigateToNewSale = { currentScreen = ScreenNav.Sales },
                        onNavigateToScanQr = { currentScreen = ScreenNav.QrScanner },
                        onNavigateToAddCustomer = { currentScreen = ScreenNav.Customers },
                        onNavigateToPurchaseStock = { currentScreen = ScreenNav.Purchases },
                        onNavigateToReports = { currentScreen = ScreenNav.Reports },
                        onNavigateToInvoice = { inv -> currentScreen = ScreenNav.Invoice(inv) },
                        onNavigateToProductDetail = { pid -> currentScreen = ScreenNav.ProductDetail(pid) }
                    )
                }

                is ScreenNav.Products -> {
                    ProductsScreen(
                        products = filteredProducts,
                        searchQuery = productSearchQuery,
                        selectedCategory = selectedCategory,
                        selectedStockFilter = selectedStockFilter,
                        settings = settings,
                        lang = lang,
                        onSearchQueryChanged = { viewModel.setProductSearchQuery(it) },
                        onCategorySelected = { viewModel.setSelectedCategoryFilter(it) },
                        onStockFilterSelected = { viewModel.setStockFilter(it) },
                        onProductClick = { pid -> currentScreen = ScreenNav.ProductDetail(pid) },
                        onAddProductClick = { currentScreen = ScreenNav.AddProduct },
                        onQuickSellClick = { prod -> currentScreen = ScreenNav.QuickSale(prod) },
                        onShowQrDialog = { prod -> currentScreen = ScreenNav.ProductDetail(prod.id) }
                    )
                }

                is ScreenNav.Sales -> {
                    PosSaleScreen(
                        cartItems = cartItems,
                        allProducts = allProducts,
                        customers = allCustomers,
                        selectedCustomer = selectedCustomer,
                        walkInName = walkInName,
                        walkInPhone = walkInPhone,
                        discountAmount = discountAmount,
                        paidAmount = paidAmount,
                        paymentMethod = paymentMethod,
                        settings = settings,
                        lang = lang,
                        onAddToCart = { prod, wholesale, qty -> viewModel.addToCart(prod, wholesale, qty) },
                        onUpdateQuantity = { idx, qty -> viewModel.updateCartItemQuantity(idx, qty) },
                        onRemoveItem = { idx -> viewModel.removeCartItem(idx) },
                        onClearCart = { viewModel.clearCart() },
                        onSelectCustomer = { cust -> viewModel.selectCustomerForSale(cust) },
                        onSetWalkInInfo = { name, phone -> viewModel.setWalkInCustomerInfo(name, phone) },
                        onSetDiscount = { disc -> viewModel.setDiscount(disc) },
                        onSetPaid = { paid -> viewModel.setPaid(paid) },
                        onSetPaymentMethod = { method -> viewModel.setPaymentMethod(method) },
                        onScanQrClick = { currentScreen = ScreenNav.QrScanner },
                        onCompleteSale = {
                            viewModel.completeCurrentSale { invoiceNum ->
                                currentScreen = ScreenNav.Invoice(invoiceNum)
                            }
                        }
                    )
                }

                is ScreenNav.Customers -> {
                    CustomersScreen(
                        customers = filteredCustomers,
                        searchQuery = customerSearchQuery,
                        settings = settings,
                        lang = lang,
                        onSearchChanged = { viewModel.setCustomerSearchQuery(it) },
                        onSaveCustomer = { id, name, phone, address ->
                            viewModel.saveCustomer(id, name, phone, address) {}
                        },
                        onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) },
                        onRecordPayment = { custId, amt, method, notes ->
                            viewModel.recordCustomerPayment(custId, amt, method, notes)
                        }
                    )
                }

                is ScreenNav.Reports -> {
                    ReportsScreen(
                        sales = allSales,
                        settings = settings,
                        lang = lang
                    )
                }

                is ScreenNav.Settings -> {
                    SettingsScreen(
                        settings = settings,
                        lang = lang,
                        onSaveSettings = { name, phone, loc, curr, limit, allowNeg, pin, l, theme ->
                            viewModel.updateSettings(name, phone, loc, curr, limit, allowNeg, pin, l, theme)
                        },
                        onBackupClick = { ctx, onReady ->
                            viewModel.createBackupFile(ctx, onReady)
                        },
                        onRestoreFilePicked = { ctx, uri ->
                            viewModel.restoreBackupFromFile(ctx, uri) {}
                        },
                        onDeleteSampleProducts = {
                            viewModel.deleteSampleProducts()
                        }
                    )
                }

                is ScreenNav.AddProduct -> {
                    AddEditProductScreen(
                        productToEdit = null,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Products },
                        onSave = { id, pid, name, cat, des, brand, sz, clr, pur, ret, ws, qty, sup, desc, minStk, img ->
                            viewModel.saveProduct(
                                id = id,
                                productId = pid,
                                name = name,
                                category = cat,
                                design = des,
                                brand = brand,
                                size = sz,
                                color = clr,
                                purchasePrice = pur,
                                retailPrice = ret,
                                wholesalePrice = ws,
                                quantity = qty,
                                supplierName = sup,
                                description = desc,
                                minStockLevel = minStk,
                                imageUri = img,
                                onSaved = { currentScreen = ScreenNav.Products }
                            )
                        }
                    )
                }

                is ScreenNav.EditProduct -> {
                    AddEditProductScreen(
                        productToEdit = nav.product,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Products },
                        onSave = { id, pid, name, cat, des, brand, sz, clr, pur, ret, ws, qty, sup, desc, minStk, img ->
                            viewModel.saveProduct(
                                id = id,
                                productId = pid,
                                name = name,
                                category = cat,
                                design = des,
                                brand = brand,
                                size = sz,
                                color = clr,
                                purchasePrice = pur,
                                retailPrice = ret,
                                wholesalePrice = ws,
                                quantity = qty,
                                supplierName = sup,
                                description = desc,
                                minStockLevel = minStk,
                                imageUri = img,
                                onSaved = { currentScreen = ScreenNav.Products }
                            )
                        }
                    )
                }

                is ScreenNav.ProductDetail -> {
                    val prod = allProducts.firstOrNull { it.id == nav.productId }
                    ProductDetailScreen(
                        product = prod,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Products },
                        onEdit = { currentScreen = ScreenNav.EditProduct(it) },
                        onQuickSell = { currentScreen = ScreenNav.QuickSale(it) },
                        onAdjustStock = { pid, newStock, reason ->
                            viewModel.adjustProductStock(pid, newStock, reason)
                        },
                        onDelete = {
                            viewModel.deleteProduct(it) {
                                currentScreen = ScreenNav.Products
                            }
                        }
                    )
                }

                is ScreenNav.QrScanner -> {
                    QrScannerScreen(
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Dashboard },
                        onProductFound = { code, onFound ->
                            viewModel.processScannedQr(code, onFound)
                        },
                        onViewProduct = { pid -> currentScreen = ScreenNav.ProductDetail(pid) },
                        onEditProduct = { prod -> currentScreen = ScreenNav.EditProduct(prod) },
                        onSellNow = { prod -> currentScreen = ScreenNav.QuickSale(prod) },
                        onAddStock = { prod -> currentScreen = ScreenNav.Purchases }
                    )
                }

                is ScreenNav.QuickSale -> {
                    QuickSaleScreen(
                        product = nav.product,
                        customers = allCustomers,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Products },
                        onCompleteSale = { sz, clr, qty, ws, price, disc, paid, method, cName, cPhone, cId ->
                            viewModel.executeQuickSale(
                                product = nav.product,
                                size = sz,
                                color = clr,
                                quantity = qty,
                                isWholesale = ws,
                                unitPrice = price,
                                discount = disc,
                                paidAmount = paid,
                                paymentMethod = method,
                                customerName = cName,
                                customerPhone = cPhone,
                                customerId = cId,
                                onSuccess = { invNum ->
                                    currentScreen = ScreenNav.Invoice(invNum)
                                }
                            )
                        }
                    )
                }

                is ScreenNav.Invoice -> {
                    val sale = allSales.firstOrNull { it.invoiceNumber == nav.invoiceNumber }
                    var items by remember(nav.invoiceNumber) { mutableStateOf<List<com.example.data.local.entity.SaleItemEntity>>(emptyList()) }
                    LaunchedEffect(nav.invoiceNumber) {
                        items = viewModel.repository.getItemsForInvoiceDirect(nav.invoiceNumber)
                    }

                    InvoiceDetailScreen(
                        sale = sale,
                        items = items,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Sales }
                    )
                }

                is ScreenNav.Purchases -> {
                    PurchasesScreen(
                        purchases = allPurchases,
                        products = allProducts,
                        suppliers = allSuppliers,
                        settings = settings,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Dashboard },
                        onRecordPurchase = { supId, supName, prodId, qty, price, paid, notes ->
                            viewModel.recordStockPurchase(supId, supName, prodId, qty, price, paid, notes) {}
                        }
                    )
                }

                is ScreenNav.StockManagement -> {
                    StockManagementScreen(
                        transactions = allStockTransactions,
                        totalStock = totalStock,
                        lang = lang,
                        onBack = { currentScreen = ScreenNav.Dashboard }
                    )
                }
            }
        }
    }

    // Admin PIN prompt dialog
    if (showAdminPinDialog) {
        AdminPinDialog(
            lang = lang,
            onDismiss = { showAdminPinDialog = false },
            onConfirm = { pin ->
                val ok = viewModel.unlockAdmin(pin)
                if (ok) {
                    showAdminPinDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Admin mode unlocked")
                    }
                }
                ok
            }
        )
    }
}
