package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.SaleEntity
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    sales: List<SaleEntity>,
    settings: AppSettingsEntity,
    lang: String
) {
    var selectedPeriod by remember { mutableStateOf("Today") } // "Today", "Week", "Month", "Year", "All"

    val filteredSales = remember(sales, selectedPeriod) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            "Today" -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                sales.filter { it.date >= cal.timeInMillis }
            }
            "Week" -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                sales.filter { it.date >= cal.timeInMillis }
            }
            "Month" -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }
                sales.filter { it.date >= cal.timeInMillis }
            }
            "Year" -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -1)
                }
                sales.filter { it.date >= cal.timeInMillis }
            }
            else -> sales
        }
    }

    val totalSales = filteredSales.sumOf { it.grandTotal }
    val totalProfit = filteredSales.sumOf { it.profit }
    val totalDiscount = filteredSales.sumOf { it.discount }
    val totalPaid = filteredSales.sumOf { it.paidAmount }
    val totalRemaining = filteredSales.sumOf { it.remainingAmount }
    val totalCost = (totalSales - totalProfit).coerceAtLeast(0.0)

    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Period Selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Today", "Week", "Month", "Year", "All").forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profit Highlight Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Calculated Profit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF1B873F))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${settings.currency} ${"%,.0f".format(totalProfit)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B873F)
                        )
                        Text(
                            text = "Revenue: ${settings.currency} ${"%,.0f".format(totalSales)} • Shoe Cost: ${settings.currency} ${"%,.0f".format(totalCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Key Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Gross Sales",
                        value = "${settings.currency} ${"%,.0f".format(totalSales)}",
                        icon = Icons.Default.PointOfSale,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Collected Cash",
                        value = "${settings.currency} ${"%,.0f".format(totalPaid)}",
                        icon = Icons.Default.AttachMoney,
                        accentColor = Color(0xFF1B873F),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Credit Due",
                        value = "${settings.currency} ${"%,.0f".format(totalRemaining)}",
                        icon = Icons.Default.CreditCard,
                        accentColor = if (totalRemaining > 0) Color(0xFFBA1A1A) else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Discounts Given",
                        value = "${settings.currency} ${"%,.0f".format(totalDiscount)}",
                        icon = Icons.Default.Assessment,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Breakdown List of Sales in Period
            item {
                Text(
                    text = "Sales & Profit Log (${filteredSales.size} transactions)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredSales.isEmpty()) {
                item {
                    Text("No transactions in selected period", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(filteredSales, key = { it.id }) { sale ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${sale.customerName} • ${sdf.format(Date(sale.date))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${settings.currency} ${"%,.0f".format(sale.grandTotal)}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Profit: +${settings.currency} ${"%,.0f".format(sale.profit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B873F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
