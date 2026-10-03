package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.database.AppDatabase
import com.example.data.repository.ShopRepository
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JsfCodeScreen
import com.example.ui.screens.MySqlScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductFormDialog
import com.example.ui.screens.ProductsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ShopViewModel

enum class AppDestination(val label: String) {
    DASHBOARD("Accueil"),
    PRODUCTS("Produits"),
    ORDERS("Commandes"),
    CUSTOMERS("Clients"),
    MYSQL("MySQL"),
    JSF("Code JSF")
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ShopViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = ShopRepository(database)
        viewModel = ShopViewModel(repository)

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ShopViewModel) {
    var currentScreen by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var showDirectAddProductDialog by remember { mutableStateOf(false) }

    val categories by viewModel.allCategories.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Hardware back navigation
    if (currentScreen != AppDestination.DASHBOARD) {
        BackHandler {
            currentScreen = AppDestination.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            AppDestination.DASHBOARD -> "Shop & MySQL Admin"
                            AppDestination.PRODUCTS -> "Catalogue Produits"
                            AppDestination.ORDERS -> "Gestion des Commandes"
                            AppDestination.CUSTOMERS -> "Gestion des Clients"
                            AppDestination.MYSQL -> "Base de Données MySQL"
                            AppDestination.JSF -> "Projet Java Web (JSF)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { currentScreen = AppDestination.CUSTOMERS },
                        modifier = Modifier.testTag("action_nav_customers")
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Clients",
                            tint = if (currentScreen == AppDestination.CUSTOMERS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppDestination.DASHBOARD,
                    onClick = { currentScreen = AppDestination.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Accueil") },
                    label = { Text("Accueil", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
                NavigationBarItem(
                    selected = currentScreen == AppDestination.PRODUCTS,
                    onClick = { currentScreen = AppDestination.PRODUCTS },
                    icon = {
                        if (lowStockProducts.isNotEmpty()) {
                            BadgedBox(badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(lowStockProducts.size.toString())
                                }
                            }) {
                                Icon(Icons.Default.Inventory2, contentDescription = "Produits")
                            }
                        } else {
                            Icon(Icons.Default.Inventory2, contentDescription = "Produits")
                        }
                    },
                    label = { Text("Produits", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_products")
                )
                NavigationBarItem(
                    selected = currentScreen == AppDestination.ORDERS,
                    onClick = { currentScreen = AppDestination.ORDERS },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Commandes") },
                    label = { Text("Commandes", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_orders")
                )
                NavigationBarItem(
                    selected = currentScreen == AppDestination.MYSQL,
                    onClick = { currentScreen = AppDestination.MYSQL },
                    icon = { Icon(Icons.Default.Storage, contentDescription = "MySQL") },
                    label = { Text("MySQL", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_mysql")
                )
                NavigationBarItem(
                    selected = currentScreen == AppDestination.JSF,
                    onClick = { currentScreen = AppDestination.JSF },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Code JSF") },
                    label = { Text("Code JSF", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_jsf")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppDestination.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToProducts = { currentScreen = AppDestination.PRODUCTS },
                    onNavigateToOrders = { currentScreen = AppDestination.ORDERS },
                    onNavigateToMySql = { currentScreen = AppDestination.MYSQL },
                    onNavigateToJsf = { currentScreen = AppDestination.JSF },
                    onOpenAddProductDialog = { showDirectAddProductDialog = true }
                )
                AppDestination.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                AppDestination.ORDERS -> OrdersScreen(viewModel = viewModel)
                AppDestination.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                AppDestination.MYSQL -> MySqlScreen(viewModel = viewModel)
                AppDestination.JSF -> JsfCodeScreen()
            }
        }
    }

    if (showDirectAddProductDialog) {
        ProductFormDialog(
            initialProduct = null,
            categories = categories,
            onDismiss = { showDirectAddProductDialog = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showDirectAddProductDialog = false
            }
        )
    }
}
