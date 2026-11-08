package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Business
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DukaanRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBusiness_andCompletePosSale_updatesStockAndCalculatesProfit() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = DukaanRepository(firestore)
        val bizId = "biz_${UUID.randomUUID().toString().take(8)}"

        val createBizResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBusiness(
                Business(
                    id = bizId,
                    name = "Al-Noor Pharmacy",
                    businessType = "PHARMACY",
                    branchName = "Gulberg",
                    city = "Lahore"
                ),
                seedSampleData = false
            )
        }
        assertTrue(createBizResult.isSuccess)

        val prodId = "prod_${UUID.randomUUID().toString().take(8)}"
        val product = Product(
            id = prodId,
            ownerUid = uid,
            businessId = bizId,
            name = "Panadol 500mg",
            sku = "SKU-PAN-1",
            barcode = "8964001",
            category = "Analgesics",
            purchasePrice = 38.0,
            sellingPrice = 45.0,
            wholesalePrice = 41.0,
            unit = "Strip",
            purchaseUnit = "Box",
            conversionFactor = 20.0,
            currentStock = 50.0,
            openingStock = 50.0,
            reorderLevel = 10.0
        )

        val saveProdResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveProduct(product, isNew = true)
        }
        assertTrue(saveProdResult.isSuccess)

        val saleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.completePosSale(
                businessId = bizId,
                cartItems = listOf(CartItem(product = product, quantity = 2.0, unitPrice = 45.0)),
                customer = null,
                discountAmount = 0.0,
                taxAmount = 0.0,
                paidAmount = 90.0,
                paymentMethod = "CASH",
                prescriptionRef = "",
                expiryOverrideReason = "",
                cashierName = "Owner"
            )
        }
        assertTrue(saleResult.isSuccess)
        val completedSale = saleResult.getOrThrow()
        assertEquals(90.0, completedSale.grandTotal, 0.01)
        assertEquals(14.0, completedSale.grossProfit, 0.01) // (45 - 38) * 2 = 14

        val updatedProduct = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProductById(prodId).getOrThrow()
        }
        assertNotNull(updatedProduct)
        assertEquals(48.0, updatedProduct!!.currentStock, 0.01)
    }

    @Test
    fun getProductById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = DukaanRepository(firestore)
        val bizId = "biz_${UUID.randomUUID().toString().take(8)}"
        val prodId = "prod_${UUID.randomUUID().toString().take(8)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveProduct(
                Product(
                    id = prodId,
                    ownerUid = aliceUid,
                    businessId = bizId,
                    name = "Alice Private Product",
                    sku = "SKU-ALC-1",
                    category = "General",
                    purchasePrice = 100.0,
                    sellingPrice = 150.0,
                    wholesalePrice = 130.0,
                    currentStock = 10.0,
                    openingStock = 10.0
                ),
                isNew = true
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = DukaanRepository(firestore)
        val bobFetch = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getProductById(prodId)
        }
        assertTrue(bobFetch.isFailure)
        val firestoreEx = bobFetch.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    @Test
    fun observeBusinesses_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = DukaanRepository(firestore)

        var thrown: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeBusinesses("unauth_uid_123").first()
            }
        } catch (e: Throwable) {
            thrown = e
        }
        assertNotNull(thrown)
        val firestoreEx = thrown!!.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_dukaan@test.com"
        const val BOB_EMAIL = "bob_dukaan@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
