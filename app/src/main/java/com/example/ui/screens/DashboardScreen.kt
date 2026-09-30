package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    settings: AppSettingsEntity,
    todaySales: Double,
    todayProfit: Double,
    todayOrders: Int,
    totalProducts: Int,
    totalStock: Int,
    totalCustomers: Int,
    totalReceivable: Double,
    lowStockList: List<ProductEntity>,
    recentSalesList: List<SaleEntity>,
    lang: String,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToScanQr: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToPurchaseStock: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToInvoice: (String) -> Unit,
    onNavigateToProductDetail: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shoes & Wholesale Dealer • ${settings.shopPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = settings.shopLocation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    ElevatedButton(
                        onClick = onNavigateToScanQr,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("action_scan_qr")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("scan_qr", lang), fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = onNavigateToNewSale,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("action_new_sale")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("new_sale", lang))
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = onNavigateToAddProduct,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("action_add_product")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("add_product", lang))
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = onNavigateToPurchaseStock,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("purchase_stock", lang))
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = onNavigateToAddCustomer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("add_customer", lang))
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = onNavigateToReports,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.tr("reports", lang))
                    }
                }
            }
        }

        // Today's Performance (Sales, Profit, Orders)
        item {
            Text(
                text = "Today's Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = Localization.tr("today_sales", lang),
                    value = "${settings.currency} ${"%,.0f".format(todaySales)}",
                    icon = Icons.Default.PointOfSale,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = Localization.tr("today_profit", lang),
                    value = "${settings.currency} ${"%,.0f".format(todayProfit)}",
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF1B873F),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = Localization.tr("today_orders", lang),
                    value = "$todayOrders",
                    icon = Icons.Default.Receipt,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(0.8f)
                )
            }
        }

        // Overall Shop Metrics (Products, Stock, Customers, Credit)
        item {
            Text(
                text = "Inventory & Credit Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = Localization.tr("total_products", lang),
                    value = "$totalProducts",
                    icon = Icons.Default.Inventory,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = Localization.tr("total_stock", lang),
                    value = "$totalStock pairs",
                    icon = Icons.Default.Inventory,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = Localization.tr("total_customers", lang),
                    value = "$totalCustomers",
                    icon = Icons.Default.People,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = Localization.tr("total_receivable", lang),
                    value = "${settings.currency} ${"%,.0f".format(totalReceivable)}",
                    icon = Icons.Default.NotificationsActive,
                    accentColor = if (totalReceivable > 0) Color(0xFFBA1A1A) else Color(0xFF1B873F),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Low Stock Alert Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (lowStockList.isNotEmpty()) Color(0xFFBA1A1A) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.tr("low_stock_products", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                SuggestionChip(
                    onClick = { },
                    label = { Text("${lowStockList.size} items") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (lowStockList.isNotEmpty()) Color(0xFFFFDAD6) else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (lowStockList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All footwear stock levels are healthy! Minimum threshold is ${settings.lowStockLimit}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lowStockList.take(4).forEach { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToProductDetail(product.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (product.quantity <= 0) Color(0xFFFFDAD6) else Color(0xFFFFF1EB)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${product.productId} • Size: ${product.size} • ${product.color}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (product.quantity <= 0) Color(0xFFBA1A1A) else Color(0xFFB87800)
                                    ) {
                                        Text(
                                            text = if (product.quantity <= 0) "OUT OF STOCK" else "LOW: ${product.quantity}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Text(
                text = Localization.tr("recent_sales", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recentSalesList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No sales recorded yet. Click 'New Sale' or 'Scan QR' to start selling!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        recentSalesList.forEachIndexed { index, sale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToInvoice(sale.invoiceNumber) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sale.invoiceNumber,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (sale.paymentMethod) {
                                                "Cash" -> Color(0xFFE8F5E9)
                                                "Credit" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF8E1)
                                            }
                                        ) {
                                            Text(
                                                text = sale.paymentMethod,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = when (sale.paymentMethod) {
                                                    "Cash" -> Color(0xFF2E7D32)
                                                    "Credit" -> Color(0xFFC62828)
                                                    else -> Color(0xFFF57F17)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${sale.customerName} • ${sdf.format(Date(sale.date))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${settings.currency} ${"%,.0f".format(sale.grandTotal)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (sale.remainingAmount > 0) {
                                        Text(
                                            text = "Due: ${settings.currency} ${"%,.0f".format(sale.remainingAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFBA1A1A)
                                        )
                                    }
                                }
                            }
                            if (index < recentSalesList.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom space for comfortable scrolling above navigation bar
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
