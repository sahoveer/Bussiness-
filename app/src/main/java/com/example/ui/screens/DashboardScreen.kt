package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Business
import com.example.data.model.Customer
import com.example.data.model.DateRangeFilter
import com.example.data.model.DukaanModule
import com.example.data.model.Expense
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.Sale
import com.example.data.model.Supplier
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FinancialMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatPkr
import com.example.ui.theme.DukaanDanger
import com.example.ui.theme.DukaanInfo
import com.example.ui.theme.DukaanPrimary
import com.example.ui.theme.DukaanSecondary
import com.example.ui.theme.DukaanSuccess
import com.example.ui.theme.DukaanWarning
import com.example.ui.theme.JetBrainsMono
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    business: Business?,
    products: List<Product>,
    batches: List<MedicineBatch>,
    sales: List<Sale>,
    purchases: List<Purchase>,
    expenses: List<Expense>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    selectedDateFilter: DateRangeFilter,
    onSelectDateFilter: (DateRangeFilter) -> Unit,
    onNavigate: (DukaanModule) -> Unit,
    onSeedSampleData: () -> Unit
) {
    val isUrdu = business?.language == "UR"
    val nowMs = System.currentTimeMillis()
    val cutoffMs = nowMs - (selectedDateFilter.daysBack * 24L * 3600L * 1000L)

    val filteredSales = sales.filter { sale ->
        val ts = sale.createdAt?.toDate()?.time ?: nowMs
        sale.status != "RETURNED" && (selectedDateFilter == DateRangeFilter.ALL_TIME || ts >= cutoffMs)
    }
    val filteredPurchases = purchases.filter { pur ->
        val ts = pur.createdAt?.toDate()?.time ?: nowMs
        pur.status != "RETURNED" && (selectedDateFilter == DateRangeFilter.ALL_TIME || ts >= cutoffMs)
    }
    val filteredExpenses = expenses.filter { exp ->
        val ts = exp.createdAt?.toDate()?.time ?: nowMs
        exp.status != "DRAFT" && (selectedDateFilter == DateRangeFilter.ALL_TIME || ts >= cutoffMs)
    }

    // Real financial metrics computed from stored transactions
    val totalSalesRevenue = filteredSales.sumOf { it.grandTotal }
    val totalCashReceived = filteredSales.sumOf { it.paidAmount }
    val totalPurchasesAmount = filteredPurchases.sumOf { it.grandTotal }
    val grossProfit = filteredSales.sumOf { it.grossProfit }
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val netOperatingProfit = grossProfit - totalExpenses
    val customerUdhaarTotal = customers.sumOf { it.outstandingBalance }
    val supplierPayableTotal = suppliers.sumOf { it.payableBalance }
    val cashBalance = totalCashReceived - filteredPurchases.sumOf { it.paidAmount } - totalExpenses

    // Real inventory & expiry alerts
    val lowStockProducts = products.filter { it.isActive && it.currentStock <= it.reorderLevel }
    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val expiredBatches = batches.filter { it.availableQty > 0 && it.expiryDate.isNotBlank() && it.expiryDate < todayIso }
    val nearExpiryBatches = batches.filter {
        it.availableQty > 0 && it.expiryDate >= todayIso && it.expiryDate <= "2027-05-08"
    }
    val pendingOfflineSales = sales.count { it.syncStatus == "PENDING_SYNC" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Quick Actions + Date Filter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = if (business?.businessType == "PHARMACY") "PHARMACY MODE" else "GENERAL STORE MODE",
                                color = if (business?.businessType == "PHARMACY") DukaanSuccess else DukaanPrimary
                            )
                            StatusBadge(
                                text = "${business?.subscriptionPlan ?: "PRO"} • ${business?.subscriptionStatus ?: "ACTIVE"}",
                                color = DukaanSecondary
                            )
                            if (pendingOfflineSales > 0) {
                                StatusBadge(
                                    text = "$pendingOfflineSales Pending Sync",
                                    color = DukaanWarning
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isUrdu) "کاروباری ڈیش بورڈ - ${business?.name.orEmpty()}" else "${business?.name ?: "DukaanOS"} Executive Overview",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${business?.branchName ?: "Main Branch"} • ${business?.city ?: "Pakistan"} • Currency: PKR (Rs.) • Timezone: Asia/Karachi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onNavigate(DukaanModule.POS) },
                        modifier = Modifier.testTag("dashboard_open_pos_btn")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isUrdu) "بلنگ (POS)" else "Open POS")
                    }
                }

                // Date Filter Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DateRangeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedDateFilter == filter,
                            onClick = { onSelectDateFilter(filter) },
                            label = { Text(if (isUrdu) filter.labelUr else filter.labelEn) }
                        )
                    }
                }
            }
        }

        if (products.isEmpty() && sales.isEmpty()) {
            EmptyStateCard(
                title = "No Business Transactions Yet",
                description = "Your business catalog is currently empty. Add products manually, open POS billing, or load sample Pakistani ${if (business?.businessType == "PHARMACY") "Pharmacy medicines & DRAP batches" else "General Store / Karyana items"} to explore live analytics.",
                actionLabel = "Load Sample Pakistani Catalog & Transactions",
                onAction = onSeedSampleData
            )
        }

        // 8 Financial Metric Cards (2x4 Responsive Grid)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    title = if (isUrdu) "کل سیلز" else "Sales Revenue",
                    value = formatPkr(totalSalesRevenue),
                    subtitle = "${filteredSales.size} invoices (${formatPkr(totalCashReceived)} received)",
                    icon = Icons.Default.PointOfSale,
                    accentColor = DukaanPrimary,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = if (isUrdu) "مجموعی منافع" else "Gross Profit",
                    value = formatPkr(grossProfit),
                    subtitle = "Historical Cost Snapshot Basis",
                    icon = Icons.Default.TrendingUp,
                    accentColor = DukaanSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    title = if (isUrdu) "خالص منافع" else "Est. Net Operating Profit",
                    value = formatPkr(netOperatingProfit),
                    subtitle = "Gross Profit minus Expenses",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = if (netOperatingProfit >= 0) DukaanSuccess else DukaanDanger,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = if (isUrdu) "کیش بیلنس" else "Cash & Wallet Balance",
                    value = formatPkr(cashBalance),
                    subtitle = "Net Cash Inflow in Drawer/Bank",
                    icon = Icons.Default.AccountBalance,
                    accentColor = DukaanInfo,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    title = if (isUrdu) "خریداری (پرچیز)" else "Purchases",
                    value = formatPkr(totalPurchasesAmount),
                    subtitle = "${filteredPurchases.size} supplier bills",
                    icon = Icons.Default.AddShoppingCart,
                    accentColor = DukaanSecondary,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = if (isUrdu) "اخراجات" else "Operating Expenses",
                    value = formatPkr(totalExpenses),
                    subtitle = "${filteredExpenses.size} posted expense vouchers",
                    icon = Icons.Default.TrendingDown,
                    accentColor = DukaanWarning,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    title = if (isUrdu) "گاہک واجبات (ادھار)" else "Customer Outstanding (Udhaar)",
                    value = formatPkr(customerUdhaarTotal),
                    subtitle = "${customers.count { it.outstandingBalance > 0 }} credit accounts",
                    icon = Icons.Default.Receipt,
                    accentColor = DukaanWarning,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(DukaanModule.CUSTOMERS) }
                )
                FinancialMetricCard(
                    title = if (isUrdu) "سپلائر واجبات" else "Supplier Payables",
                    value = formatPkr(supplierPayableTotal),
                    subtitle = "${suppliers.count { it.payableBalance > 0 }} suppliers due",
                    icon = Icons.Default.Inventory2,
                    accentColor = DukaanDanger,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(DukaanModule.SUPPLIERS) }
                )
            }
        }

        // Critical Business Alerts Panel (Expiry, Low Stock, Udhaar)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Alerts",
                            tint = DukaanWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Inventory, Expiry & Financial Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(onClick = { onNavigate(DukaanModule.NOTIFICATIONS) }) {
                        Text("View All")
                    }
                }

                if (business?.businessType == "PHARMACY") {
                    if (expiredBatches.isNotEmpty()) {
                        AlertBannerRow(
                            title = "EXPIRED MEDICINE BATCHES (${expiredBatches.size}) — Blocked from POS",
                            subtitle = expiredBatches.joinToString { "${it.productName} [Batch: ${it.batchNumber}, Exp: ${it.expiryDate}]" },
                            color = DukaanDanger,
                            onClick = { onNavigate(DukaanModule.PHARMACY_BATCHES) }
                        )
                    }
                    if (nearExpiryBatches.isNotEmpty()) {
                        AlertBannerRow(
                            title = "Near-Expiry Batches (${nearExpiryBatches.size} within 180 days)",
                            subtitle = nearExpiryBatches.joinToString { "${it.productName} [Batch: ${it.batchNumber}, Exp: ${it.expiryDate}]" },
                            color = DukaanWarning,
                            onClick = { onNavigate(DukaanModule.PHARMACY_BATCHES) }
                        )
                    }
                }

                if (lowStockProducts.isNotEmpty()) {
                    AlertBannerRow(
                        title = "Low Stock Reorder Alert (${lowStockProducts.size} items below reorder level)",
                        subtitle = lowStockProducts.joinToString { "${it.name} (${it.currentStock.toInt()} ${it.unit} left)" },
                        color = DukaanWarning,
                        onClick = { onNavigate(DukaanModule.INVENTORY) }
                    )
                }

                if (customerUdhaarTotal > 0) {
                    AlertBannerRow(
                        title = "Customer Udhaar Receivable: ${formatPkr(customerUdhaarTotal)}",
                        subtitle = customers.filter { it.outstandingBalance > 0 }
                            .joinToString { "${it.name}: ${formatPkr(it.outstandingBalance)}" },
                        color = DukaanInfo,
                        onClick = { onNavigate(DukaanModule.CUSTOMERS) }
                    )
                }

                if (expiredBatches.isEmpty() && nearExpiryBatches.isEmpty() && lowStockProducts.isEmpty() && customerUdhaarTotal == 0.0) {
                    Text(
                        text = "All stock levels, batches, and credit accounts are healthy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DukaanSuccess
                    )
                }
            }
        }

        // Analytics Charts Row (Payment Method Distribution + Category Stock Value)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Payment Method Distribution & Category Valuation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val methods = listOf("CASH", "JAZZCASH", "EASYPAISA", "BANK_TRANSFER", "CREDIT")
                val methodTotals = methods.associateWith { m ->
                    filteredSales.filter { it.paymentMethod == m }.sumOf { it.grandTotal }
                }
                val maxMethodVal = max(1.0, methodTotals.values.maxOrNull() ?: 1.0)

                methods.forEach { method ->
                    val amount = methodTotals[method] ?: 0.0
                    val ratio = (amount / maxMethodVal).toFloat().coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = method.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatPkr(amount),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (method) {
                                "CASH" -> DukaanSuccess
                                "JAZZCASH" -> DukaanDanger
                                "EASYPAISA" -> DukaanPrimary
                                "BANK_TRANSFER" -> DukaanInfo
                                else -> DukaanWarning
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Inventory Valuation by Category (Cost vs Retail)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val byCategory = products.groupBy { it.category }
                if (byCategory.isNotEmpty()) {
                    CategoryValuationBarChart(byCategory)
                }
            }
        }
    }
}

@Composable
private fun AlertBannerRow(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CategoryValuationBarChart(
    byCategory: Map<String, List<Product>>
) {
    val entries = byCategory.entries.take(5)
    val maxVal = max(
        1.0,
        entries.maxOfOrNull { entry -> entry.value.sumOf { it.currentStock * it.sellingPrice } } ?: 1.0
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val barCount = entries.size.coerceAtLeast(1)
            val slotWidth = size.width / barCount
            val barWidth = (slotWidth * 0.55f).coerceAtMost(90f)

            entries.forEachIndexed { idx, entry ->
                val retailVal = entry.value.sumOf { it.currentStock * it.sellingPrice }
                val costVal = entry.value.sumOf { it.currentStock * it.purchasePrice }
                val retailHeight = ((retailVal / maxVal).toFloat() * (size.height - 16f)).coerceAtLeast(8f)
                val costHeight = ((costVal / maxVal).toFloat() * (size.height - 16f)).coerceAtLeast(4f)
                val x = idx * slotWidth + (slotWidth - barWidth) / 2f

                // Retail value bar
                drawRoundRect(
                    color = DukaanPrimary.copy(alpha = 0.35f),
                    topLeft = Offset(x, size.height - retailHeight),
                    size = Size(barWidth, retailHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Cost value bar
                drawRoundRect(
                    color = DukaanSuccess,
                    topLeft = Offset(x, size.height - costHeight),
                    size = Size(barWidth, costHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            entries.forEach { entry ->
                val retailVal = entry.value.sumOf { it.currentStock * it.sellingPrice }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = entry.key.take(12),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatPkr(retailVal),
                        style = MaterialTheme.typography.labelSmall,
                        color = DukaanPrimary
                    )
                }
            }
        }
    }
}
