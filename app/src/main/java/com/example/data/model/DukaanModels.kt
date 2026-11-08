package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Business(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val businessType: String = "PHARMACY", // "PHARMACY" or "GENERAL_STORE"
    val branchName: String = "Main Branch",
    val city: String = "Lahore",
    val phone: String = "",
    val address: String = "",
    val currency: String = "PKR",
    val timezone: String = "Asia/Karachi",
    val language: String = "EN", // "EN" or "UR"
    val taxRatePercent: Double = 0.0,
    val receiptHeader: String = "Licensed Retail & POS Partner",
    val receiptFooter: String = "Thank you for shopping with us!",
    val subscriptionPlan: String = "PROFESSIONAL", // "STARTER", "PROFESSIONAL", "BUSINESS"
    val subscriptionBillingCycle: String = "MONTHLY", // "MONTHLY", "ANNUAL"
    val subscriptionStatus: String = "ACTIVE", // "TRIAL", "ACTIVE", "GRACE_PERIOD"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val brand: String = "",
    val description: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val unit: String = "Piece", // Piece, Packet, Box, Kilogram, Gram, Litre, Millilitre, Dozen, Strip, Tablet, Bottle
    val purchaseUnit: String = "Box",
    val conversionFactor: Double = 1.0,
    val currentStock: Double = 0.0,
    val openingStock: Double = 0.0,
    val reorderLevel: Double = 10.0,
    val taxPercent: Double = 0.0,
    @get:PropertyName("isActive") val isActive: Boolean = true,
    @get:PropertyName("isLooseWeight") val isLooseWeight: Boolean = false,
    // Pharmacy specific fields
    val genericName: String = "",
    val saltFormula: String = "",
    val manufacturer: String = "",
    val strength: String = "",
    val dosageForm: String = "",
    val drapRegNumber: String = "",
    val requiresPrescription: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MedicineBatch(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val productId: String = "",
    val productName: String = "",
    val batchNumber: String = "",
    val manufacturingDate: String = "", // YYYY-MM-DD
    val expiryDate: String = "", // YYYY-MM-DD
    val purchaseCost: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val availableQty: Double = 0.0,
    val supplierId: String = "",
    val supplierName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Sale(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val customerName: String = "Walk-in Customer",
    val itemsSummary: String = "",
    val itemCount: Long = 1L,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val totalCostSnapshot: Double = 0.0,
    val grossProfit: Double = 0.0,
    val paidAmount: Double = 0.0,
    val creditAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, JAZZCASH, EASYPAISA, BANK_TRANSFER, CREDIT
    val prescriptionRef: String = "",
    val expiryOverrideReason: String = "",
    val status: String = "COMPLETED", // COMPLETED, RETURNED, PARTIAL_RETURN
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC
    val idempotencyKey: String = "",
    val cashierName: String = "Owner",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Purchase(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val invoiceNumber: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val itemsSummary: String = "",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val payableAmount: Double = 0.0,
    val paymentMethod: String = "CASH",
    val batchNumber: String = "",
    val expiryDate: String = "",
    val status: String = "RECEIVED", // RECEIVED, RETURNED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StockMovement(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val productId: String = "",
    val productName: String = "",
    val batchNumber: String = "",
    val quantityChange: Double = 0.0,
    val stockAfter: Double = 0.0,
    val movementType: String = "OPENING", // OPENING, SALE, PURCHASE, RETURN_IN, RETURN_OUT, ADJUSTMENT, DAMAGED, RECONCILIATION
    val reason: String = "",
    val referenceId: String = "",
    val performedBy: String = "Owner",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val outstandingBalance: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Supplier(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val name: String = "",
    val companyName: String = "",
    val phone: String = "",
    val address: String = "",
    val payableBalance: Double = 0.0,
    val totalPurchased: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val category: String = "Other", // Rent, Electricity, Internet, Salaries, Transport, Maintenance, Other
    val title: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "CASH",
    val status: String = "POSTED", // DRAFT, POSTED, ADJUSTED
    val notes: String = "",
    val recordedBy: String = "Owner",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Employee(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "Cashier", // Owner, Manager, Cashier, Inventory Manager, Accountant, Viewer
    val permissionsCsv: String = "BILLING,DISCOUNTS",
    @get:PropertyName("isActive") val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val businessId: String = "",
    val action: String = "",
    val module: String = "",
    val details: String = "",
    val performedBy: String = "Owner",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CartItem(
    val product: Product,
    val batch: MedicineBatch? = null,
    val quantity: Double = 1.0,
    val selectedUnit: String = product.unit,
    val unitPrice: Double = product.sellingPrice,
    val isWholesalePrice: Boolean = false
) {
    val lineSubtotal: Double
        get() = quantity * unitPrice
    val lineCostSnapshot: Double
        get() = quantity * (batch?.purchaseCost ?: product.purchasePrice)
    val lineTax: Double
        get() = lineSubtotal * (product.taxPercent / 100.0)
}

enum class DateRangeFilter(val labelEn: String, val labelUr: String, val daysBack: Int) {
    TODAY("Today", "آج", 1),
    YESTERDAY("Yesterday", "کل", 2),
    LAST_7_DAYS("Last 7 Days", "پچھلے 7 دن", 7),
    LAST_30_DAYS("Last 30 Days", "پچھلے 30 دن", 30),
    ALL_TIME("All Time", "تمام ریکارڈ", 3650)
}

enum class DukaanModule(
    val route: String,
    val titleEn: String,
    val titleUr: String,
    val pharmacyOnly: Boolean = false
) {
    DASHBOARD("dashboard", "Dashboard", "ڈیش بورڈ"),
    POS("pos", "Point of Sale", "پوائنٹ آف سیل (بلنگ)"),
    PRODUCTS("products", "Products", "مصنوعات / ادویات"),
    CATEGORIES("categories", "Categories & Units", "اقسام اور یونٹس"),
    PHARMACY_BATCHES("pharmacy_batches", "Batches & Expiry", "بیچ اور ایکسپائری", pharmacyOnly = true),
    INVENTORY("inventory", "Inventory Ledger", "اسٹاک لیجر"),
    PURCHASES("purchases", "Purchases", "خریداری (پرچیز)"),
    SALES("sales", "Sales & Invoices", "سیلز اور انوائسز"),
    CUSTOMERS("customers", "Customers & Udhaar", "گاہک اور ادھار کھاتہ"),
    SUPPLIERS("suppliers", "Suppliers", "سپلائرز"),
    EXPENSES("expenses", "Expenses", "اخراجات"),
    ACCOUNTS("accounts", "Accounts & Ledgers", "کھاتہ اور لیجر"),
    EMPLOYEES("employees", "Employees & Roles", "ملازمین اور اختیارات"),
    REPORTS("reports", "Financial Reports", "مالیاتی رپورٹس"),
    NOTIFICATIONS("notifications", "Alerts Center", "الرٹس اور اطلاعات"),
    SUBSCRIPTION("subscription", "Subscription", "سبسکرپشن پلان"),
    SETTINGS("settings", "Business Settings", "سیٹنگز")
}
