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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Supplier
import com.example.ui.components.EmptyStateCard
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
fun ProductsScreen(
    business: Business?,
    products: List<Product>,
    onSaveProduct: (Product, Boolean) -> Unit,
    onToggleArchive: (Product) -> Unit,
    onBulkPriceChange: (Double) -> Unit,
    onSeedSampleData: () -> Unit
) {
    val isPharmacy = business?.businessType == "PHARMACY"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showArchived by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showBulkPriceDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val filtered = products.filter { p ->
        (showArchived || p.isActive) &&
            (selectedCategory == "All" || p.category == selectedCategory) &&
            (searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true) ||
                p.genericName.contains(searchQuery, ignoreCase = true) ||
                p.saltFormula.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPharmacy) "Pharmacy Medicines & DRAP Catalog" else "General Store Product Catalog",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${products.count { it.isActive }} active products • Supports Piece, Packet, Box, Kg, Gram, Litre, Dozen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            isCreatingNew = true
                            editingProduct = Product(
                                businessId = business?.id.orEmpty(),
                                unit = if (isPharmacy) "Strip" else "Piece",
                                purchaseUnit = "Box",
                                conversionFactor = if (isPharmacy) 10.0 else 1.0
                            )
                        },
                        modifier = Modifier.testTag("add_new_product_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPharmacy) "Add Medicine" else "Add Product")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search name, SKU, barcode, or generic salt…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedButton(onClick = { showBulkPriceDialog = true }) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bulk Price %")
                    }
                    OutlinedButton(onClick = { showCsvExportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                    FilterChip(
                        selected = showArchived,
                        onClick = { showArchived = !showArchived },
                        label = { Text("Show Archived") }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = "No Matching Products Found",
                description = "Create a new product above or load sample Pakistani inventory to get started.",
                actionLabel = "Load Sample Catalog",
                onAction = onSeedSampleData
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { product ->
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
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusBadge(product.category, DukaanPrimary)
                                    if (!product.isActive) {
                                        StatusBadge("ARCHIVED", DukaanDanger)
                                    }
                                    if (product.isLooseWeight) {
                                        StatusBadge("Loose Weight", DukaanInfo)
                                    }
                                }
                                Text(
                                    text = "SKU: ${product.sku} • Barcode: ${product.barcode.ifBlank { "N/A" }} • Unit: ${product.unit} (1 ${product.purchaseUnit} = ${product.conversionFactor.toInt()} ${product.unit})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isPharmacy && product.genericName.isNotBlank()) {
                                    Text(
                                        text = "Generic: ${product.genericName} | Salt: ${product.saltFormula} | DRAP: ${product.drapRegNumber.ifBlank { "N/A" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DukaanSecondary
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "Cost: ${formatPkr(product.purchasePrice)}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Retail: ${formatPkr(product.sellingPrice)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DukaanSuccess
                                    )
                                    Text(
                                        text = "Wholesale: ${formatPkr(product.wholesalePrice)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DukaanInfo
                                    )
                                    Text(
                                        text = "Stock: ${product.currentStock} ${product.unit}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (product.currentStock <= product.reorderLevel) DukaanDanger else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        isCreatingNew = false
                                        editingProduct = product
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                                }
                                IconButton(onClick = { onToggleArchive(product) }) {
                                    Icon(
                                        imageVector = if (product.isActive) Icons.Default.Archive else Icons.Default.Unarchive,
                                        contentDescription = "Archive or Restore",
                                        tint = if (product.isActive) DukaanWarning else DukaanSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingProduct != null) {
        ProductEditorDialog(
            initial = editingProduct!!,
            isNew = isCreatingNew,
            isPharmacy = isPharmacy,
            onDismiss = { editingProduct = null },
            onSave = { updated ->
                onSaveProduct(updated, isCreatingNew)
                editingProduct = null
            }
        )
    }

    if (showBulkPriceDialog) {
        var pctText by remember { mutableStateOf("5") }
        Dialog(onDismissRequest = { showBulkPriceDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Bulk Price Adjustment (%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Increase or decrease retail and wholesale prices across all catalog products (e.g. 5 for +5%, -5 for -5%).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = pctText,
                        onValueChange = { pctText = it },
                        label = { Text("Percentage Change (%)") },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showBulkPriceDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pct = pctText.toDoubleOrNull() ?: 0.0
                                if (pct != 0.0) onBulkPriceChange(pct)
                                showBulkPriceDialog = false
                            }
                        ) {
                            Text("Apply to ${products.size} Products")
                        }
                    }
                }
            }
        }
    }

    if (showCsvExportDialog) {
        val csvContent = remember(products) {
            buildString {
                appendLine("SKU,Name,Barcode,Category,Unit,PurchasePrice,SellingPrice,WholesalePrice,CurrentStock")
                products.forEach { p ->
                    appendLine("${p.sku},\"${p.name}\",${p.barcode},\"${p.category}\",${p.unit},${p.purchasePrice},${p.sellingPrice},${p.wholesalePrice},${p.currentStock}")
                }
            }
        }
        Dialog(onDismissRequest = { showCsvExportDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CSV Catalog Export / Import Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = csvContent,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono)
                    )
                    Button(
                        onClick = { showCsvExportDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initial: Product,
    isNew: Boolean,
    isPharmacy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var sku by remember { mutableStateOf(initial.sku.ifBlank { "SKU-${System.currentTimeMillis().toString().takeLast(4)}" }) }
    var barcode by remember { mutableStateOf(initial.barcode) }
    var category by remember { mutableStateOf(initial.category) }
    var brand by remember { mutableStateOf(initial.brand) }
    var purchasePrice by remember { mutableStateOf(if (initial.purchasePrice > 0) initial.purchasePrice.toString() else "100") }
    var sellingPrice by remember { mutableStateOf(if (initial.sellingPrice > 0) initial.sellingPrice.toString() else "130") }
    var wholesalePrice by remember { mutableStateOf(if (initial.wholesalePrice > 0) initial.wholesalePrice.toString() else "115") }
    var unit by remember { mutableStateOf(initial.unit) }
    var purchaseUnit by remember { mutableStateOf(initial.purchaseUnit) }
    var conversionFactor by remember { mutableStateOf(initial.conversionFactor.toString()) }
    var openingStock by remember { mutableStateOf(initial.openingStock.toString()) }
    var reorderLevel by remember { mutableStateOf(initial.reorderLevel.toString()) }
    var isLooseWeight by remember { mutableStateOf(initial.isLooseWeight) }
    var genericName by remember { mutableStateOf(initial.genericName) }
    var saltFormula by remember { mutableStateOf(initial.saltFormula) }
    var manufacturer by remember { mutableStateOf(initial.manufacturer) }
    var drapReg by remember { mutableStateOf(initial.drapRegNumber) }
    var requiresRx by remember { mutableStateOf(initial.requiresPrescription) }

    val units = listOf("Piece", "Packet", "Box", "Kilogram", "Gram", "Litre", "Millilitre", "Dozen", "Strip", "Tablet", "Bottle")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add New Product" else "Edit Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product / Brand Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_editor_name_input"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand / Company") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Purchase Rs.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Retail Rs.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = wholesalePrice,
                        onValueChange = { wholesalePrice = it },
                        label = { Text("Wholesale Rs.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Selling Unit:", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseUnit,
                        onValueChange = { purchaseUnit = it },
                        label = { Text("Purchase Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = conversionFactor,
                        onValueChange = { conversionFactor = it },
                        label = { Text("Units per Purchase Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (isNew) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openingStock,
                            onValueChange = { openingStock = it },
                            label = { Text("Opening Stock") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reorderLevel,
                            onValueChange = { reorderLevel = it },
                            label = { Text("Low Stock Alert Level") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (isPharmacy) {
                    HorizontalDivider()
                    Text("Pharmacy / DRAP Metadata", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = genericName,
                            onValueChange = { genericName = it },
                            label = { Text("Generic Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = saltFormula,
                            onValueChange = { saltFormula = it },
                            label = { Text("Salt / Strength") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = manufacturer,
                            onValueChange = { manufacturer = it },
                            label = { Text("Manufacturer") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = drapReg,
                            onValueChange = { drapReg = it },
                            label = { Text("DRAP Reg #") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = requiresRx, onCheckedChange = { requiresRx = it })
                        Text("Requires Doctor Prescription (Rx)")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isLooseWeight, onCheckedChange = { isLooseWeight = it })
                        Text("Loose Weight Item (Sell by 250g, 500g, 1Kg, etc.)")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val openQty = openingStock.toDoubleOrNull() ?: 0.0
                                onSave(
                                    initial.copy(
                                        name = name.trim(),
                                        sku = sku.trim().ifBlank { "SKU-100" },
                                        barcode = barcode.trim(),
                                        category = category.trim().ifBlank { "General" },
                                        brand = brand.trim(),
                                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                                        unit = unit,
                                        purchaseUnit = purchaseUnit,
                                        conversionFactor = conversionFactor.toDoubleOrNull() ?: 1.0,
                                        openingStock = openQty,
                                        currentStock = if (isNew) openQty else initial.currentStock,
                                        reorderLevel = reorderLevel.toDoubleOrNull() ?: 10.0,
                                        isLooseWeight = isLooseWeight,
                                        genericName = genericName.trim(),
                                        saltFormula = saltFormula.trim(),
                                        manufacturer = manufacturer.trim(),
                                        drapRegNumber = drapReg.trim(),
                                        requiresPrescription = requiresRx
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_product_dialog_btn")
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PharmacyBatchesScreen(
    products: List<Product>,
    batches: List<MedicineBatch>,
    suppliers: List<Supplier>,
    onAddBatch: (MedicineBatch) -> Unit
) {
    var expiryFilter by remember { mutableStateOf("ALL") } // ALL, EXPIRED, 30D, 90D, 180D
    var showAddDialog by remember { mutableStateOf(false) }
    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    val filteredBatches = batches.filter { batch ->
        when (expiryFilter) {
            "EXPIRED" -> batch.expiryDate < todayIso
            "30D" -> batch.expiryDate >= todayIso && batch.expiryDate <= "2026-12-08"
            "90D" -> batch.expiryDate >= todayIso && batch.expiryDate <= "2027-02-08"
            "180D" -> batch.expiryDate >= todayIso && batch.expiryDate <= "2027-05-08"
            else -> true
        }
    }

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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pharmacy Batch & Expiry Control",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Traceable batch-wise stock, supplier history, and 180 / 90 / 30-day expiry alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        enabled = products.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Batch")
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ALL" to "All Batches (${batches.size})",
                        "EXPIRED" to "Expired",
                        "30D" to "< 30 Days",
                        "90D" to "< 90 Days",
                        "180D" to "< 180 Days"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = expiryFilter == key,
                            onClick = { expiryFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBatches, key = { it.id }) { batch ->
                val isExpired = batch.expiryDate < todayIso
                val isNear = !isExpired && batch.expiryDate <= "2027-05-08"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isExpired -> DukaanDanger
                            isNear -> DukaanWarning
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    )
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
                                Text(batch.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                StatusBadge(
                                    text = "Batch: ${batch.batchNumber}",
                                    color = DukaanPrimary
                                )
                                StatusBadge(
                                    text = when {
                                        isExpired -> "EXPIRED (${batch.expiryDate})"
                                        isNear -> "NEAR EXPIRY (${batch.expiryDate})"
                                        else -> "EXP: ${batch.expiryDate}"
                                    },
                                    color = when {
                                        isExpired -> DukaanDanger
                                        isNear -> DukaanWarning
                                        else -> DukaanSuccess
                                    }
                                )
                            }
                            Text(
                                text = "Mfg: ${batch.manufacturingDate.ifBlank { "N/A" }} • Supplier: ${batch.supplierName.ifBlank { "Direct" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Qty: ${batch.availableQty.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cost: ${formatPkr(batch.purchaseCost)} | Sell: ${formatPkr(batch.sellingPrice)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && products.isNotEmpty()) {
        var selectedProduct by remember { mutableStateOf(products.first()) }
        var batchNo by remember { mutableStateOf("B-2026-${(100..999).random()}") }
        var mfgDate by remember { mutableStateOf("2026-06-01") }
        var expDate by remember { mutableStateOf("2028-06-01") }
        var qty by remember { mutableStateOf("50") }
        var cost by remember { mutableStateOf(selectedProduct.purchasePrice.toString()) }
        var sell by remember { mutableStateOf(selectedProduct.sellingPrice.toString()) }
        var supplierName by remember { mutableStateOf(suppliers.firstOrNull()?.companyName ?: "Premier Agencies") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add Medicine Batch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Select Medicine:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        products.take(5).forEach { p ->
                            FilterChip(
                                selected = selectedProduct.id == p.id,
                                onClick = {
                                    selectedProduct = p
                                    cost = p.purchasePrice.toString()
                                    sell = p.sellingPrice.toString()
                                },
                                label = { Text(p.name) }
                            )
                        }
                    }
                    OutlinedTextField(value = batchNo, onValueChange = { batchNo = it }, label = { Text("Batch Number") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = mfgDate, onValueChange = { mfgDate = it }, label = { Text("Mfg (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = expDate, onValueChange = { expDate = it }, label = { Text("Exp (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Purchase Cost") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = sell, onValueChange = { sell = it }, label = { Text("Selling Price") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Supplier Name") }, singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onAddBatch(
                                    MedicineBatch(
                                        productId = selectedProduct.id,
                                        productName = selectedProduct.name,
                                        batchNumber = batchNo.trim(),
                                        manufacturingDate = mfgDate.trim(),
                                        expiryDate = expDate.trim(),
                                        availableQty = qty.toDoubleOrNull() ?: 10.0,
                                        purchaseCost = cost.toDoubleOrNull() ?: selectedProduct.purchasePrice,
                                        sellingPrice = sell.toDoubleOrNull() ?: selectedProduct.sellingPrice,
                                        supplierName = supplierName.trim()
                                    )
                                )
                                showAddDialog = false
                            }
                        ) {
                            Text("Save Batch & Increase Stock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesAndUnitsScreen(
    products: List<Product>
) {
    val grouped = products.groupBy { it.category }
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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Categories & Unit Conversions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Supported units: Piece, Packet, Box, Kilogram, Gram, Litre, Millilitre, Dozen, Strip, Tablet, Bottle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(grouped.entries.toList(), key = { it.key }) { (category, catProducts) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            StatusBadge("${catProducts.size} Products", DukaanPrimary)
                        }
                        catProducts.forEach { p ->
                            Text(
                                text = "• ${p.name}: 1 ${p.purchaseUnit} = ${p.conversionFactor.toInt()} ${p.unit} | Stock: ${p.currentStock} ${p.unit}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
