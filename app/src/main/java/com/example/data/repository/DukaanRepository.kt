package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AuditLog
import com.example.data.model.Business
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.example.data.model.Supplier
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.data.seed.SampleDataCatalog
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlin.math.max
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DukaanRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun newDocId(prefix: String): String {
        return "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    }

    // --- REALTIME OBSERVATION FLOWS ---

    fun observeBusinesses(userId: String = requireUserId()): Flow<List<Business>> = flow {
        val path = "businesses"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Business::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeProducts(userId: String = requireUserId(), businessId: String): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedBy { it.name.lowercase() }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeBatches(userId: String = requireUserId(), businessId: String): Flow<List<MedicineBatch>> = flow {
        val path = "medicine_batches"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MedicineBatch::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedBy { it.expiryDate }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeSales(userId: String = requireUserId(), businessId: String): Flow<List<Sale>> = flow {
        val path = "sales"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Sale::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observePurchases(userId: String = requireUserId(), businessId: String): Flow<List<Purchase>> = flow {
        val path = "purchases"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Purchase::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeStockMovements(userId: String = requireUserId(), businessId: String): Flow<List<StockMovement>> = flow {
        val path = "stock_movements"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(StockMovement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeCustomers(userId: String = requireUserId(), businessId: String): Flow<List<Customer>> = flow {
        val path = "customers"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Customer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.outstandingBalance }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeSuppliers(userId: String = requireUserId(), businessId: String): Flow<List<Supplier>> = flow {
        val path = "suppliers"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Supplier::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.payableBalance }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeExpenses(userId: String = requireUserId(), businessId: String): Flow<List<Expense>> = flow {
        val path = "expenses"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Expense::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeEmployees(userId: String = requireUserId(), businessId: String): Flow<List<Employee>> = flow {
        val path = "employees"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Employee::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedBy { it.name }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAuditLogs(userId: String = requireUserId(), businessId: String): Flow<List<AuditLog>> = flow {
        val path = "audit_logs"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerUid", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.businessId == businessId }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logIfSignedIn(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    private fun logIfSignedIn(error: Throwable, uid: String, op: OperationType, path: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    // --- BUSINESS ONBOARDING & SAMPLE DATA SEEDING ---

    suspend fun createBusiness(
        business: Business,
        seedSampleData: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val bizId = business.id.ifBlank { newDocId("biz") }
        val docRef = db.collection("businesses").document(bizId)
        try {
            val cleanBiz = business.copy(
                id = bizId,
                ownerUid = uid,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(cleanBiz).await()
            if (seedSampleData) {
                seedBusinessSampleData(bizId, business.businessType == "PHARMACY")
            }
            bizId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun seedBusinessSampleData(businessId: String, isPharmacy: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val batch = db.batch()

        val products = if (isPharmacy) {
            SampleDataCatalog.pharmacyProducts(uid, businessId)
        } else {
            SampleDataCatalog.generalStoreProducts(uid, businessId)
        }

        products.forEach { product ->
            val pRef = db.collection("products").document(product.id)
            batch.set(pRef, product.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))

            val movId = newDocId("mov")
            val movRef = db.collection("stock_movements").document(movId)
            batch.set(
                movRef,
                StockMovement(
                    id = movId,
                    ownerUid = uid,
                    businessId = businessId,
                    productId = product.id,
                    productName = product.name,
                    batchNumber = if (isPharmacy) "INITIAL-BATCH" else "",
                    quantityChange = product.openingStock,
                    stockAfter = product.currentStock,
                    movementType = "OPENING",
                    reason = "Opening stock seeded on onboarding",
                    referenceId = "OPENING-$businessId",
                    performedBy = "Owner",
                    createdAt = null
                )
            )
        }

        if (isPharmacy) {
            SampleDataCatalog.pharmacyBatches(uid, businessId).forEach { medBatch ->
                val bRef = db.collection("medicine_batches").document(medBatch.id)
                batch.set(bRef, medBatch.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))
            }
        }

        SampleDataCatalog.defaultCustomers(uid, businessId).forEach { customer ->
            val cRef = db.collection("customers").document(customer.id)
            batch.set(cRef, customer.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))
        }

        SampleDataCatalog.defaultSuppliers(uid, businessId, isPharmacy).forEach { supplier ->
            val sRef = db.collection("suppliers").document(supplier.id)
            batch.set(sRef, supplier.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))
        }

        SampleDataCatalog.defaultExpenses(uid, businessId).forEach { expense ->
            val eRef = db.collection("expenses").document(expense.id)
            batch.set(eRef, expense.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))
        }

        SampleDataCatalog.defaultEmployees(uid, businessId).forEach { employee ->
            val empRef = db.collection("employees").document(employee.id)
            batch.set(empRef, employee.copy(ownerUid = uid, businessId = businessId, createdAt = null, updatedAt = null))
        }

        // Seed 2 realistic completed POS sales so Dashboard charts and profit metrics are immediately live
        val suffix = businessId.takeLast(4)
        val sampleSale1Id = "sale_seed1_$suffix"
        val sampleSale1 = if (isPharmacy) {
            Sale(
                id = sampleSale1Id,
                ownerUid = uid,
                businessId = businessId,
                invoiceNumber = "INV-PK-1001",
                customerId = "cust_uzma_$suffix",
                customerName = "Dr. Uzma Rashid",
                itemsSummary = "2x Augmentin 625mg @ Rs.365, 3x Panadol Advance @ Rs.45",
                itemCount = 5L,
                subtotal = 865.0,
                discountAmount = 15.0,
                taxAmount = 0.0,
                grandTotal = 850.0,
                totalCostSnapshot = 734.0,
                grossProfit = 116.0,
                paidAmount = 850.0,
                creditAmount = 0.0,
                paymentMethod = "CASH",
                prescriptionRef = "RX-LHR-9921",
                expiryOverrideReason = "",
                status = "COMPLETED",
                syncStatus = "SYNCED",
                idempotencyKey = "idem_seed_1_$suffix",
                cashierName = "Owner"
            )
        } else {
            Sale(
                id = sampleSale1Id,
                ownerUid = uid,
                businessId = businessId,
                invoiceNumber = "INV-PK-1001",
                customerId = "cust_tariq_$suffix",
                customerName = "Haji Tariq Mehmood",
                itemsSummary = "5 Kg Guard Basmati Rice @ Rs.360, 1x Dalda Oil 5L @ Rs.2650",
                itemCount = 2L,
                subtotal = 4450.0,
                discountAmount = 50.0,
                taxAmount = 0.0,
                grandTotal = 4400.0,
                totalCostSnapshot = 4030.0,
                grossProfit = 370.0,
                paidAmount = 3400.0,
                creditAmount = 1000.0,
                paymentMethod = "JAZZCASH",
                prescriptionRef = "",
                expiryOverrideReason = "",
                status = "COMPLETED",
                syncStatus = "SYNCED",
                idempotencyKey = "idem_seed_1_$suffix",
                cashierName = "Owner"
            )
        }
        batch.set(db.collection("sales").document(sampleSale1Id), sampleSale1)

        val logId = newDocId("log")
        batch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = businessId,
                action = "BUSINESS_INITIALIZED",
                module = "ONBOARDING",
                details = "Initialized ${if (isPharmacy) "Pharmacy" else "General Store"} catalog and opening stock ledger",
                performedBy = "Owner",
                createdAt = null
            )
        )

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "batch/seed/$businessId")
            throw e
        }
    }

    suspend fun updateBusinessSettings(
        businessId: String,
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
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("businesses").document(businessId)
        try {
            docRef.update(
                mapOf(
                    "name" to name.trim(),
                    "branchName" to branchName.trim(),
                    "city" to city.trim(),
                    "phone" to phone.trim(),
                    "address" to address.trim(),
                    "language" to language,
                    "taxRatePercent" to taxRatePercent,
                    "receiptHeader" to receiptHeader.trim(),
                    "receiptFooter" to receiptFooter.trim(),
                    "subscriptionPlan" to subscriptionPlan,
                    "subscriptionBillingCycle" to subscriptionBillingCycle,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- PRODUCT CRUD & BULK OPERATIONS ---

    suspend fun saveProduct(product: Product, isNew: Boolean): Result<String> = runCatching {
        val uid = requireUserId()
        val prodId = product.id.ifBlank { newDocId("prod") }
        val docRef = db.collection("products").document(prodId)
        try {
            if (isNew) {
                val batch = db.batch()
                val clean = product.copy(
                    id = prodId,
                    ownerUid = uid,
                    createdAt = null,
                    updatedAt = null
                )
                batch.set(docRef, clean)

                if (product.openingStock > 0) {
                    val movId = newDocId("mov")
                    val movRef = db.collection("stock_movements").document(movId)
                    batch.set(
                        movRef,
                        StockMovement(
                            id = movId,
                            ownerUid = uid,
                            businessId = product.businessId,
                            productId = prodId,
                            productName = product.name,
                            batchNumber = "",
                            quantityChange = product.openingStock,
                            stockAfter = product.currentStock,
                            movementType = "OPENING",
                            reason = "Product opening stock",
                            referenceId = prodId,
                            performedBy = "Owner",
                            createdAt = null
                        )
                    )
                }
                batch.commit().await()
            } else {
                docRef.update(
                    mapOf(
                        "name" to product.name.trim(),
                        "sku" to product.sku.trim(),
                        "barcode" to product.barcode.trim(),
                        "category" to product.category.trim(),
                        "brand" to product.brand.trim(),
                        "description" to product.description.trim(),
                        "purchasePrice" to product.purchasePrice,
                        "sellingPrice" to product.sellingPrice,
                        "wholesalePrice" to product.wholesalePrice,
                        "unit" to product.unit,
                        "purchaseUnit" to product.purchaseUnit,
                        "conversionFactor" to max(1.0, product.conversionFactor),
                        "reorderLevel" to product.reorderLevel,
                        "taxPercent" to product.taxPercent,
                        "isActive" to product.isActive,
                        "isLooseWeight" to product.isLooseWeight,
                        "genericName" to product.genericName.trim(),
                        "saltFormula" to product.saltFormula.trim(),
                        "manufacturer" to product.manufacturer.trim(),
                        "strength" to product.strength.trim(),
                        "dosageForm" to product.dosageForm.trim(),
                        "drapRegNumber" to product.drapRegNumber.trim(),
                        "requiresPrescription" to product.requiresPrescription,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            prodId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun getProductById(productId: String): Result<Product?> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            val snap = docRef.get().await()
            snap.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun archiveProduct(product: Product): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(product.id)
        try {
            docRef.update(
                mapOf(
                    "isActive" to !product.isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun bulkAdjustPrices(
        businessId: String,
        products: List<Product>,
        percentageChange: Double
    ): Result<Int> = runCatching {
        val uid = requireUserId()
        val batch = db.batch()
        val multiplier = 1.0 + (percentageChange / 100.0)
        products.forEach { product ->
            val pRef = db.collection("products").document(product.id)
            val newSell = kotlin.math.round(product.sellingPrice * multiplier * 100.0) / 100.0
            val newWholesale = kotlin.math.round(product.wholesalePrice * multiplier * 100.0) / 100.0
            batch.update(
                pRef,
                mapOf(
                    "sellingPrice" to max(1.0, newSell),
                    "wholesalePrice" to max(1.0, newWholesale),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        val logId = newDocId("log")
        batch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = businessId,
                action = "BULK_PRICE_UPDATE",
                module = "PRODUCTS",
                details = "Adjusted prices by $percentageChange% across ${products.size} products",
                performedBy = "Owner",
                createdAt = null
            )
        )
        try {
            batch.commit().await()
            products.size
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "products/bulk_price")
            throw e
        }
    }

    // --- PHARMACY BATCH MANAGEMENT ---

    suspend fun addMedicineBatch(batchModel: MedicineBatch, currentProductStock: Double): Result<String> = runCatching {
        val uid = requireUserId()
        val batchId = batchModel.id.ifBlank { newDocId("batch") }
        val writeBatch = db.batch()

        val batchRef = db.collection("medicine_batches").document(batchId)
        writeBatch.set(
            batchRef,
            batchModel.copy(
                id = batchId,
                ownerUid = uid,
                createdAt = null,
                updatedAt = null
            )
        )

        val prodRef = db.collection("products").document(batchModel.productId)
        val newStock = currentProductStock + batchModel.availableQty
        writeBatch.update(
            prodRef,
            mapOf(
                "currentStock" to newStock,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        val movId = newDocId("mov")
        writeBatch.set(
            db.collection("stock_movements").document(movId),
            StockMovement(
                id = movId,
                ownerUid = uid,
                businessId = batchModel.businessId,
                productId = batchModel.productId,
                productName = batchModel.productName,
                batchNumber = batchModel.batchNumber,
                quantityChange = batchModel.availableQty,
                stockAfter = newStock,
                movementType = "PURCHASE",
                reason = "Added pharmacy batch ${batchModel.batchNumber} (Exp: ${batchModel.expiryDate})",
                referenceId = batchId,
                performedBy = "Owner",
                createdAt = null
            )
        )

        try {
            writeBatch.commit().await()
            batchId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, batchRef.path)
            throw e
        }
    }

    // --- ATOMIC POINT OF SALE (POS) BILLING & INVENTORY LEDGER ---

    suspend fun completePosSale(
        businessId: String,
        cartItems: List<CartItem>,
        customer: Customer?,
        discountAmount: Double,
        taxAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        prescriptionRef: String,
        expiryOverrideReason: String,
        cashierName: String,
        isOfflineQueueMode: Boolean = false
    ): Result<Sale> = runCatching {
        val uid = requireUserId()
        require(cartItems.isNotEmpty()) { "Cart cannot be empty" }

        val subtotal = cartItems.sumOf { it.lineSubtotal }
        val grandTotal = max(0.0, subtotal - discountAmount + taxAmount)
        val totalCostSnapshot = cartItems.sumOf { it.lineCostSnapshot }
        val grossProfit = grandTotal - taxAmount - totalCostSnapshot
        val creditAmount = max(0.0, grandTotal - paidAmount)

        val saleId = newDocId("sale")
        val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
        val idempotencyKey = UUID.randomUUID().toString()
        val itemsSummary = cartItems.joinToString(", ") { item ->
            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format("%.2f", item.quantity)
            "${qtyStr} ${item.selectedUnit} ${item.product.name} @ Rs.${item.unitPrice.toInt()}"
        }.take(950)

        val sale = Sale(
            id = saleId,
            ownerUid = uid,
            businessId = businessId,
            invoiceNumber = invoiceNumber,
            customerId = customer?.id ?: "",
            customerName = customer?.name ?: "Walk-in Customer",
            itemsSummary = itemsSummary,
            itemCount = cartItems.size.toLong(),
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            totalCostSnapshot = totalCostSnapshot,
            grossProfit = grossProfit,
            paidAmount = paidAmount,
            creditAmount = creditAmount,
            paymentMethod = paymentMethod,
            prescriptionRef = prescriptionRef.trim(),
            expiryOverrideReason = expiryOverrideReason.trim(),
            status = "COMPLETED",
            syncStatus = if (isOfflineQueueMode) "PENDING_SYNC" else "SYNCED",
            idempotencyKey = idempotencyKey,
            cashierName = cashierName.ifBlank { "Owner" },
            createdAt = null,
            updatedAt = null
        )

        val writeBatch = db.batch()
        val saleRef = db.collection("sales").document(saleId)
        writeBatch.set(saleRef, sale)

        // Deduct stock and append stock movement ledger entries atomically
        cartItems.forEach { item ->
            val pRef = db.collection("products").document(item.product.id)
            val newStock = max(0.0, item.product.currentStock - item.quantity)
            writeBatch.update(
                pRef,
                mapOf(
                    "currentStock" to newStock,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            if (item.batch != null && item.batch.id.isNotBlank()) {
                val bRef = db.collection("medicine_batches").document(item.batch.id)
                val newBatchQty = max(0.0, item.batch.availableQty - item.quantity)
                writeBatch.update(
                    bRef,
                    mapOf(
                        "availableQty" to newBatchQty,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val movId = newDocId("mov")
            writeBatch.set(
                db.collection("stock_movements").document(movId),
                StockMovement(
                    id = movId,
                    ownerUid = uid,
                    businessId = businessId,
                    productId = item.product.id,
                    productName = item.product.name,
                    batchNumber = item.batch?.batchNumber ?: "",
                    quantityChange = -item.quantity,
                    stockAfter = newStock,
                    movementType = "SALE",
                    reason = "POS Sale $invoiceNumber",
                    referenceId = saleId,
                    performedBy = cashierName.ifBlank { "Owner" },
                    createdAt = null
                )
            )
        }

        // Update customer Udhaar & purchase history if customer selected
        if (customer != null && customer.id.isNotBlank()) {
            val custRef = db.collection("customers").document(customer.id)
            writeBatch.update(
                custRef,
                mapOf(
                    "outstandingBalance" to (customer.outstandingBalance + creditAmount),
                    "totalPurchases" to (customer.totalPurchases + grandTotal),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // Audit record for sale (and expiry override if applicable)
        val auditId = newDocId("log")
        val auditDetails = if (expiryOverrideReason.isNotBlank()) {
            "Invoice $invoiceNumber (Rs.${grandTotal.toInt()}) completed with EXPIRY OVERRIDE: $expiryOverrideReason"
        } else {
            "Invoice $invoiceNumber completed for Rs.${grandTotal.toInt()} via $paymentMethod"
        }
        writeBatch.set(
            db.collection("audit_logs").document(auditId),
            AuditLog(
                id = auditId,
                ownerUid = uid,
                businessId = businessId,
                action = if (expiryOverrideReason.isNotBlank()) "EXPIRY_OVERRIDE_SALE" else "POS_SALE_COMPLETED",
                module = "POS",
                details = auditDetails.take(390),
                performedBy = cashierName.ifBlank { "Owner" },
                createdAt = null
            )
        )

        try {
            writeBatch.commit().await()
            sale
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, saleRef.path)
            throw e
        }
    }

    suspend fun processSaleReturn(
        sale: Sale,
        reason: String,
        isPharmacyMode: Boolean
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(sale.status != "RETURNED") { "Invoice is already returned" }

        val writeBatch = db.batch()
        val saleRef = db.collection("sales").document(sale.id)
        writeBatch.update(
            saleRef,
            mapOf(
                "status" to "RETURNED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        val logId = newDocId("log")
        val quarantineNote = if (isPharmacyMode) {
            "Returned medicines logged to quarantine inspection (not auto-returned to sellable stock)."
        } else {
            "Return processed and linked to ${sale.invoiceNumber}."
        }
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = sale.businessId,
                action = "SALE_RETURN",
                module = "SALES",
                details = "Return for ${sale.invoiceNumber} (Rs.${sale.grandTotal.toInt()}): $reason. $quarantineNote".take(390),
                performedBy = "Owner",
                createdAt = null
            )
        )

        try {
            writeBatch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, saleRef.path)
            throw e
        }
    }

    suspend fun syncPendingOfflineSales(
        businessId: String,
        pendingSales: List<Sale>
    ): Result<Int> = runCatching {
        val uid = requireUserId()
        if (pendingSales.isEmpty()) return@runCatching 0
        val writeBatch = db.batch()
        pendingSales.forEach { sale ->
            val sRef = db.collection("sales").document(sale.id)
            writeBatch.update(
                sRef,
                mapOf(
                    "syncStatus" to "SYNCED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        val logId = newDocId("log")
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = businessId,
                action = "OFFLINE_QUEUE_SYNC",
                module = "SYNC",
                details = "Synchronized ${pendingSales.size} offline invoices using idempotency keys",
                performedBy = "Owner",
                createdAt = null
            )
        )
        try {
            writeBatch.commit().await()
            pendingSales.size
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "sales/sync")
            throw e
        }
    }

    // --- STOCK ADJUSTMENT & RECONCILIATION ---

    suspend fun recordStockAdjustment(
        product: Product,
        quantityDelta: Double,
        movementType: String, // ADJUSTMENT, DAMAGED, RECONCILIATION
        reason: String,
        performedBy: String = "Owner"
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val newStock = max(0.0, product.currentStock + quantityDelta)
        val writeBatch = db.batch()

        val prodRef = db.collection("products").document(product.id)
        writeBatch.update(
            prodRef,
            mapOf(
                "currentStock" to newStock,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        val movId = newDocId("mov")
        writeBatch.set(
            db.collection("stock_movements").document(movId),
            StockMovement(
                id = movId,
                ownerUid = uid,
                businessId = product.businessId,
                productId = product.id,
                productName = product.name,
                batchNumber = "",
                quantityChange = quantityDelta,
                stockAfter = newStock,
                movementType = movementType,
                reason = reason.trim().ifBlank { "Stock reconciliation" },
                referenceId = movId,
                performedBy = performedBy,
                createdAt = null
            )
        )

        val logId = newDocId("log")
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = product.businessId,
                action = "STOCK_$movementType",
                module = "INVENTORY",
                details = "${product.name}: change $quantityDelta -> new stock $newStock ($reason)".take(390),
                performedBy = performedBy,
                createdAt = null
            )
        )

        try {
            writeBatch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, prodRef.path)
            throw e
        }
    }

    // --- PURCHASE MANAGEMENT ---

    suspend fun recordPurchaseInvoice(
        businessId: String,
        supplier: Supplier?,
        product: Product,
        quantity: Double,
        unitCost: Double,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        batchNumber: String,
        expiryDate: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val subtotal = quantity * unitCost
        val grandTotal = max(0.0, subtotal - discountAmount)
        val payableAmount = max(0.0, grandTotal - paidAmount)

        val purchaseId = newDocId("pur")
        val invoiceNumber = "PUR-${System.currentTimeMillis().toString().takeLast(6)}"
        val writeBatch = db.batch()

        val purRef = db.collection("purchases").document(purchaseId)
        writeBatch.set(
            purRef,
            Purchase(
                id = purchaseId,
                ownerUid = uid,
                businessId = businessId,
                invoiceNumber = invoiceNumber,
                supplierId = supplier?.id ?: "",
                supplierName = supplier?.companyName?.ifBlank { supplier.name } ?: "Direct Supplier",
                itemsSummary = "${quantity.toInt()} ${product.unit} ${product.name} @ Rs.${unitCost.toInt()}",
                subtotal = subtotal,
                discountAmount = discountAmount,
                grandTotal = grandTotal,
                paidAmount = paidAmount,
                payableAmount = payableAmount,
                paymentMethod = paymentMethod,
                batchNumber = batchNumber.trim(),
                expiryDate = expiryDate.trim(),
                status = "RECEIVED",
                createdAt = null,
                updatedAt = null
            )
        )

        val newStock = product.currentStock + quantity
        val prodRef = db.collection("products").document(product.id)
        writeBatch.update(
            prodRef,
            mapOf(
                "currentStock" to newStock,
                "purchasePrice" to unitCost,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        if (batchNumber.isNotBlank() && expiryDate.isNotBlank()) {
            val bId = newDocId("batch")
            writeBatch.set(
                db.collection("medicine_batches").document(bId),
                MedicineBatch(
                    id = bId,
                    ownerUid = uid,
                    businessId = businessId,
                    productId = product.id,
                    productName = product.name,
                    batchNumber = batchNumber.trim(),
                    manufacturingDate = "2026-01-01",
                    expiryDate = expiryDate.trim(),
                    purchaseCost = unitCost,
                    sellingPrice = product.sellingPrice,
                    availableQty = quantity,
                    supplierId = supplier?.id ?: "",
                    supplierName = supplier?.companyName ?: "",
                    createdAt = null,
                    updatedAt = null
                )
            )
        }

        val movId = newDocId("mov")
        writeBatch.set(
            db.collection("stock_movements").document(movId),
            StockMovement(
                id = movId,
                ownerUid = uid,
                businessId = businessId,
                productId = product.id,
                productName = product.name,
                batchNumber = batchNumber.trim(),
                quantityChange = quantity,
                stockAfter = newStock,
                movementType = "PURCHASE",
                reason = "Purchase Invoice $invoiceNumber",
                referenceId = purchaseId,
                performedBy = "Owner",
                createdAt = null
            )
        )

        if (supplier != null && supplier.id.isNotBlank()) {
            val supRef = db.collection("suppliers").document(supplier.id)
            writeBatch.update(
                supRef,
                mapOf(
                    "payableBalance" to (supplier.payableBalance + payableAmount),
                    "totalPurchased" to (supplier.totalPurchased + grandTotal),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        try {
            writeBatch.commit().await()
            purchaseId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, purRef.path)
            throw e
        }
    }

    // --- CUSTOMERS & UDHAAR LEDGER ---

    suspend fun addCustomer(customer: Customer): Result<String> = runCatching {
        val uid = requireUserId()
        val custId = customer.id.ifBlank { newDocId("cust") }
        val docRef = db.collection("customers").document(custId)
        try {
            docRef.set(
                customer.copy(
                    id = custId,
                    ownerUid = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            custId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun receiveCustomerUdhaarPayment(
        customer: Customer,
        amountReceived: Double,
        paymentMethod: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val newBalance = max(0.0, customer.outstandingBalance - amountReceived)
        val writeBatch = db.batch()
        val custRef = db.collection("customers").document(customer.id)
        writeBatch.update(
            custRef,
            mapOf(
                "outstandingBalance" to newBalance,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        val logId = newDocId("log")
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = customer.businessId,
                action = "UDHAAR_PAYMENT_RECEIVED",
                module = "CUSTOMERS",
                details = "Received Rs.${amountReceived.toInt()} via $paymentMethod from ${customer.name}. Remaining Udhaar: Rs.${newBalance.toInt()}",
                performedBy = "Owner",
                createdAt = null
            )
        )
        try {
            writeBatch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, custRef.path)
            throw e
        }
    }

    // --- SUPPLIERS & PAYABLES ---

    suspend fun addSupplier(supplier: Supplier): Result<String> = runCatching {
        val uid = requireUserId()
        val supId = supplier.id.ifBlank { newDocId("sup") }
        val docRef = db.collection("suppliers").document(supId)
        try {
            docRef.set(
                supplier.copy(
                    id = supId,
                    ownerUid = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            supId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun paySupplierBalance(
        supplier: Supplier,
        amountPaid: Double,
        paymentMethod: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val newPayable = max(0.0, supplier.payableBalance - amountPaid)
        val writeBatch = db.batch()
        val supRef = db.collection("suppliers").document(supplier.id)
        writeBatch.update(
            supRef,
            mapOf(
                "payableBalance" to newPayable,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        val logId = newDocId("log")
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = supplier.businessId,
                action = "SUPPLIER_PAYMENT_SENT",
                module = "SUPPLIERS",
                details = "Paid Rs.${amountPaid.toInt()} via $paymentMethod to ${supplier.companyName.ifBlank { supplier.name }}. Remaining Payable: Rs.${newPayable.toInt()}",
                performedBy = "Owner",
                createdAt = null
            )
        )
        try {
            writeBatch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, supRef.path)
            throw e
        }
    }

    // --- EXPENSES ---

    suspend fun addExpense(expense: Expense): Result<String> = runCatching {
        val uid = requireUserId()
        val expId = expense.id.ifBlank { newDocId("exp") }
        val docRef = db.collection("expenses").document(expId)
        try {
            docRef.set(
                expense.copy(
                    id = expId,
                    ownerUid = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            expId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- EMPLOYEES & PERMISSIONS ---

    suspend fun addEmployee(employee: Employee): Result<String> = runCatching {
        val uid = requireUserId()
        val empId = employee.id.ifBlank { newDocId("emp") }
        val docRef = db.collection("employees").document(empId)
        try {
            docRef.set(
                employee.copy(
                    id = empId,
                    ownerUid = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            empId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateEmployeeRole(employee: Employee, newRole: String, newPermissionsCsv: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val writeBatch = db.batch()
        val empRef = db.collection("employees").document(employee.id)
        writeBatch.update(
            empRef,
            mapOf(
                "role" to newRole,
                "permissionsCsv" to newPermissionsCsv,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        val logId = newDocId("log")
        writeBatch.set(
            db.collection("audit_logs").document(logId),
            AuditLog(
                id = logId,
                ownerUid = uid,
                businessId = employee.businessId,
                action = "EMPLOYEE_ROLE_UPDATED",
                module = "EMPLOYEES",
                details = "Updated ${employee.name} role to $newRole ($newPermissionsCsv)",
                performedBy = "Owner",
                createdAt = null
            )
        )
        try {
            writeBatch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, empRef.path)
            throw e
        }
    }
}
