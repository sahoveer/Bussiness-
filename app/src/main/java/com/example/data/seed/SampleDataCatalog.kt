package com.example.data.seed

import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.MedicineBatch
import com.example.data.model.Product
import com.example.data.model.Supplier

object SampleDataCatalog {

    fun pharmacyProducts(ownerUid: String, businessId: String): List<Product> = listOf(
        Product(
            id = "med_panadol_${businessId.takeLast(4)}",
            ownerUid = ownerUid,
            businessId = businessId,
            name = "Panadol Advance 500mg",
            sku = "PHM-PAN-500",
            barcode = "8964001001011",
            category = "Analgesics & Antipyretics",
            brand = "Haleon Pakistan",
            description = "Fast-release paracetamol film-coated tablets (20 strips x 10 tabs)",
            purchasePrice = 38.0,
            sellingPrice = 45.0,
            wholesalePrice = 41.0,
            unit = "Strip",
            purchaseUnit = "Box",
            conversionFactor = 20.0,
            currentStock = 140.0,
            openingStock = 140.0,
            reorderLevel = 30.0,
            taxPercent = 0.0,
            isActive = true,
            isLooseWeight = false,
            genericName = "Paracetamol",
            saltFormula = "Paracetamol 500mg",
            manufacturer = "Haleon Pakistan Ltd",
            strength = "500mg",
            dosageForm = "Tablet",
            drapRegNumber = "DRAP-004129",
            requiresPrescription = false
        ),
        Product(
            id = "med_augmentin_${businessId.takeLast(4)}",
            ownerUid = ownerUid,
            businessId = businessId,
            name = "Augmentin 625mg",
            sku = "PHM-AUG-625",
            barcode = "8964001002025",
            category = "Antibiotics",
            brand = "GSK Pakistan",
            description = "Broad-spectrum antibiotic pack of 6 tablets",
            purchasePrice = 310.0,
            sellingPrice = 365.0,
            wholesalePrice = 340.0,
            unit = "Box",
            purchaseUnit = "Box",
            conversionFactor = 1.0,
            currentStock = 38.0,
            openingStock = 38.0,
            reorderLevel = 12.0,
            taxPercent = 0.0,
            isActive = true,
            isLooseWeight = false,
            genericName = "Co-Amoxiclav",
            saltFormula = "Amoxicillin 500mg + Clavulanic Acid 125mg",
            manufacturer = "GlaxoSmithKline Pakistan",
            strength = "625mg",
            dosageForm = "Tablet",
            drapRegNumber = "DRAP-008812",
            requiresPrescription = true
        ),
        Product(
            id = "med_risek_${businessId.takeLast(4)}",
            ownerUid = ownerUid,
            businessId = businessId,
            name = "Risek 20mg Capsules",
            sku = "PHM-RSK-020",
            barcode = "8964001003039",
            category = "Gastrointestinal (PPI)",
            brand = "Getz Pharma",
            description = "Omeprazole delayed-release capsules (14 capsules pack)",
            purchasePrice = 245.0,
            sellingPrice = 290.0,
            wholesalePrice = 268.0,
            unit = "Box",
            purchaseUnit = "Box",
            conversionFactor = 1.0,
            currentStock = 8.0, // Low stock alert trigger
            openingStock = 25.0,
            reorderLevel = 15.0,
            taxPercent = 0.0,
            isActive = true,
            isLooseWeight = false,
            genericName = "Omeprazole",
            saltFormula = "Omeprazole 20mg",
            manufacturer = "Getz Pharma (Pvt) Ltd",
            strength = "20mg",
            dosageForm = "Capsule",
            drapRegNumber = "DRAP-015920",
            requiresPrescription = false
        ),
        Product(
            id = "med_brufen_${businessId.takeLast(4)}",
            ownerUid = ownerUid,
            businessId = businessId,
            name = "Brufen Suspension 120ml",
            sku = "PHM-BRF-120",
            barcode = "8964001004043",
            category = "Pediatric Analgesics",
            brand = "Abbott Laboratories",
            description = "Orange flavored pediatric ibuprofen oral suspension",
            purchasePrice = 112.0,
            sellingPrice = 135.0,
            wholesalePrice = 124.0,
            unit = "Bottle",
            purchaseUnit = "Box",
            conversionFactor = 12.0,
            currentStock = 42.0,
            openingStock = 42.0,
            reorderLevel = 10.0,
            taxPercent = 0.0,
            isActive = true,
            isLooseWeight = false,
            genericName = "Ibuprofen",
            saltFormula = "Ibuprofen 100mg/5ml",
            manufacturer = "Abbott Laboratories Pakistan",
            strength = "100mg/5ml",
            dosageForm = "Syrup",
            drapRegNumber = "DRAP-002310",
            requiresPrescription = false
        ),
        Product(
            id = "med_arinac_${businessId.takeLast(4)}",
            ownerUid = ownerUid,
            businessId = businessId,
            name = "Arinac Forte Tablets",
            sku = "PHM-ARN-400",
            barcode = "8964001005057",
            category = "Cold & Flu",
            brand = "Abbott Laboratories",
            description = "Cold, flu and sinus congestion relief strip of 10 tablets",
            purchasePrice = 95.0,
            sellingPrice = 115.0,
            wholesalePrice = 105.0,
            unit = "Strip",
            purchaseUnit = "Box",
            conversionFactor = 10.0,
            currentStock = 15.0,
            openingStock = 15.0,
            reorderLevel = 20.0,
            taxPercent = 0.0,
            isActive = true,
            isLooseWeight = false,
            genericName = "Ibuprofen + Pseudoephedrine",
            saltFormula = "Ibuprofen 400mg + Pseudoephedrine HCl 60mg",
            manufacturer = "Abbott Laboratories Pakistan",
            strength = "400mg/60mg",
            dosageForm = "Tablet",
            drapRegNumber = "DRAP-009411",
            requiresPrescription = false
        )
    )

    fun pharmacyBatches(ownerUid: String, businessId: String): List<MedicineBatch> {
        val suffix = businessId.takeLast(4)
        return listOf(
            MedicineBatch(
                id = "batch_pan_a_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                productId = "med_panadol_$suffix",
                productName = "Panadol Advance 500mg",
                batchNumber = "PND-2609A",
                manufacturingDate = "2026-01-15",
                expiryDate = "2028-01-15",
                purchaseCost = 38.0,
                sellingPrice = 45.0,
                availableQty = 140.0,
                supplierId = "sup_premier_$suffix",
                supplierName = "Premier Agencies Pharma"
            ),
            MedicineBatch(
                id = "batch_aug_near_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                productId = "med_augmentin_$suffix",
                productName = "Augmentin 625mg",
                batchNumber = "AUG-2512N",
                manufacturingDate = "2025-06-01",
                expiryDate = "2026-12-10", // Near expiry (< 45 days from Nov 2026)
                purchaseCost = 310.0,
                sellingPrice = 365.0,
                availableQty = 38.0,
                supplierId = "sup_muller_$suffix",
                supplierName = "Muller & Phipps Pakistan"
            ),
            MedicineBatch(
                id = "batch_rsk_good_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                productId = "med_risek_$suffix",
                productName = "Risek 20mg Capsules",
                batchNumber = "RSK-2604B",
                manufacturingDate = "2026-04-01",
                expiryDate = "2028-03-31",
                purchaseCost = 245.0,
                sellingPrice = 290.0,
                availableQty = 8.0,
                supplierId = "sup_premier_$suffix",
                supplierName = "Premier Agencies Pharma"
            ),
            MedicineBatch(
                id = "batch_arn_exp_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                productId = "med_arinac_$suffix",
                productName = "Arinac Forte Tablets",
                batchNumber = "ARN-2408X",
                manufacturingDate = "2024-08-01",
                expiryDate = "2026-09-30", // Expired batch (before Nov 2026) to demonstrate POS safety block!
                purchaseCost = 95.0,
                sellingPrice = 115.0,
                availableQty = 15.0,
                supplierId = "sup_muller_$suffix",
                supplierName = "Muller & Phipps Pakistan"
            )
        )
    }

    fun generalStoreProducts(ownerUid: String, businessId: String): List<Product> {
        val suffix = businessId.takeLast(4)
        return listOf(
            Product(
                id = "gro_rice_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Guard Super Kernel Basmati Rice (Loose)",
                sku = "GRO-RCE-001",
                barcode = "8961100220011",
                category = "Grains, Rice & Pulses",
                brand = "Guard Rice",
                description = "Aged extra-long grain Pakistani Basmati rice sold by weight (Kg / Gram)",
                purchasePrice = 310.0,
                sellingPrice = 360.0,
                wholesalePrice = 335.0,
                unit = "Kilogram",
                purchaseUnit = "Bag (25Kg)",
                conversionFactor = 25.0,
                currentStock = 85.0,
                openingStock = 100.0,
                reorderLevel = 25.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = true
            ),
            Product(
                id = "gro_dalda_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Dalda Cooking Oil 5 Litre Pouch",
                sku = "GRO-OIL-005",
                barcode = "8961100220028",
                category = "Cooking Oil & Ghee",
                brand = "Dalda Foods",
                description = "Cholesterol-free fortified cooking oil 5L standup pouch",
                purchasePrice = 2480.0,
                sellingPrice = 2650.0,
                wholesalePrice = 2560.0,
                unit = "Packet",
                purchaseUnit = "Box",
                conversionFactor = 4.0,
                currentStock = 22.0,
                openingStock = 24.0,
                reorderLevel = 8.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = false
            ),
            Product(
                id = "gro_tapal_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Tapal Danedar Tea 900g Family Pack",
                sku = "GRO-TEA-900",
                barcode = "8961100220035",
                category = "Tea & Beverages",
                brand = "Tapal Tea",
                description = "Rich Kenyan & Assam blend granulated black tea",
                purchasePrice = 1520.0,
                sellingPrice = 1680.0,
                wholesalePrice = 1600.0,
                unit = "Packet",
                purchaseUnit = "Box",
                conversionFactor = 12.0,
                currentStock = 6.0, // Low stock trigger
                openingStock = 18.0,
                reorderLevel = 10.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = false
            ),
            Product(
                id = "gro_sugar_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Refined White Sugar / Cheeni (Loose)",
                sku = "GRO-SUG-001",
                barcode = "8961100220042",
                category = "Grains, Rice & Pulses",
                brand = "JDW Sugar Mills",
                description = "Crystal white refined sugar sold loose by Kilogram or Gram",
                purchasePrice = 132.0,
                sellingPrice = 148.0,
                wholesalePrice = 140.0,
                unit = "Kilogram",
                purchaseUnit = "Bag (50Kg)",
                conversionFactor = 50.0,
                currentStock = 120.0,
                openingStock = 150.0,
                reorderLevel = 40.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = true
            ),
            Product(
                id = "gro_shan_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Shan Sindhi Biryani Masala 60g",
                sku = "GRO-SPC-060",
                barcode = "8961100220059",
                category = "Spices & Masalas",
                brand = "Shan Foods",
                description = "Authentic traditional Sindhi Biryani spice mix box",
                purchasePrice = 105.0,
                sellingPrice = 130.0,
                wholesalePrice = 115.0,
                unit = "Box",
                purchaseUnit = "Dozen",
                conversionFactor = 12.0,
                currentStock = 64.0,
                openingStock = 72.0,
                reorderLevel = 18.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = false
            ),
            Product(
                id = "gro_olpers_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Olper's Full Cream Milk 1 Litre",
                sku = "GRO-MLK-001",
                barcode = "8961100220066",
                category = "Dairy & Breakfast",
                brand = "FrieslandCampina Engro",
                description = "UHT treated standardized full cream milk 1L tetra pack",
                purchasePrice = 268.0,
                sellingPrice = 295.0,
                wholesalePrice = 280.0,
                unit = "Litre",
                purchaseUnit = "Box",
                conversionFactor = 12.0,
                currentStock = 48.0,
                openingStock = 48.0,
                reorderLevel = 15.0,
                taxPercent = 0.0,
                isActive = true,
                isLooseWeight = false
            )
        )
    }

    fun defaultCustomers(ownerUid: String, businessId: String): List<Customer> {
        val suffix = businessId.takeLast(4)
        return listOf(
            Customer(
                id = "cust_tariq_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Haji Tariq Mehmood",
                phone = "0300-4455667",
                address = "House 42, Block C, Model Town",
                outstandingBalance = 4250.0,
                totalPurchases = 28500.0,
                notes = "Monthly family account - settles on 5th of every month"
            ),
            Customer(
                id = "cust_uzma_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Dr. Uzma Rashid",
                phone = "0321-8899112",
                address = "Street 8, Cavalry Ground",
                outstandingBalance = 1450.0,
                totalPurchases = 14900.0,
                notes = "Regular chronic care & household customer"
            ),
            Customer(
                id = "cust_kamran_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Kamran Ahmed Khan",
                phone = "0333-5123490",
                address = "Johar Town Phase 2",
                outstandingBalance = 0.0,
                totalPurchases = 9200.0,
                notes = "Cash & JazzCash buyer"
            )
        )
    }

    fun defaultSuppliers(ownerUid: String, businessId: String, isPharmacy: Boolean): List<Supplier> {
        val suffix = businessId.takeLast(4)
        return if (isPharmacy) {
            listOf(
                Supplier(
                    id = "sup_premier_$suffix",
                    ownerUid = ownerUid,
                    businessId = businessId,
                    name = "Salman Farooq",
                    companyName = "Premier Agencies Pharma Distributors",
                    phone = "042-37221199",
                    address = "Medicine Market, Lohari Gate, Lahore",
                    payableBalance = 18500.0,
                    totalPurchased = 145000.0
                ),
                Supplier(
                    id = "sup_muller_$suffix",
                    ownerUid = ownerUid,
                    businessId = businessId,
                    name = "Asadullah Sheikh",
                    companyName = "Muller & Phipps (M&P) Pakistan",
                    phone = "021-35681122",
                    address = "Industrial Area, Kot Lakhpat",
                    payableBalance = 9200.0,
                    totalPurchased = 98000.0
                )
            )
        } else {
            listOf(
                Supplier(
                    id = "sup_akbari_$suffix",
                    ownerUid = ownerUid,
                    businessId = businessId,
                    name = "Mian Abdul Rauf",
                    companyName = "Akbari Mandi Wholesale Traders",
                    phone = "042-37654321",
                    address = "Akbari Mandi, Walled City, Lahore",
                    payableBalance = 32000.0,
                    totalPurchased = 260000.0
                ),
                Supplier(
                    id = "sup_unilever_$suffix",
                    ownerUid = ownerUid,
                    businessId = businessId,
                    name = "Zeeshan Butt",
                    companyName = "Metro FMCG & Beverages Distribution",
                    phone = "0301-7788990",
                    address = "Main Ferozepur Road",
                    payableBalance = 11500.0,
                    totalPurchased = 115000.0
                )
            )
        }
    }

    fun defaultExpenses(ownerUid: String, businessId: String): List<Expense> {
        val suffix = businessId.takeLast(4)
        return listOf(
            Expense(
                id = "exp_elec_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                category = "Electricity",
                title = "LESCO / K-Electric Commercial Shop Bill",
                amount = 14500.0,
                paymentMethod = "BANK_TRANSFER",
                status = "POSTED",
                notes = "Commercial meter #8892101",
                recordedBy = "Owner"
            ),
            Expense(
                id = "exp_inet_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                category = "Internet",
                title = "PTCL Flash Fiber POS Connection",
                amount = 3800.0,
                paymentMethod = "JAZZCASH",
                status = "POSTED",
                notes = "Monthly 50 Mbps fiber package",
                recordedBy = "Owner"
            )
        )
    }

    fun defaultEmployees(ownerUid: String, businessId: String): List<Employee> {
        val suffix = businessId.takeLast(4)
        return listOf(
            Employee(
                id = "emp_owner_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Shop Owner (Admin)",
                phone = "0300-1112233",
                email = "owner@dukaanos.pk",
                role = "Owner",
                permissionsCsv = "BILLING,DISCOUNTS,RETURNS,STOCK_ADJUST,PURCHASES,EXPENSES,REPORTS,SETTINGS",
                isActive = true
            ),
            Employee(
                id = "emp_cashier_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Bilal Hussain",
                phone = "0312-9988776",
                email = "bilal.pos@dukaanos.pk",
                role = "Cashier",
                permissionsCsv = "BILLING,DISCOUNTS",
                isActive = true
            ),
            Employee(
                id = "emp_inv_$suffix",
                ownerUid = ownerUid,
                businessId = businessId,
                name = "Usman Raza",
                phone = "0345-6677889",
                email = "usman.stock@dukaanos.pk",
                role = "Inventory Manager",
                permissionsCsv = "STOCK_ADJUST,PURCHASES",
                isActive = true
            )
        )
    }
}
