package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Business
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
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
fun PosScreen(
    business: Business?,
    products: List<Product>,
    batches: List<MedicineBatch>,
    customers: List<Customer>,
    cartItems: List<CartItem>,
    selectedCustomer: Customer?,
    discountAmount: Double,
    paymentMethod: String,
    prescriptionRef: String,
    expiryOverrideReason: String,
    isOfflineQueueMode: Boolean,
    hasExpiredBatchInCart: Boolean,
    isBusy: Boolean,
    onAddToCart: (Product, MedicineBatch?, Double, Boolean) -> Unit,
    onUpdateCartQty: (String, String?, Double) -> Unit,
    onRemoveFromCart: (String, String?) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onPrescriptionRefChange: (String) -> Unit,
    onExpiryOverrideChange: (String) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onCompleteSale: (Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var useWholesalePricing by remember { mutableStateOf(false) }
    var showMobileCartView by remember { mutableStateOf(false) }

    val isPharmacy = business?.businessType == "PHARMACY"
    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = products.filter { product ->
        product.isActive &&
            (selectedCategory == "All" || product.category == selectedCategory) &&
            (searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true) ||
                product.genericName.contains(searchQuery, ignoreCase = true) ||
                product.saltFormula.contains(searchQuery, ignoreCase = true))
    }

    val subtotal = cartItems.sumOf { it.lineSubtotal }
    val itemTax = cartItems.sumOf { it.lineTax }
    val taxAmount = if (itemTax > 0) itemTax else subtotal * ((business?.taxRatePercent ?: 0.0) / 100.0)
    val grandTotal = max(0.0, subtotal - discountAmount + taxAmount)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Pane: Product Catalog & Barcode Search
                Column(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PosSearchAndToolbar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        isPharmacy = isPharmacy,
                        useWholesalePricing = useWholesalePricing,
                        onToggleWholesale = { useWholesalePricing = !useWholesalePricing },
                        isOfflineQueueMode = isOfflineQueueMode,
                        onToggleOfflineMode = onToggleOfflineMode,
                        onQuickScanBarcode = {
                            val firstMatch = products.firstOrNull { it.barcode == searchQuery.trim() }
                                ?: filteredProducts.firstOrNull()
                            if (firstMatch != null) {
                                val defaultBatch = batches.firstOrNull {
                                    it.productId == firstMatch.id && it.availableQty > 0 && it.expiryDate >= todayIso
                                } ?: batches.firstOrNull { it.productId == firstMatch.id }
                                onAddToCart(firstMatch, defaultBatch, 1.0, useWholesalePricing)
                            }
                        }
                    )

                    PosProductList(
                        products = filteredProducts,
                        batches = batches,
                        isPharmacy = isPharmacy,
                        todayIso = todayIso,
                        useWholesalePricing = useWholesalePricing,
                        onAddToCart = onAddToCart,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right Pane: Shopping Cart & Checkout
                PosCartAndCheckoutPanel(
                    isPharmacy = isPharmacy,
                    cartItems = cartItems,
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    subtotal = subtotal,
                    discountAmount = discountAmount,
                    taxAmount = taxAmount,
                    grandTotal = grandTotal,
                    paymentMethod = paymentMethod,
                    prescriptionRef = prescriptionRef,
                    expiryOverrideReason = expiryOverrideReason,
                    hasExpiredBatchInCart = hasExpiredBatchInCart,
                    isBusy = isBusy,
                    onUpdateCartQty = onUpdateCartQty,
                    onRemoveFromCart = onRemoveFromCart,
                    onClearCart = onClearCart,
                    onSelectCustomer = onSelectCustomer,
                    onDiscountChange = onDiscountChange,
                    onPaymentMethodChange = onPaymentMethodChange,
                    onPrescriptionRefChange = onPrescriptionRefChange,
                    onExpiryOverrideChange = onExpiryOverrideChange,
                    onCompleteSale = onCompleteSale,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Compact Mobile Layout with fast Catalog <-> Cart toggle bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showMobileCartView = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showMobileCartView) DukaanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!showMobileCartView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Products (${filteredProducts.size})")
                    }
                    Button(
                        onClick = { showMobileCartView = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mobile_pos_cart_tab_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showMobileCartView) DukaanSuccess else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (showMobileCartView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cart (${cartItems.size}) • ${formatPkr(grandTotal)}")
                    }
                }

                if (!showMobileCartView) {
                    PosSearchAndToolbar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        isPharmacy = isPharmacy,
                        useWholesalePricing = useWholesalePricing,
                        onToggleWholesale = { useWholesalePricing = !useWholesalePricing },
                        isOfflineQueueMode = isOfflineQueueMode,
                        onToggleOfflineMode = onToggleOfflineMode,
                        onQuickScanBarcode = {
                            val firstMatch = products.firstOrNull { it.barcode == searchQuery.trim() }
                                ?: filteredProducts.firstOrNull()
                            if (firstMatch != null) {
                                val defaultBatch = batches.firstOrNull {
                                    it.productId == firstMatch.id && it.availableQty > 0 && it.expiryDate >= todayIso
                                } ?: batches.firstOrNull { it.productId == firstMatch.id }
                                onAddToCart(firstMatch, defaultBatch, 1.0, useWholesalePricing)
                            }
                        }
                    )

                    PosProductList(
                        products = filteredProducts,
                        batches = batches,
                        isPharmacy = isPharmacy,
                        todayIso = todayIso,
                        useWholesalePricing = useWholesalePricing,
                        onAddToCart = onAddToCart,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    PosCartAndCheckoutPanel(
                        isPharmacy = isPharmacy,
                        cartItems = cartItems,
                        customers = customers,
                        selectedCustomer = selectedCustomer,
                        subtotal = subtotal,
                        discountAmount = discountAmount,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        paymentMethod = paymentMethod,
                        prescriptionRef = prescriptionRef,
                        expiryOverrideReason = expiryOverrideReason,
                        hasExpiredBatchInCart = hasExpiredBatchInCart,
                        isBusy = isBusy,
                        onUpdateCartQty = onUpdateCartQty,
                        onRemoveFromCart = onRemoveFromCart,
                        onClearCart = onClearCart,
                        onSelectCustomer = onSelectCustomer,
                        onDiscountChange = onDiscountChange,
                        onPaymentMethodChange = onPaymentMethodChange,
                        onPrescriptionRefChange = onPrescriptionRefChange,
                        onExpiryOverrideChange = onExpiryOverrideChange,
                        onCompleteSale = onCompleteSale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PosSearchAndToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    isPharmacy: Boolean,
    useWholesalePricing: Boolean,
    onToggleWholesale: () -> Unit,
    isOfflineQueueMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    onQuickScanBarcode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            if (isPharmacy) "Search brand, generic salt (e.g. Paracetamol), SKU, barcode…"
                            else "Search item name, SKU, or scan barcode…"
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_input"),
                    singleLine = true
                )
                Button(
                    onClick = onQuickScanBarcode,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("pos_barcode_scan_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = DukaanSecondary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = useWholesalePricing,
                        onClick = onToggleWholesale,
                        label = { Text(if (useWholesalePricing) "Wholesale Tier Active" else "Retail Price Tier") }
                    )
                    FilterChip(
                        selected = isOfflineQueueMode,
                        onClick = onToggleOfflineMode,
                        leadingIcon = {
                            Icon(
                                imageVector = if (isOfflineQueueMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(if (isOfflineQueueMode) "Offline Queue Mode" else "Online Synced") }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PosProductList(
    products: List<Product>,
    batches: List<MedicineBatch>,
    isPharmacy: Boolean,
    todayIso: String,
    useWholesalePricing: Boolean,
    onAddToCart: (Product, MedicineBatch?, Double, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            val productBatches = batches.filter { it.productId == product.id }
            val activePrice = if (useWholesalePricing && product.wholesalePrice > 0) {
                product.wholesalePrice
            } else {
                product.sellingPrice
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (product.requiresPrescription) {
                                    StatusBadge("Rx Required", DukaanWarning)
                                }
                                if (product.isLooseWeight) {
                                    StatusBadge("Weight-Based", DukaanInfo)
                                }
                            }
                            if (isPharmacy && product.genericName.isNotBlank()) {
                                Text(
                                    text = "Generic: ${product.genericName} (${product.saltFormula}) • ${product.manufacturer}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DukaanPrimary
                                )
                            } else {
                                Text(
                                    text = "SKU: ${product.sku} • Barcode: ${product.barcode.ifBlank { "N/A" }} • ${product.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${formatPkr(activePrice)} / ${product.unit}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                                color = DukaanSuccess
                            )
                            Text(
                                text = "Stock: ${if (product.currentStock % 1.0 == 0.0) product.currentStock.toInt() else product.currentStock} ${product.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (product.currentStock <= product.reorderLevel) DukaanDanger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pharmacy Batch Selection Chips OR Loose Weight Quick Buttons
                    if (isPharmacy && productBatches.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            productBatches.forEach { batch ->
                                val isExpired = batch.expiryDate.isNotBlank() && batch.expiryDate < todayIso
                                OutlinedButton(
                                    onClick = { onAddToCart(product, batch, 1.0, useWholesalePricing) },
                                    border = BorderStroke(1.dp, if (isExpired) DukaanDanger else DukaanPrimary)
                                ) {
                                    Text(
                                        text = if (isExpired) "EXPIRED Batch ${batch.batchNumber} (${batch.expiryDate})"
                                        else "+ Batch ${batch.batchNumber} (Exp: ${batch.expiryDate} • Qty: ${batch.availableQty.toInt()})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isExpired) DukaanDanger else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else if (product.isLooseWeight) {
                        // General Store Weight-Based Billing Buttons (250g, 500g, 1Kg, 2Kg, 5Kg)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = "Weight",
                                tint = DukaanInfo,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            listOf(
                                0.25 to "250g (1 Pao)",
                                0.50 to "500g (Adha Kg)",
                                1.0 to "1 Kg",
                                2.5 to "2.5 Kg",
                                5.0 to "5 Kg (1 Dadi)"
                            ).forEach { (kgWeight, label) ->
                                OutlinedButton(
                                    onClick = { onAddToCart(product, null, kgWeight, useWholesalePricing) }
                                ) {
                                    Text("+ $label", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (product.conversionFactor > 1.0) {
                                OutlinedButton(
                                    onClick = { onAddToCart(product, null, product.conversionFactor, useWholesalePricing) },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("+ 1 ${product.purchaseUnit} (${product.conversionFactor.toInt()} ${product.unit})")
                                }
                            }
                            Button(
                                onClick = { onAddToCart(product, null, 1.0, useWholesalePricing) },
                                modifier = Modifier.testTag("add_to_cart_${product.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add 1 ${product.unit}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PosCartAndCheckoutPanel(
    isPharmacy: Boolean,
    cartItems: List<CartItem>,
    customers: List<Customer>,
    selectedCustomer: Customer?,
    subtotal: Double,
    discountAmount: Double,
    taxAmount: Double,
    grandTotal: Double,
    paymentMethod: String,
    prescriptionRef: String,
    expiryOverrideReason: String,
    hasExpiredBatchInCart: Boolean,
    isBusy: Boolean,
    onUpdateCartQty: (String, String?, Double) -> Unit,
    onRemoveFromCart: (String, String?) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onPrescriptionRefChange: (String) -> Unit,
    onExpiryOverrideChange: (String) -> Unit,
    onCompleteSale: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var discountInput by remember(discountAmount) {
        mutableStateOf(if (discountAmount == 0.0) "" else discountAmount.toInt().toString())
    }
    var customPaidInput by remember(grandTotal, paymentMethod) {
        mutableStateOf(if (paymentMethod == "CREDIT") "0" else grandTotal.toInt().toString())
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Bill (${cartItems.size} items)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (cartItems.isNotEmpty()) {
                    OutlinedButton(onClick = onClearCart) {
                        Text("Clear")
                    }
                }
            }

            // Customer Selector (Walk-in vs Udhaar Customer)
            Text("Customer (Walk-in or Udhaar Account):", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedCustomer == null,
                    onClick = { onSelectCustomer(null) },
                    label = { Text("Walk-in") }
                )
                customers.take(4).forEach { cust ->
                    FilterChip(
                        selected = selectedCustomer?.id == cust.id,
                        onClick = { onSelectCustomer(cust) },
                        label = {
                            Text(
                                text = "${cust.name} (${formatPkr(cust.outstandingBalance)})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            // Cart Line Items
            if (cartItems.isEmpty()) {
                Text(
                    text = "Tap any product or scan a barcode to add items to the invoice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                cartItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = buildString {
                                        append("${formatPkr(item.unitPrice)} x ")
                                        append(if (item.quantity % 1.0 == 0.0) item.quantity.toInt() else item.quantity)
                                        append(" ${item.selectedUnit}")
                                        if (item.batch != null) append(" • Batch: ${item.batch.batchNumber}")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val step = if (item.product.isLooseWeight) 0.25 else 1.0
                                        onUpdateCartQty(item.product.id, item.batch?.id, item.quantity - step)
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                                }
                                Text(
                                    text = formatPkr(item.lineSubtotal),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        val step = if (item.product.isLooseWeight) 0.25 else 1.0
                                        onUpdateCartQty(item.product.id, item.batch?.id, item.quantity + step)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                                }
                                IconButton(
                                    onClick = { onRemoveFromCart(item.product.id, item.batch?.id) }
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Remove item",
                                        tint = DukaanDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expired Medicine Safety Gate
            if (hasExpiredBatchInCart) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DukaanDanger.copy(alpha = 0.14f)),
                    border = BorderStroke(1.dp, DukaanDanger)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = DukaanDanger)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXPIRED BATCH SAFETY BLOCK",
                                style = MaterialTheme.typography.titleSmall,
                                color = DukaanDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "DukaanOS prevents expired medicine sales by default. Remove the expired batch or enter an authorized Pharmacist Override Reason (recorded in Audit Log):",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = expiryOverrideReason,
                            onValueChange = onExpiryOverrideChange,
                            label = { Text("Authorized Override Reason") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            if (isPharmacy) {
                OutlinedTextField(
                    value = prescriptionRef,
                    onValueChange = onPrescriptionRefChange,
                    label = { Text("Doctor / Prescription Reference (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Discount & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = {
                        discountInput = it
                        onDiscountChange(it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Discount (Rs.)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = customPaidInput,
                    onValueChange = { customPaidInput = it },
                    label = { Text("Amount Paid (Rs.)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text("Payment Method (Recorded Transaction):", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("CASH", "JAZZCASH", "EASYPAISA", "BANK_TRANSFER", "CREDIT").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = {
                            onPaymentMethodChange(method)
                            customPaidInput = if (method == "CREDIT") "0" else grandTotal.toInt().toString()
                        },
                        label = { Text(method.replace("_", " ")) }
                    )
                }
            }

            HorizontalDivider()

            // Totals Summary
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                Text(formatPkr(subtotal), style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono))
            }
            if (discountAmount > 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Discount", style = MaterialTheme.typography.bodySmall, color = DukaanWarning)
                    Text("- ${formatPkr(discountAmount)}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono))
                }
            }
            if (taxAmount > 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Tax (GST)", style = MaterialTheme.typography.bodySmall)
                    Text("+ ${formatPkr(taxAmount)}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono))
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("GRAND TOTAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = formatPkr(grandTotal),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                    fontWeight = FontWeight.Bold,
                    color = DukaanPrimary
                )
            }

            val paidDouble = customPaidInput.toDoubleOrNull() ?: grandTotal
            val creditPortion = max(0.0, grandTotal - paidDouble)
            if (creditPortion > 0) {
                Text(
                    text = "Udhaar / Credit Balance to Record: ${formatPkr(creditPortion)} (${selectedCustomer?.name ?: "Select a customer for Udhaar"})",
                    style = MaterialTheme.typography.bodySmall,
                    color = DukaanWarning
                )
            }

            Button(
                onClick = { onCompleteSale(paidDouble) },
                enabled = cartItems.isNotEmpty() && !isBusy && (!hasExpiredBatchInCart || expiryOverrideReason.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("complete_pos_sale_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DukaanSuccess)
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Sale • ${formatPkr(grandTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
