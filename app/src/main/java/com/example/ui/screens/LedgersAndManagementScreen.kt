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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.example.data.model.AuditLog
import com.example.data.model.Business
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.Sale
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

@Composable
fun CustomersUdhaarScreen(
    customers: List<Customer>,
    onAddCustomer: (String, String, String, String, Double) -> Unit,
    onReceiveUdhaar: (Customer, Double, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var paymentTargetCustomer by remember { mutableStateOf<Customer?>(null) }

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
                    Text("Customers & Udhaar (Credit) Khata", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Total Receivable Udhaar: ${formatPkr(customers.sumOf { it.outstandingBalance })} • All credit payments are logged in Audit Ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DukaanWarning
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_customer_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Customer")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customers, key = { it.id }) { cust ->
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
                            Text(cust.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Phone: ${cust.phone.ifBlank { "N/A" }} • ${cust.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Lifetime Purchases: ${formatPkr(cust.totalPurchases)} • ${cust.notes}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Udhaar: ${formatPkr(cust.outstandingBalance)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                color = if (cust.outstandingBalance > 0) DukaanWarning else DukaanSuccess
                            )
                            if (cust.outstandingBalance > 0) {
                                Button(onClick = { paymentTargetCustomer = cust }) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receive Payment")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("0300-") }
        var address by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("Monthly Udhaar account") }
        var opening by remember { mutableStateOf("0") }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Customer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true)
                    OutlinedTextField(value = opening, onValueChange = { opening = it }, label = { Text("Opening Udhaar Balance (Rs.)") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onAddCustomer(name, phone, address, notes, opening.toDoubleOrNull() ?: 0.0)
                                    showAddDialog = false
                                }
                            }
                        ) { Text("Save Customer") }
                    }
                }
            }
        }
    }

    if (paymentTargetCustomer != null) {
        val cust = paymentTargetCustomer!!
        var amountText by remember { mutableStateOf(cust.outstandingBalance.toInt().toString()) }
        var method by remember { mutableStateOf("CASH") }
        Dialog(onDismissRequest = { paymentTargetCustomer = null }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Receive Udhaar Payment from ${cust.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount Received (Rs.)") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("CASH", "JAZZCASH", "EASYPAISA", "BANK_TRANSFER").forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { paymentTargetCustomer = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) onReceiveUdhaar(cust, amt, method)
                                paymentTargetCustomer = null
                            }
                        ) { Text("Record Receipt") }
                    }
                }
            }
        }
    }
}

@Composable
fun SuppliersScreen(
    suppliers: List<Supplier>,
    onAddSupplier: (String, String, String, String, Double) -> Unit,
    onPaySupplier: (Supplier, Double, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var payTarget by remember { mutableStateOf<Supplier?>(null) }

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
                    Text("Suppliers & Payables Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Total Supplier Payables: ${formatPkr(suppliers.sumOf { it.payableBalance })}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DukaanDanger
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Supplier")
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suppliers, key = { it.id }) { sup ->
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
                            Text(sup.companyName.ifBlank { sup.name }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Contact: ${sup.name} • ${sup.phone} • ${sup.address}", style = MaterialTheme.typography.bodySmall)
                            Text("Total Purchased: ${formatPkr(sup.totalPurchased)}", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Payable: ${formatPkr(sup.payableBalance)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                color = if (sup.payableBalance > 0) DukaanDanger else DukaanSuccess
                            )
                            if (sup.payableBalance > 0) {
                                Button(onClick = { payTarget = sup }) { Text("Pay Supplier") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("042-") }
        var address by remember { mutableStateOf("") }
        var payable by remember { mutableStateOf("0") }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Supplier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company / Distributor Name *") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact Person *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true)
                    OutlinedTextField(value = payable, onValueChange = { payable = it }, label = { Text("Opening Payable Balance (Rs.)") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (company.isNotBlank() || name.isNotBlank()) {
                                    onAddSupplier(name.ifBlank { company }, company, phone, address, payable.toDoubleOrNull() ?: 0.0)
                                    showAddDialog = false
                                }
                            }
                        ) { Text("Save Supplier") }
                    }
                }
            }
        }
    }

    if (payTarget != null) {
        val sup = payTarget!!
        var amtText by remember { mutableStateOf(sup.payableBalance.toInt().toString()) }
        var method by remember { mutableStateOf("BANK_TRANSFER") }
        Dialog(onDismissRequest = { payTarget = null }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pay ${sup.companyName.ifBlank { sup.name }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = amtText, onValueChange = { amtText = it }, label = { Text("Payment Amount (Rs.)") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BANK_TRANSFER", "CASH", "JAZZCASH", "EASYPAISA").forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { payTarget = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amtText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) onPaySupplier(sup, amt, method)
                                payTarget = null
                            }
                        ) { Text("Confirm Payment") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    onAddExpense: (String, String, Double, String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val categories = listOf("Rent", "Electricity", "Internet", "Salaries", "Transport", "Maintenance", "Other")

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
                    Text("Operating Expenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Total Posted Expenses: ${formatPkr(expenses.filter { it.status != "DRAFT" }.sumOf { it.amount })}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DukaanWarning
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Expense")
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { exp ->
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
                                Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusBadge(exp.category, DukaanPrimary)
                                StatusBadge(exp.status, if (exp.status == "POSTED") DukaanSuccess else DukaanWarning)
                            }
                            Text("${exp.paymentMethod} • ${exp.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = formatPkr(exp.amount),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                            fontWeight = FontWeight.Bold,
                            color = DukaanWarning
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var category by remember { mutableStateOf("Electricity") }
        var title by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("5000") }
        var method by remember { mutableStateOf("CASH") }
        var notes by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Record Operating Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Expense Title *") }, singleLine = true)
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Rs.) *") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Voucher / Receipt Notes") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAddExpense(category, title, amount.toDoubleOrNull() ?: 0.0, method, "POSTED", notes)
                                    showAddDialog = false
                                }
                            }
                        ) { Text("Post Expense") }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialReportsAndAccountsScreen(
    products: List<Product>,
    sales: List<Sale>,
    purchases: List<Purchase>,
    expenses: List<Expense>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    auditLogs: List<AuditLog>
) {
    val activeSales = sales.filter { it.status != "RETURNED" }
    val returnedSales = sales.filter { it.status == "RETURNED" }
    val grossSalesRevenue = activeSales.sumOf { it.grandTotal }
    val cashReceived = activeSales.sumOf { it.paidAmount }
    val costOfGoodsSold = activeSales.sumOf { it.totalCostSnapshot }
    val grossProfit = activeSales.sumOf { it.grossProfit }
    val totalOperatingExpenses = expenses.filter { it.status != "DRAFT" }.sumOf { it.amount }
    val netOperatingProfit = grossProfit - totalOperatingExpenses
    val stockValuationCost = products.sumOf { it.currentStock * it.purchasePrice }
    val stockValuationRetail = products.sumOf { it.currentStock * it.sellingPrice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Statement of Profit & Loss (Historical Cost Snapshot Basis)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Distinguishes billed revenue, actual cash collected, historical COGS snapshots, gross profit, and estimated net operating profit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ReportLine("1. Total Billed Sales Revenue (${activeSales.size} invoices)", formatPkr(grossSalesRevenue), DukaanPrimary)
                ReportLine("   • Cash / Wallet Collected at Counter", formatPkr(cashReceived), DukaanInfo)
                ReportLine("   • Credit / Udhaar Extended on Sales", formatPkr(activeSales.sumOf { it.creditAmount }), DukaanWarning)
                ReportLine("   • Sales Returns (${returnedSales.size} invoices)", formatPkr(returnedSales.sumOf { it.grandTotal }), DukaanDanger)
                HorizontalDivider()
                ReportLine("2. Cost of Goods Sold (Historical Cost Snapshot)", "- ${formatPkr(costOfGoodsSold)}", MaterialTheme.colorScheme.onSurface)
                ReportLine("3. GROSS PROFIT (Revenue ex-Tax minus COGS)", formatPkr(grossProfit), DukaanSuccess)
                ReportLine("4. Operating Expenses (${expenses.size} vouchers)", "- ${formatPkr(totalOperatingExpenses)}", DukaanWarning)
                HorizontalDivider()
                ReportLine(
                    "5. ESTIMATED NET OPERATING PROFIT",
                    formatPkr(netOperatingProfit),
                    if (netOperatingProfit >= 0) DukaanSuccess else DukaanDanger
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Balance Sheet & Stock Valuation Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ReportLine("Inventory Valuation (Purchase Cost Basis)", formatPkr(stockValuationCost), DukaanPrimary)
                ReportLine("Inventory Valuation (Retail Selling Basis)", formatPkr(stockValuationRetail), DukaanSuccess)
                ReportLine("Total Purchases Recorded (${purchases.size} bills)", formatPkr(purchases.sumOf { it.grandTotal }), DukaanSecondary)
                ReportLine("Accounts Receivable (Customer Udhaar)", formatPkr(customers.sumOf { it.outstandingBalance }), DukaanWarning)
                ReportLine("Accounts Payable (Supplier Balances)", formatPkr(suppliers.sumOf { it.payableBalance }), DukaanDanger)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = DukaanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Append-Only Security & Financial Audit Log (${auditLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                auditLogs.take(12).forEach { log ->
                    val ts = SimpleDateFormat("dd MMM HH:mm", Locale.US).format(log.createdAt?.toDate() ?: Date())
                    Text(
                        text = "[$ts] ${log.module} • ${log.action}: ${log.details} (By ${log.performedBy})",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeesScreen(
    employees: List<Employee>,
    activeRole: String,
    onSwitchActiveRole: (String) -> Unit,
    onAddEmployee: (String, String, String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val roles = listOf(
        "Owner" to "BILLING,DISCOUNTS,RETURNS,STOCK_ADJUST,PURCHASES,EXPENSES,REPORTS,SETTINGS",
        "Manager" to "BILLING,DISCOUNTS,RETURNS,STOCK_ADJUST,PURCHASES,EXPENSES,REPORTS",
        "Cashier" to "BILLING,DISCOUNTS",
        "Inventory Manager" to "STOCK_ADJUST,PURCHASES",
        "Accountant" to "EXPENSES,REPORTS",
        "Viewer" to "REPORTS"
    )

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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Employees & Role-Based Authorization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Active Session Role: $activeRole • Switch role below to test sidebar permission filtering.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DukaanPrimary
                        )
                    }
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invite Staff")
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { (roleName, _) ->
                        FilterChip(
                            selected = activeRole == roleName,
                            onClick = { onSwitchActiveRole(roleName) },
                            label = { Text("Simulate: $roleName") }
                        )
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(employees, key = { it.id }) { emp ->
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
                                Text(emp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                StatusBadge(emp.role, DukaanPrimary)
                            }
                            Text("${emp.phone} • ${emp.email}", style = MaterialTheme.typography.bodySmall)
                            Text("Permissions: ${emp.permissionsCsv}", style = MaterialTheme.typography.labelSmall, color = DukaanSuccess)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("0300-") }
        var email by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(roles[2]) }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Invite Employee", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { pair ->
                            FilterChip(
                                selected = selectedRole.first == pair.first,
                                onClick = { selectedRole = pair },
                                label = { Text(pair.first) }
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onAddEmployee(name, phone, email, selectedRole.first, selectedRole.second)
                                    showAddDialog = false
                                }
                            }
                        ) { Text("Save Employee") }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsAlertsScreen(
    products: List<Product>,
    batches: List<MedicineBatch>,
    customers: List<Customer>,
    suppliers: List<Supplier>
) {
    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val expired = batches.filter { it.availableQty > 0 && it.expiryDate < todayIso }
    val nearExp = batches.filter { it.availableQty > 0 && it.expiryDate >= todayIso && it.expiryDate <= "2027-05-08" }
    val lowStock = products.filter { it.isActive && it.currentStock <= it.reorderLevel }
    val udhaarDue = customers.filter { it.outstandingBalance > 0 }
    val supplierDue = suppliers.filter { it.payableBalance > 0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Notification & Alert Center", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        items(expired, key = { "exp_${it.id}" }) { b ->
            AlertItemCard("EXPIRED BATCH", "${b.productName} • Batch ${b.batchNumber} expired on ${b.expiryDate} (${b.availableQty.toInt()} units)", DukaanDanger)
        }
        items(nearExp, key = { "near_${it.id}" }) { b ->
            AlertItemCard("NEAR EXPIRY", "${b.productName} • Batch ${b.batchNumber} expires on ${b.expiryDate}", DukaanWarning)
        }
        items(lowStock, key = { "low_${it.id}" }) { p ->
            AlertItemCard("LOW STOCK", "${p.name} has ${p.currentStock} ${p.unit} remaining (Reorder level: ${p.reorderLevel})", DukaanWarning)
        }
        items(udhaarDue, key = { "cust_${it.id}" }) { c ->
            AlertItemCard("CUSTOMER UDHAAR", "${c.name} (${c.phone}) owes ${formatPkr(c.outstandingBalance)}", DukaanInfo)
        }
        items(supplierDue, key = { "sup_${it.id}" }) { s ->
            AlertItemCard("SUPPLIER PAYABLE", "${s.companyName.ifBlank { s.name }} due ${formatPkr(s.payableBalance)}", DukaanSecondary)
        }
    }
}

@Composable
private fun AlertItemCard(badge: String, message: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = color)
            Column(modifier = Modifier.weight(1f)) {
                StatusBadge(badge, color)
                Spacer(modifier = Modifier.height(4.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SubscriptionAndSettingsScreen(
    business: Business?,
    isSubscriptionTab: Boolean,
    onSaveSettings: (String, String, String, String, String, String, Double, String, String, String, String) -> Unit
) {
    if (business == null) return
    var name by remember(business.id) { mutableStateOf(business.name) }
    var branch by remember(business.id) { mutableStateOf(business.branchName) }
    var city by remember(business.id) { mutableStateOf(business.city) }
    var phone by remember(business.id) { mutableStateOf(business.phone) }
    var address by remember(business.id) { mutableStateOf(business.address) }
    var language by remember(business.id) { mutableStateOf(business.language) }
    var taxText by remember(business.id) { mutableStateOf(business.taxRatePercent.toString()) }
    var receiptHeader by remember(business.id) { mutableStateOf(business.receiptHeader) }
    var receiptFooter by remember(business.id) { mutableStateOf(business.receiptFooter) }
    var plan by remember(business.id) { mutableStateOf(business.subscriptionPlan) }
    var cycle by remember(business.id) { mutableStateOf(business.subscriptionBillingCycle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isSubscriptionTab) {
            Text("DukaanOS SaaS Subscription Plans", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Customer records are permanently preserved even during grace periods. Configurable payment provider architecture ready for JazzCash, Easypaisa & Bank Transfer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = cycle == "MONTHLY", onClick = { cycle = "MONTHLY" }, label = { Text("Monthly Billing") })
                FilterChip(selected = cycle == "ANNUAL", onClick = { cycle = "ANNUAL" }, label = { Text("Annual Billing (Save 15%)") })
            }

            SubscriptionTierCard(
                tier = "STARTER",
                price = if (cycle == "MONTHLY") "Rs. 999 / month" else "Rs. 9,990 / year",
                features = "1 Branch • 1 Cashier • Unlimited POS Billing • Udhaar Ledger",
                selected = plan == "STARTER",
                onSelect = { plan = "STARTER" }
            )
            SubscriptionTierCard(
                tier = "PROFESSIONAL",
                price = if (cycle == "MONTHLY") "Rs. 2,499 / month" else "Rs. 24,990 / year",
                features = "Up to 3 Branches • 5 Staff Roles • Pharmacy Batch & Expiry Tracking • Financial Reports",
                selected = plan == "PROFESSIONAL",
                onSelect = { plan = "PROFESSIONAL" }
            )
            SubscriptionTierCard(
                tier = "BUSINESS",
                price = if (cycle == "MONTHLY") "Rs. 4,999 / month" else "Rs. 49,990 / year",
                features = "Unlimited Branches • Multi-Business Switching • Full Audit Logs • Priority Cloud Sync",
                selected = plan == "BUSINESS",
                onSelect = { plan = "BUSINESS" }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Business Profile, Receipt & Language Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch Name") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = taxText, onValueChange = { taxText = it }, label = { Text("Tax Rate %") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = receiptHeader, onValueChange = { receiptHeader = it }, label = { Text("Thermal Receipt Header") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = receiptFooter, onValueChange = { receiptFooter = it }, label = { Text("Thermal Receipt Footer") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Interface Language:")
                    FilterChip(selected = language == "EN", onClick = { language = "EN" }, label = { Text("English (LTR)") })
                    FilterChip(selected = language == "UR", onClick = { language = "UR" }, label = { Text("اردو (RTL)") })
                }

                Button(
                    onClick = {
                        onSaveSettings(
                            name, branch, city, phone, address, language,
                            taxText.toDoubleOrNull() ?: 0.0, receiptHeader, receiptFooter, plan, cycle
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Configuration & Plan")
                }
            }
        }
    }
}

@Composable
private fun SubscriptionTierCard(
    tier: String,
    price: String,
    features: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) DukaanPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) DukaanPrimary else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = DukaanPrimary, modifier = Modifier.size(18.dp))
                    Text(tier, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("• $price", style = MaterialTheme.typography.titleSmall, color = DukaanSuccess)
                }
                Text(features, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onSelect) {
                if (selected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Active")
                } else {
                    Text("Select")
                }
            }
        }
    }
}
