package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLog
import com.example.data.model.Business
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.DateRangeFilter
import com.example.data.model.DukaanModule
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.example.data.model.Supplier
import com.example.data.repository.DukaanRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DukaanViewModel(
    private val repository: DukaanRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedBusinessId = MutableStateFlow<String?>(null)
    val selectedBusinessId: StateFlow<String?> = _selectedBusinessId.asStateFlow()

    private val _currentModule = MutableStateFlow(DukaanModule.DASHBOARD)
    val currentModule: StateFlow<DukaanModule> = _currentModule.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateRangeFilter.ALL_TIME)
    val dateFilter: StateFlow<DateRangeFilter> = _dateFilter.asStateFlow()

    private val _activeEmployeeRole = MutableStateFlow("Owner")
    val activeEmployeeRole: StateFlow<String> = _activeEmployeeRole.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // POS State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _posDiscount = MutableStateFlow(0.0)
    val posDiscount: StateFlow<Double> = _posDiscount.asStateFlow()

    private val _posPaymentMethod = MutableStateFlow("CASH")
    val posPaymentMethod: StateFlow<String> = _posPaymentMethod.asStateFlow()

    private val _posPrescriptionRef = MutableStateFlow("")
    val posPrescriptionRef: StateFlow<String> = _posPrescriptionRef.asStateFlow()

    private val _posExpiryOverrideReason = MutableStateFlow("")
    val posExpiryOverrideReason: StateFlow<String> = _posExpiryOverrideReason.asStateFlow()

    private val _isOfflineQueueMode = MutableStateFlow(false)
    val isOfflineQueueMode: StateFlow<Boolean> = _isOfflineQueueMode.asStateFlow()

    private val _lastReceiptSale = MutableStateFlow<Sale?>(null)
    val lastReceiptSale: StateFlow<Sale?> = _lastReceiptSale.asStateFlow()

    val businesses: StateFlow<List<Business>> = repository.observeBusinesses(currentUserId)
        .catch { e -> handleStreamError("businesses", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeBusiness: StateFlow<Business?> = combine(businesses, _selectedBusinessId) { list, selectedId ->
        val found = list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
        if (found != null && _selectedBusinessId.value != found.id) {
            _selectedBusinessId.value = found.id
        }
        found
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private fun <T> businessScopedFlow(name: String, block: (String) -> Flow<List<T>>): StateFlow<List<T>> {
        return activeBusiness
            .flatMapLatest { biz ->
                if (biz == null) flowOf(emptyList())
                else block(biz.id).catch { e -> handleStreamError(name, e); emit(emptyList()) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    }

    val products: StateFlow<List<Product>> = businessScopedFlow("products") { bizId ->
        repository.observeProducts(currentUserId, bizId)
    }

    val medicineBatches: StateFlow<List<MedicineBatch>> = businessScopedFlow("medicine_batches") { bizId ->
        repository.observeBatches(currentUserId, bizId)
    }

    val sales: StateFlow<List<Sale>> = businessScopedFlow("sales") { bizId ->
        repository.observeSales(currentUserId, bizId)
    }

    val purchases: StateFlow<List<Purchase>> = businessScopedFlow("purchases") { bizId ->
        repository.observePurchases(currentUserId, bizId)
    }

    val stockMovements: StateFlow<List<StockMovement>> = businessScopedFlow("stock_movements") { bizId ->
        repository.observeStockMovements(currentUserId, bizId)
    }

    val customers: StateFlow<List<Customer>> = businessScopedFlow("customers") { bizId ->
        repository.observeCustomers(currentUserId, bizId)
    }

    val suppliers: StateFlow<List<Supplier>> = businessScopedFlow("suppliers") { bizId ->
        repository.observeSuppliers(currentUserId, bizId)
    }

    val expenses: StateFlow<List<Expense>> = businessScopedFlow("expenses") { bizId ->
        repository.observeExpenses(currentUserId, bizId)
    }

    val employees: StateFlow<List<Employee>> = businessScopedFlow("employees") { bizId ->
        repository.observeEmployees(currentUserId, bizId)
    }

    val auditLogs: StateFlow<List<AuditLog>> = businessScopedFlow("audit_logs") { bizId ->
        repository.observeAuditLogs(currentUserId, bizId)
    }

    private fun handleStreamError(streamName: String, error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w("DukaanVM", "Stream error in $streamName: ${firestoreError?.message ?: error.message}")
    }

    fun selectBusiness(businessId: String) {
        _selectedBusinessId.value = businessId
        clearCart()
    }

    fun navigateTo(module: DukaanModule) {
        _currentModule.value = module
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDateFilter(filter: DateRangeFilter) {
        _dateFilter.value = filter
    }

    fun setActiveEmployeeRole(role: String) {
        _activeEmployeeRole.value = role
        _statusMessage.value = "Switched active role view to: $role"
    }

    fun toggleOfflineQueueMode() {
        _isOfflineQueueMode.value = !_isOfflineQueueMode.value
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun dismissReceiptDialog() {
        _lastReceiptSale.value = null
    }

    fun showReceipt(sale: Sale) {
        _lastReceiptSale.value = sale
    }

    // --- POS CART ACTIONS ---

    fun addToCart(
        product: Product,
        batch: MedicineBatch? = null,
        quantity: Double = 1.0,
        useWholesale: Boolean = false
    ) {
        if (!product.isActive) {
            _statusMessage.value = "${product.name} is archived/inactive."
            return
        }
        val price = if (useWholesale && product.wholesalePrice > 0) {
            product.wholesalePrice
        } else {
            batch?.sellingPrice?.takeIf { it > 0 } ?: product.sellingPrice
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst {
            it.product.id == product.id && it.batch?.id == batch?.id && it.isWholesalePrice == useWholesale
        }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    product = product,
                    batch = batch,
                    quantity = quantity,
                    selectedUnit = product.unit,
                    unitPrice = price,
                    isWholesalePrice = useWholesale
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: String, batchId: String?, newQty: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId && it.batch?.id == batchId }
        if (index >= 0) {
            if (newQty <= 0.0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: String, batchId: String?) {
        _cartItems.value = _cartItems.value.filterNot {
            it.product.id == productId && it.batch?.id == batchId
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _posDiscount.value = 0.0
        _posPrescriptionRef.value = ""
        _posExpiryOverrideReason.value = ""
    }

    fun setPosCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setPosDiscount(discount: Double) {
        _posDiscount.value = max(0.0, discount)
    }

    fun setPosPaymentMethod(method: String) {
        _posPaymentMethod.value = method
    }

    fun setPosPrescriptionRef(ref: String) {
        _posPrescriptionRef.value = ref
    }

    fun setPosExpiryOverrideReason(reason: String) {
        _posExpiryOverrideReason.value = reason
    }

    // Check if any cart item has an expired batch (reference date 2026-11-08)
    fun hasExpiredBatchInCart(): Boolean {
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return _cartItems.value.any { item ->
            val exp = item.batch?.expiryDate.orEmpty()
            exp.isNotBlank() && exp < todayIso
        }
    }

    // --- REPOSITORY MUTATIONS ---

    fun createNewBusiness(
        name: String,
        businessType: String,
        branchName: String,
        city: String,
        phone: String,
        address: String,
        language: String,
        taxRatePercent: Double,
        subscriptionPlan: String,
        seedSample: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val biz = Business(
                name = name.trim().ifBlank {
                    if (businessType == "PHARMACY") "Al-Shifa Pharmacy" else "Madina Cash & Carry"
                },
                businessType = businessType,
                branchName = branchName.trim().ifBlank { "Main Branch" },
                city = city.trim().ifBlank { "Lahore" },
                phone = phone.trim(),
                address = address.trim(),
                language = language,
                taxRatePercent = taxRatePercent,
                subscriptionPlan = subscriptionPlan
            )
            repository.createBusiness(biz, seedSampleData = seedSample)
                .onSuccess { newId ->
                    _selectedBusinessId.value = newId
                    _statusMessage.value = "Created ${biz.name} (${biz.businessType.replace("_", " ")})"
                    onComplete()
                }
                .onFailure { e ->
                    _statusMessage.value = "Error creating business: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun seedSampleDataForActiveBusiness() {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            repository.seedBusinessSampleData(biz.id, biz.businessType == "PHARMACY")
                .onSuccess {
                    _statusMessage.value = "Loaded Pakistani sample catalog & ledger records for ${biz.name}"
                }
                .onFailure { e ->
                    _statusMessage.value = "Failed to load sample data: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun completeSale(paidAmount: Double) {
        val biz = activeBusiness.value ?: return
        val items = _cartItems.value
        if (items.isEmpty()) {
            _statusMessage.value = "Add at least one item to complete sale."
            return
        }
        if (hasExpiredBatchInCart() && _posExpiryOverrideReason.value.isBlank()) {
            _statusMessage.value = "Blocked: Cart contains an expired medicine batch. Remove it or provide an authorized override reason."
            return
        }
        val subtotal = items.sumOf { it.lineSubtotal }
        val itemTax = items.sumOf { it.lineTax }
        val bizTax = if (itemTax > 0) itemTax else subtotal * (biz.taxRatePercent / 100.0)

        viewModelScope.launch {
            _isBusy.value = true
            repository.completePosSale(
                businessId = biz.id,
                cartItems = items,
                customer = _selectedCustomer.value,
                discountAmount = _posDiscount.value,
                taxAmount = bizTax,
                paidAmount = paidAmount,
                paymentMethod = _posPaymentMethod.value,
                prescriptionRef = _posPrescriptionRef.value,
                expiryOverrideReason = _posExpiryOverrideReason.value,
                cashierName = _activeEmployeeRole.value,
                isOfflineQueueMode = _isOfflineQueueMode.value
            ).onSuccess { completedSale ->
                clearCart()
                _lastReceiptSale.value = completedSale
                _statusMessage.value = "Sale ${completedSale.invoiceNumber} completed (Rs. ${completedSale.grandTotal.toInt()})"
            }.onFailure { e ->
                _statusMessage.value = "Sale failed: ${e.localizedMessage}"
            }
            _isBusy.value = false
        }
    }

    fun saveProduct(product: Product, isNew: Boolean) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            repository.saveProduct(product.copy(businessId = biz.id), isNew)
                .onSuccess {
                    _statusMessage.value = if (isNew) "Product '${product.name}' added" else "Product '${product.name}' updated"
                }
                .onFailure { e ->
                    _statusMessage.value = "Failed to save product: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun toggleProductArchive(product: Product) {
        viewModelScope.launch {
            repository.archiveProduct(product)
                .onSuccess {
                    _statusMessage.value = if (product.isActive) "Archived ${product.name}" else "Restored ${product.name}"
                }
        }
    }

    fun applyBulkPriceChange(percent: Double) {
        val biz = activeBusiness.value ?: return
        val list = products.value
        if (list.isEmpty()) return
        viewModelScope.launch {
            _isBusy.value = true
            repository.bulkAdjustPrices(biz.id, list, percent)
                .onSuccess { count ->
                    _statusMessage.value = "Updated prices by $percent% across $count products"
                }
                .onFailure { e ->
                    _statusMessage.value = "Bulk price update failed: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun addMedicineBatch(batch: MedicineBatch) {
        val biz = activeBusiness.value ?: return
        val product = products.value.firstOrNull { it.id == batch.productId } ?: return
        viewModelScope.launch {
            _isBusy.value = true
            repository.addMedicineBatch(batch.copy(businessId = biz.id, productName = product.name), product.currentStock)
                .onSuccess {
                    _statusMessage.value = "Added Batch ${batch.batchNumber} (+${batch.availableQty.toInt()} ${product.unit})"
                }
                .onFailure { e ->
                    _statusMessage.value = "Failed to add batch: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun adjustStock(product: Product, delta: Double, movementType: String, reason: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.recordStockAdjustment(product, delta, movementType, reason, _activeEmployeeRole.value)
                .onSuccess {
                    _statusMessage.value = "Recorded $movementType for ${product.name}"
                }
                .onFailure { e ->
                    _statusMessage.value = "Stock adjustment failed: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun createPurchaseInvoice(
        supplier: Supplier?,
        product: Product,
        quantity: Double,
        unitCost: Double,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        batchNumber: String,
        expiryDate: String
    ) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            repository.recordPurchaseInvoice(
                businessId = biz.id,
                supplier = supplier,
                product = product,
                quantity = quantity,
                unitCost = unitCost,
                discountAmount = discount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                batchNumber = batchNumber,
                expiryDate = expiryDate
            ).onSuccess {
                _statusMessage.value = "Purchase recorded & stock increased (+${quantity.toInt()} ${product.unit})"
            }.onFailure { e ->
                _statusMessage.value = "Purchase error: ${e.localizedMessage}"
            }
            _isBusy.value = false
        }
    }

    fun processReturn(sale: Sale, reason: String) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            repository.processSaleReturn(sale, reason, biz.businessType == "PHARMACY")
                .onSuccess {
                    _statusMessage.value = "Processed return for ${sale.invoiceNumber}"
                }
                .onFailure { e ->
                    _statusMessage.value = "Return error: ${e.localizedMessage}"
                }
            _isBusy.value = false
        }
    }

    fun syncOfflineSales() {
        val biz = activeBusiness.value ?: return
        val pending = sales.value.filter { it.syncStatus == "PENDING_SYNC" }
        viewModelScope.launch {
            _isBusy.value = true
            repository.syncPendingOfflineSales(biz.id, pending)
                .onSuccess { count ->
                    _statusMessage.value = "Synchronized $count offline invoices with idempotency verification"
                }
            _isBusy.value = false
        }
    }

    fun addCustomer(name: String, phone: String, address: String, notes: String, openingUdhaar: Double) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            repository.addCustomer(
                Customer(
                    businessId = biz.id,
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    outstandingBalance = max(0.0, openingUdhaar),
                    notes = notes.trim()
                )
            ).onSuccess {
                _statusMessage.value = "Added customer $name"
            }
        }
    }

    fun receiveUdhaarPayment(customer: Customer, amount: Double, method: String) {
        viewModelScope.launch {
            repository.receiveCustomerUdhaarPayment(customer, amount, method)
                .onSuccess {
                    _statusMessage.value = "Received Rs. ${amount.toInt()} from ${customer.name}"
                }
        }
    }

    fun addSupplier(name: String, companyName: String, phone: String, address: String, openingPayable: Double) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            repository.addSupplier(
                Supplier(
                    businessId = biz.id,
                    name = name.trim(),
                    companyName = companyName.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    payableBalance = max(0.0, openingPayable)
                )
            ).onSuccess {
                _statusMessage.value = "Added supplier ${companyName.ifBlank { name }}"
            }
        }
    }

    fun paySupplier(supplier: Supplier, amount: Double, method: String) {
        viewModelScope.launch {
            repository.paySupplierBalance(supplier, amount, method)
                .onSuccess {
                    _statusMessage.value = "Paid Rs. ${amount.toInt()} to ${supplier.companyName.ifBlank { supplier.name }}"
                }
        }
    }

    fun addExpense(category: String, title: String, amount: Double, paymentMethod: String, status: String, notes: String) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    businessId = biz.id,
                    category = category,
                    title = title.trim(),
                    amount = amount,
                    paymentMethod = paymentMethod,
                    status = status,
                    notes = notes.trim(),
                    recordedBy = _activeEmployeeRole.value
                )
            ).onSuccess {
                _statusMessage.value = "Recorded expense: $title (Rs. ${amount.toInt()})"
            }
        }
    }

    fun addEmployee(name: String, phone: String, email: String, role: String, permissionsCsv: String) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            repository.addEmployee(
                Employee(
                    businessId = biz.id,
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    role = role,
                    permissionsCsv = permissionsCsv
                )
            ).onSuccess {
                _statusMessage.value = "Added employee $name ($role)"
            }
        }
    }

    fun updateSettings(
        name: String,
        branchName: String,
        city: String,
        phone: String,
        address: String,
        language: String,
        taxRatePercent: Double,
        receiptHeader: String,
        receiptFooter: String,
        subscriptionPlan: String,
        subscriptionBillingCycle: String
    ) {
        val biz = activeBusiness.value ?: return
        viewModelScope.launch {
            repository.updateBusinessSettings(
                businessId = biz.id,
                name = name,
                branchName = branchName,
                city = city,
                phone = phone,
                address = address,
                language = language,
                taxRatePercent = taxRatePercent,
                receiptHeader = receiptHeader,
                receiptFooter = receiptFooter,
                subscriptionPlan = subscriptionPlan,
                subscriptionBillingCycle = subscriptionBillingCycle
            ).onSuccess {
                _statusMessage.value = "Business settings saved"
            }
        }
    }
}
