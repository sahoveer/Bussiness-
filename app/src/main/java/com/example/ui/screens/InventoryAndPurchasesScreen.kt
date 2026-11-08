package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Business
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.example.data.model.Supplier
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryLedgerScreen(
    products: List<Product>,
    movements: List<StockMovement>,
    onRecordAdjustment: (Product, Double, String, String) -> Unit
) {
    var showAdjustDialog by remember { mutableStateOf(false) }
    val totalCostValue = products.sumOf { it.currentStock * it.purchasePrice }
    val totalRetailValue = products.sumOf { it.currentStock * it.sellingPrice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Append-Only Inventory Ledger & Valuation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cost Valuation: ${formatPkr(totalCostValue)} • Retail Valuation: ${formatPkr(totalRetailValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DukaanSuccess
                        )
                    }
                    Button(
                        onClick = { showAdjustDialog = true },
                        enabled = products.isNotEmpty(),
                        modifier = Modifier.testTag("stock_reconciliation_btn")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stock Adjustment")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movements, key = { it.id }) { mov ->
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                    .format(mov.createdAt?.toDate() ?: Date())
                val isPositive = mov.quantityChange >= 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mov.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusBadge(
                                    text = mov.movementType,
                                    color = if (isPositive) DukaanSuccess else DukaanWarning
                                )
                                if (mov.batchNumber.isNotBlank()) {
                                    StatusBadge("Batch: ${mov.batchNumber}", DukaanPrimary)
                                }
                            }
                            Text(
                                text = "${mov.reason} • By ${mov.performedBy} • $dateStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (isPositive) "+" else ""}${mov.quantityChange}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) DukaanSuccess else DukaanDanger
                            )
                            Text(
                                text = "Stock After: ${mov.stockAfter}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdjustDialog && products.isNotEmpty()) {
        var selectedProduct by remember { mutableStateOf(products.first()) }
        var movType by remember { mutableStateOf("ADJUSTMENT") } // ADJUSTMENT, DAMAGED, RECONCILIATION
        var deltaText by remember { mutableStateOf("-2") }
        var reason by remember { mutableStateOf("Physical shelf count reconciliation") }

        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Record Stock Adjustment / Reconciliation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        products.take(6).forEach { p ->
                            FilterChip(
                                selected = selectedProduct.id == p.id,
                                onClick = { selectedProduct = p },
                                label = { Text("${p.name} (${p.currentStock.toInt()})") }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ADJUSTMENT", "DAMAGED", "RECONCILIATION").forEach { t ->
                            FilterChip(
                                selected = movType == t,
                                onClick = { movType = t },
                                label = { Text(t) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = deltaText,
                        onValueChange = { deltaText = it },
                        label = { Text("Quantity Change (e.g. +10 or -2)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Audit Reason *") },
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAdjustDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val d = deltaText.toDoubleOrNull() ?: 0.0
                                if (d != 0.0) {
                                    onRecordAdjustment(selectedProduct, d, movType, reason)
                                }
                                showAdjustDialog = false
                            }
                        ) {
                            Text("Append Ledger Entry")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurchasesScreen(
    business: Business?,
    purchases: List<Purchase>,
    products: List<Product>,
    suppliers: List<Supplier>,
    onCreatePurchase: (Supplier?, Product, Double, Double, Double, Double, String, String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val isPharmacy = business?.businessType == "PHARMACY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Supplier Purchase Invoices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Automatically increases product stock, creates pharmacy batches, and updates supplier payables.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    enabled = products.isNotEmpty(),
                    modifier = Modifier.testTag("new_purchase_invoice_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Purchase")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(purchases, key = { it.id }) { pur ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(pur.invoiceNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusBadge(pur.supplierName, DukaanSecondary)
                                if (pur.batchNumber.isNotBlank()) {
                                    StatusBadge("Batch: ${pur.batchNumber}", DukaanPrimary)
                                }
                            }
                            Text(pur.itemsSummary, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatPkr(pur.grandTotal),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Paid: ${formatPkr(pur.paidAmount)} | Due: ${formatPkr(pur.payableAmount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pur.payableAmount > 0) DukaanWarning else DukaanSuccess
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && products.isNotEmpty()) {
        var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
        var selectedProduct by remember { mutableStateOf(products.first()) }
        var qtyText by remember { mutableStateOf("25") }
        var costText by remember { mutableStateOf(selectedProduct.purchasePrice.toString()) }
        var discountText by remember { mutableStateOf("0") }
        var paidText by remember { mutableStateOf("2500") }
        var batchNo by remember { mutableStateOf(if (isPharmacy) "B-2026-PUR" else "") }
        var expDate by remember { mutableStateOf(if (isPharmacy) "2028-09-01" else "") }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Create Purchase Invoice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (suppliers.isNotEmpty()) {
                        Text("Supplier:", style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            suppliers.forEach { s ->
                                FilterChip(
                                    selected = selectedSupplier?.id == s.id,
                                    onClick = { selectedSupplier = s },
                                    label = { Text(s.companyName.ifBlank { s.name }) }
                                )
                            }
                        }
                    }
                    Text("Product:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        products.take(5).forEach { p ->
                            FilterChip(
                                selected = selectedProduct.id == p.id,
                                onClick = {
                                    selectedProduct = p
                                    costText = p.purchasePrice.toString()
                                },
                                label = { Text(p.name) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("Qty (${selectedProduct.unit})") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Unit Cost (Rs.)") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = discountText, onValueChange = { discountText = it }, label = { Text("Discount (Rs.)") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = paidText, onValueChange = { paidText = it }, label = { Text("Amount Paid (Rs.)") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    if (isPharmacy) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = batchNo, onValueChange = { batchNo = it }, label = { Text("Batch #") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = expDate, onValueChange = { expDate = it }, label = { Text("Expiry YYYY-MM-DD") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onCreatePurchase(
                                    selectedSupplier,
                                    selectedProduct,
                                    qtyText.toDoubleOrNull() ?: 10.0,
                                    costText.toDoubleOrNull() ?: selectedProduct.purchasePrice,
                                    discountText.toDoubleOrNull() ?: 0.0,
                                    paidText.toDoubleOrNull() ?: 0.0,
                                    "CASH",
                                    batchNo,
                                    expDate
                                )
                                showCreateDialog = false
                            }
                        ) {
                            Text("Save Purchase & Update Stock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesHistoryScreen(
    sales: List<Sale>,
    onViewReceipt: (Sale) -> Unit,
    onProcessReturn: (Sale, String) -> Unit,
    onSyncOfflineSales: () -> Unit
) {
    val pendingCount = sales.count { it.syncStatus == "PENDING_SYNC" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sales Invoices & Linked Returns", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${sales.size} invoices recorded • Deletion prohibited by security rules (returns create auditable state transitions).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (pendingCount > 0) {
                    Button(onClick = onSyncOfflineSales) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync $pendingCount Offline")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(sale.invoiceNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                StatusBadge(
                                    text = sale.status,
                                    color = if (sale.status == "COMPLETED") DukaanSuccess else DukaanDanger
                                )
                                StatusBadge(sale.paymentMethod, DukaanPrimary)
                                if (sale.syncStatus == "PENDING_SYNC") {
                                    StatusBadge("OFFLINE QUEUED", DukaanWarning)
                                }
                            }
                            Text(
                                text = formatPkr(sale.grandTotal),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Customer: ${sale.customerName} • Items: ${sale.itemsSummary}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cost Snapshot: ${formatPkr(sale.totalCostSnapshot)} • Gross Profit: ${formatPkr(sale.grossProfit)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DukaanInfo
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onViewReceipt(sale) }) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt")
                                }
                                if (sale.status != "RETURNED") {
                                    OutlinedButton(
                                        onClick = { onProcessReturn(sale, "Customer counter return") }
                                    ) {
                                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Return")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
