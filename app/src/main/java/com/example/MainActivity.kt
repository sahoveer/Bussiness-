package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.Business
import com.example.data.model.DukaanModule
import com.example.data.repository.DukaanRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.BusinessOnboardingScreen
import com.example.ui.auth.signOutDukaan
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.screens.CategoriesAndUnitsScreen
import com.example.ui.screens.CustomersUdhaarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.FinancialReportsAndAccountsScreen
import com.example.ui.screens.InventoryLedgerScreen
import com.example.ui.screens.NotificationsAlertsScreen
import com.example.ui.screens.PharmacyBatchesScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.SalesHistoryScreen
import com.example.ui.screens.SubscriptionAndSettingsScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.theme.DukaanOSTheme
import com.example.ui.theme.DukaanPrimary
import com.example.ui.theme.DukaanSuccess
import com.example.ui.viewmodel.DukaanViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        DukaanOSTheme(darkTheme = true) {
            AuthScreen(
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                }
            )
        }
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            AuthenticatedDukaanShell(
                currentUserId = uid,
                userEmail = currentUser?.email ?: "owner@dukaanos.pk",
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedDukaanShell(
    currentUserId: String,
    userEmail: String,
    onSignedOut: () -> Unit
) {
    val viewModel: DukaanViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DukaanViewModel(DukaanRepository(db), currentUserId)
            }
        }
    )

    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val activeBusiness by viewModel.activeBusiness.collectAsStateWithLifecycle()
    val currentModule by viewModel.currentModule.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val activeRole by viewModel.activeEmployeeRole.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val products by viewModel.products.collectAsStateWithLifecycle()
    val batches by viewModel.medicineBatches.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val movements by viewModel.stockMovements.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val posDiscount by viewModel.posDiscount.collectAsStateWithLifecycle()
    val posPaymentMethod by viewModel.posPaymentMethod.collectAsStateWithLifecycle()
    val posPrescriptionRef by viewModel.posPrescriptionRef.collectAsStateWithLifecycle()
    val posExpiryOverrideReason by viewModel.posExpiryOverrideReason.collectAsStateWithLifecycle()
    val isOfflineQueueMode by viewModel.isOfflineQueueMode.collectAsStateWithLifecycle()
    val lastReceiptSale by viewModel.lastReceiptSale.collectAsStateWithLifecycle()

    var showCreateBusinessScreen by remember { mutableStateOf(false) }
    var showBusinessDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle Android back button when on secondary modules
    BackHandler(enabled = currentModule != DukaanModule.DASHBOARD || showCreateBusinessScreen) {
        if (showCreateBusinessScreen) {
            showCreateBusinessScreen = false
        } else {
            viewModel.navigateTo(DukaanModule.DASHBOARD)
        }
    }

    val isUrdu = activeBusiness?.language == "UR"
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    DukaanOSTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            if (businesses.isEmpty() || showCreateBusinessScreen) {
                BusinessOnboardingScreen(
                    isBusy = isBusy,
                    onCreateBusiness = { name, type, branch, city, phone, address, lang, tax, plan, seed ->
                        viewModel.createNewBusiness(
                            name = name,
                            businessType = type,
                            branchName = branch,
                            city = city,
                            phone = phone,
                            address = address,
                            language = lang,
                            taxRatePercent = tax,
                            subscriptionPlan = plan,
                            seedSample = seed,
                            onComplete = { showCreateBusinessScreen = false }
                        )
                    },
                    onCancel = if (businesses.isNotEmpty()) {
                        { showCreateBusinessScreen = false }
                    } else null
                )
            } else {
                val visibleModules = remember(activeBusiness?.businessType, activeRole) {
                    DukaanModule.entries.filter { mod ->
                        val modeAllowed = !mod.pharmacyOnly || activeBusiness?.businessType == "PHARMACY"
                        val roleAllowed = when (activeRole) {
                            "Cashier" -> mod in listOf(
                                DukaanModule.DASHBOARD,
                                DukaanModule.POS,
                                DukaanModule.SALES,
                                DukaanModule.CUSTOMERS
                            )
                            "Inventory Manager" -> mod in listOf(
                                DukaanModule.DASHBOARD,
                                DukaanModule.PRODUCTS,
                                DukaanModule.CATEGORIES,
                                DukaanModule.PHARMACY_BATCHES,
                                DukaanModule.INVENTORY,
                                DukaanModule.PURCHASES,
                                DukaanModule.SUPPLIERS
                            )
                            "Accountant" -> mod in listOf(
                                DukaanModule.DASHBOARD,
                                DukaanModule.SALES,
                                DukaanModule.PURCHASES,
                                DukaanModule.EXPENSES,
                                DukaanModule.ACCOUNTS,
                                DukaanModule.REPORTS
                            )
                            else -> true
                        }
                        modeAllowed && roleAllowed
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isPermanentSidebar = maxWidth >= 900.dp

                    val mainScaffoldContent: @Composable () -> Unit = {
                        Scaffold(
                            contentWindowInsets = WindowInsets.safeDrawing,
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (isUrdu) currentModule.titleUr else currentModule.titleEn,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${activeBusiness?.name.orEmpty()} • Role: $activeRole",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        if (!isPermanentSidebar) {
                                            IconButton(
                                                onClick = { scope.launch { drawerState.open() } },
                                                modifier = Modifier.testTag("open_sidebar_drawer_btn")
                                            ) {
                                                Icon(Icons.Default.Menu, contentDescription = "Open Navigation Sidebar")
                                            }
                                        }
                                    },
                                    actions = {
                                        // Business & Branch Switcher
                                        Box {
                                            OutlinedButton(
                                                onClick = { showBusinessDropdown = true },
                                                modifier = Modifier.testTag("business_switcher_btn")
                                            ) {
                                                Text(
                                                    text = activeBusiness?.name?.take(14) ?: "Switch Shop",
                                                    maxLines = 1
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showBusinessDropdown,
                                                onDismissRequest = { showBusinessDropdown = false }
                                            ) {
                                                businesses.forEach { biz ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text("${biz.name} (${biz.businessType.replace("_", " ")})")
                                                        },
                                                        onClick = {
                                                            viewModel.selectBusiness(biz.id)
                                                            showBusinessDropdown = false
                                                        }
                                                    )
                                                }
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text("+ Add New Business / Mode") },
                                                    leadingIcon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                                                    onClick = {
                                                        showBusinessDropdown = false
                                                        showCreateBusinessScreen = true
                                                    }
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.navigateTo(DukaanModule.NOTIFICATIONS) }) {
                                            Icon(Icons.Default.Notifications, contentDescription = "Alerts Center")
                                        }

                                        IconButton(onClick = { viewModel.toggleTheme() }) {
                                            Icon(
                                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = "Toggle Dark/Light Theme"
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentModule) {
                                    DukaanModule.DASHBOARD -> DashboardScreen(
                                        business = activeBusiness,
                                        products = products,
                                        batches = batches,
                                        sales = sales,
                                        purchases = purchases,
                                        expenses = expenses,
                                        customers = customers,
                                        suppliers = suppliers,
                                        selectedDateFilter = dateFilter,
                                        onSelectDateFilter = viewModel::setDateFilter,
                                        onNavigate = viewModel::navigateTo,
                                        onSeedSampleData = viewModel::seedSampleDataForActiveBusiness
                                    )

                                    DukaanModule.POS -> PosScreen(
                                        business = activeBusiness,
                                        products = products,
                                        batches = batches,
                                        customers = customers,
                                        cartItems = cartItems,
                                        selectedCustomer = selectedCustomer,
                                        discountAmount = posDiscount,
                                        paymentMethod = posPaymentMethod,
                                        prescriptionRef = posPrescriptionRef,
                                        expiryOverrideReason = posExpiryOverrideReason,
                                        isOfflineQueueMode = isOfflineQueueMode,
                                        hasExpiredBatchInCart = viewModel.hasExpiredBatchInCart(),
                                        isBusy = isBusy,
                                        onAddToCart = viewModel::addToCart,
                                        onUpdateCartQty = viewModel::updateCartItemQuantity,
                                        onRemoveFromCart = viewModel::removeFromCart,
                                        onClearCart = viewModel::clearCart,
                                        onSelectCustomer = viewModel::setPosCustomer,
                                        onDiscountChange = viewModel::setPosDiscount,
                                        onPaymentMethodChange = viewModel::setPosPaymentMethod,
                                        onPrescriptionRefChange = viewModel::setPosPrescriptionRef,
                                        onExpiryOverrideChange = viewModel::setPosExpiryOverrideReason,
                                        onToggleOfflineMode = viewModel::toggleOfflineQueueMode,
                                        onCompleteSale = viewModel::completeSale
                                    )

                                    DukaanModule.PRODUCTS -> ProductsScreen(
                                        business = activeBusiness,
                                        products = products,
                                        onSaveProduct = viewModel::saveProduct,
                                        onToggleArchive = viewModel::toggleProductArchive,
                                        onBulkPriceChange = viewModel::applyBulkPriceChange,
                                        onSeedSampleData = viewModel::seedSampleDataForActiveBusiness
                                    )

                                    DukaanModule.CATEGORIES -> CategoriesAndUnitsScreen(products = products)

                                    DukaanModule.PHARMACY_BATCHES -> PharmacyBatchesScreen(
                                        products = products,
                                        batches = batches,
                                        suppliers = suppliers,
                                        onAddBatch = viewModel::addMedicineBatch
                                    )

                                    DukaanModule.INVENTORY -> InventoryLedgerScreen(
                                        products = products,
                                        movements = movements,
                                        onRecordAdjustment = viewModel::adjustStock
                                    )

                                    DukaanModule.PURCHASES -> PurchasesScreen(
                                        business = activeBusiness,
                                        purchases = purchases,
                                        products = products,
                                        suppliers = suppliers,
                                        onCreatePurchase = viewModel::createPurchaseInvoice
                                    )

                                    DukaanModule.SALES -> SalesHistoryScreen(
                                        sales = sales,
                                        onViewReceipt = viewModel::showReceipt,
                                        onProcessReturn = viewModel::processReturn,
                                        onSyncOfflineSales = viewModel::syncOfflineSales
                                    )

                                    DukaanModule.CUSTOMERS -> CustomersUdhaarScreen(
                                        customers = customers,
                                        onAddCustomer = viewModel::addCustomer,
                                        onReceiveUdhaar = viewModel::receiveUdhaarPayment
                                    )

                                    DukaanModule.SUPPLIERS -> SuppliersScreen(
                                        suppliers = suppliers,
                                        onAddSupplier = viewModel::addSupplier,
                                        onPaySupplier = viewModel::paySupplier
                                    )

                                    DukaanModule.EXPENSES -> ExpensesScreen(
                                        expenses = expenses,
                                        onAddExpense = viewModel::addExpense
                                    )

                                    DukaanModule.ACCOUNTS, DukaanModule.REPORTS -> FinancialReportsAndAccountsScreen(
                                        products = products,
                                        sales = sales,
                                        purchases = purchases,
                                        expenses = expenses,
                                        customers = customers,
                                        suppliers = suppliers,
                                        auditLogs = auditLogs
                                    )

                                    DukaanModule.EMPLOYEES -> EmployeesScreen(
                                        employees = employees,
                                        activeRole = activeRole,
                                        onSwitchActiveRole = viewModel::setActiveEmployeeRole,
                                        onAddEmployee = viewModel::addEmployee
                                    )

                                    DukaanModule.NOTIFICATIONS -> NotificationsAlertsScreen(
                                        products = products,
                                        batches = batches,
                                        customers = customers,
                                        suppliers = suppliers
                                    )

                                    DukaanModule.SUBSCRIPTION -> SubscriptionAndSettingsScreen(
                                        business = activeBusiness,
                                        isSubscriptionTab = true,
                                        onSaveSettings = viewModel::updateSettings
                                    )

                                    DukaanModule.SETTINGS -> SubscriptionAndSettingsScreen(
                                        business = activeBusiness,
                                        isSubscriptionTab = false,
                                        onSaveSettings = viewModel::updateSettings
                                    )
                                }
                            }
                        }
                    }

                    if (isPermanentSidebar) {
                        PermanentNavigationDrawer(
                            drawerContent = {
                                PermanentDrawerSheet(modifier = Modifier.width(270.dp)) {
                                    SidebarContent(
                                        business = activeBusiness,
                                        userEmail = userEmail,
                                        isUrdu = isUrdu,
                                        visibleModules = visibleModules,
                                        currentModule = currentModule,
                                        onSelectModule = { mod -> viewModel.navigateTo(mod) },
                                        onNewBusiness = { showCreateBusinessScreen = true },
                                        onSignOut = {
                                            signOutDukaan(credentialManager, scope, onSignedOut)
                                        }
                                    )
                                }
                            },
                            content = mainScaffoldContent
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(modifier = Modifier.width(285.dp)) {
                                    SidebarContent(
                                        business = activeBusiness,
                                        userEmail = userEmail,
                                        isUrdu = isUrdu,
                                        visibleModules = visibleModules,
                                        currentModule = currentModule,
                                        onSelectModule = { mod ->
                                            viewModel.navigateTo(mod)
                                            scope.launch { drawerState.close() }
                                        },
                                        onNewBusiness = {
                                            scope.launch { drawerState.close() }
                                            showCreateBusinessScreen = true
                                        },
                                        onSignOut = {
                                            signOutDukaan(credentialManager, scope, onSignedOut)
                                        }
                                    )
                                }
                            },
                            content = mainScaffoldContent
                        )
                    }
                }
            }

            if (lastReceiptSale != null) {
                ThermalReceiptDialog(
                    sale = lastReceiptSale!!,
                    business = activeBusiness,
                    onDismiss = viewModel::dismissReceiptDialog
                )
            }
        }
    }
}

@Composable
private fun SidebarContent(
    business: Business?,
    userEmail: String,
    isUrdu: Boolean,
    visibleModules: List<DukaanModule>,
    currentModule: DukaanModule,
    onSelectModule: (DukaanModule) -> Unit,
    onNewBusiness: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "DukaanOS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DukaanPrimary
        )
        Text(
            text = business?.name ?: "Select Business",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(
                text = if (business?.businessType == "PHARMACY") "PHARMACY" else "GENERAL STORE",
                color = if (business?.businessType == "PHARMACY") DukaanSuccess else DukaanPrimary
            )
            StatusBadge(text = "PKR (Rs.)", color = DukaanPrimary)
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))

        visibleModules.forEach { mod ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = moduleIcon(mod),
                        contentDescription = mod.titleEn
                    )
                },
                label = {
                    Text(
                        text = if (isUrdu) mod.titleUr else mod.titleEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (currentModule == mod) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentModule == mod,
                onClick = { onSelectModule(mod) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("nav_item_${mod.route}")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onNewBusiness,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Business / Switch Mode")
        }

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sidebar_sign_out_btn")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out (${userEmail.substringBefore("@")})")
        }
    }
}

private fun moduleIcon(module: DukaanModule): ImageVector = when (module) {
    DukaanModule.DASHBOARD -> Icons.Default.Dashboard
    DukaanModule.POS -> Icons.Default.PointOfSale
    DukaanModule.PRODUCTS -> Icons.Default.Inventory2
    DukaanModule.CATEGORIES -> Icons.Default.Category
    DukaanModule.PHARMACY_BATCHES -> Icons.Default.LocalPharmacy
    DukaanModule.INVENTORY -> Icons.Default.Inventory
    DukaanModule.PURCHASES -> Icons.Default.ShoppingCart
    DukaanModule.SALES -> Icons.AutoMirrored.Filled.ReceiptLong
    DukaanModule.CUSTOMERS -> Icons.Default.People
    DukaanModule.SUPPLIERS -> Icons.Default.LocalShipping
    DukaanModule.EXPENSES -> Icons.Default.RequestQuote
    DukaanModule.ACCOUNTS -> Icons.Default.AccountBalance
    DukaanModule.EMPLOYEES -> Icons.Default.Badge
    DukaanModule.REPORTS -> Icons.Default.Assessment
    DukaanModule.NOTIFICATIONS -> Icons.Default.Notifications
    DukaanModule.SUBSCRIPTION -> Icons.Default.WorkspacePremium
    DukaanModule.SETTINGS -> Icons.Default.Settings
}
