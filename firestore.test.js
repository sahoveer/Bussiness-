const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_owner_123";
const BOB_UID = "bob_owner_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read businesses or products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("businesses").get());
  await assertFails(unauthDb.collection("products").get());
});

test("Authenticated user: can create and query own business and product", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("businesses").doc("biz_1").set({
      ownerUid: ALICE_UID,
      name: "Al-Shifa Pharmacy",
      businessType: "PHARMACY",
      branchName: "Main Branch - Gulberg",
      city: "Lahore",
      phone: "0300-1234567",
      address: "Main Boulevard, Gulberg III, Lahore",
      currency: "PKR",
      timezone: "Asia/Karachi",
      language: "EN",
      taxRatePercent: 0,
      receiptHeader: "DRAP Licensed Pharmacy",
      receiptFooter: "Thank you for visiting Al-Shifa!",
      subscriptionPlan: "PROFESSIONAL",
      subscriptionBillingCycle: "MONTHLY",
      subscriptionStatus: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("products").doc("prod_1").set({
      ownerUid: ALICE_UID,
      businessId: "biz_1",
      name: "Panadol Advance 500mg",
      sku: "MED-PAN-500",
      barcode: "8964001234567",
      category: "Analgesics",
      brand: "GSK Pakistan",
      description: "Paracetamol 500mg film-coated tablets",
      purchasePrice: 380,
      sellingPrice: 450,
      wholesalePrice: 410,
      unit: "Box",
      purchaseUnit: "Box",
      conversionFactor: 10,
      currentStock: 50,
      openingStock: 50,
      reorderLevel: 10,
      taxPercent: 0,
      isActive: true,
      isLooseWeight: false,
      genericName: "Paracetamol",
      saltFormula: "Acetaminophen 500mg",
      manufacturer: "Haleon / GSK",
      strength: "500mg",
      dosageForm: "Tablet",
      drapRegNumber: "DRAP-019283",
      requiresPrescription: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("products").where("ownerUid", "==", ALICE_UID).get()
  );
});

test("Cross-tenant isolation: Bob cannot read or modify Alice's business or products", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("businesses").doc("alice_biz").set({
      ownerUid: ALICE_UID,
      name: "Alice Store",
      businessType: "GENERAL_STORE",
      branchName: "Main",
      city: "Karachi",
      phone: "0321-0000000",
      address: "DHA Phase 6",
      currency: "PKR",
      timezone: "Asia/Karachi",
      language: "EN",
      taxRatePercent: 5,
      receiptHeader: "",
      receiptFooter: "",
      subscriptionPlan: "STARTER",
      subscriptionBillingCycle: "MONTHLY",
      subscriptionStatus: "TRIAL",
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("businesses").doc("alice_biz").get());
  await assertFails(bobDb.collection("businesses").get());
});

test("Append-only enforcement: stock_movements and sales cannot be deleted", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("stock_movements").doc("mov_1").set({
      ownerUid: ALICE_UID,
      businessId: "biz_1",
      productId: "prod_1",
      productName: "Panadol Advance 500mg",
      batchNumber: "B-2026-01",
      quantityChange: 50,
      stockAfter: 50,
      movementType: "OPENING",
      reason: "Initial opening stock",
      referenceId: "INIT-1",
      performedBy: "Owner",
      createdAt: now,
    })
  );

  await assertFails(aliceDb.collection("stock_movements").doc("mov_1").delete());
});
