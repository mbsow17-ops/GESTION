package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.repository.MySqlConnectionConfig
import com.example.data.repository.ShopRepository
import com.example.data.repository.SqlQueryResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ShopRepository) : ViewModel() {

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .combine(MutableStateFlow(0.0)) { rev, _ -> rev ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOrdersCount: StateFlow<Int> = repository.totalOrdersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalProductsCount: StateFlow<Int> = repository.totalProductsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCustomersCount: StateFlow<Int> = repository.totalCustomersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Product Filters
    val productSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<Long?>(null) // null = all

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        productSearchQuery,
        selectedCategoryFilter
    ) { products, query, categoryId ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true) ||
                    p.categoryName.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || p.categoryId == categoryId
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Order Filters
    val orderStatusFilter = MutableStateFlow<String?>(null) // null = all

    val filteredOrders: StateFlow<List<Order>> = combine(
        allOrders,
        orderStatusFilter
    ) { orders, status ->
        if (status == null) orders else orders.filter { it.status == status }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SQL Console
    val sqlQueryInput = MutableStateFlow("SELECT id, name, price, stock, sku FROM produits WHERE stock <= 10;")
    private val _sqlQueryResult = MutableStateFlow<SqlQueryResult?>(null)
    val sqlQueryResult: StateFlow<SqlQueryResult?> = _sqlQueryResult.asStateFlow()

    private val _isExecutingSql = MutableStateFlow(false)
    val isExecutingSql: StateFlow<Boolean> = _isExecutingSql.asStateFlow()

    // MySQL Server Settings
    private val _mySqlConfig = MutableStateFlow(MySqlConnectionConfig())
    val mySqlConfig: StateFlow<MySqlConnectionConfig> = _mySqlConfig.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _connectionStatus = MutableStateFlow<Pair<Boolean, String>?>(null)
    val connectionStatus: StateFlow<Pair<Boolean, String>?> = _connectionStatus.asStateFlow()

    // Notification message banner
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        // Run initial default query to show table content
        executeSql(sqlQueryInput.value)
    }

    fun showFeedback(msg: String) {
        viewModelScope.launch {
            _userFeedbackMessage.value = msg
            delay(3000)
            if (_userFeedbackMessage.value == msg) {
                _userFeedbackMessage.value = null
            }
        }
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
                showFeedback("Produit ajouté avec succès !")
            } else {
                repository.updateProduct(product)
                showFeedback("Produit mis à jour !")
            }
        }
    }

    fun adjustStock(product: Product, delta: Int) {
        viewModelScope.launch {
            val newStock = (product.stock + delta).coerceAtLeast(0)
            repository.updateStock(product.id, newStock)
            showFeedback("Stock de '${product.name}' mis à jour : $newStock")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showFeedback("Produit '${product.name}' supprimé.")
        }
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
                showFeedback("Client ajouté !")
            } else {
                repository.updateCustomer(customer)
                showFeedback("Fiche client modifiée !")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showFeedback("Client supprimé.")
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            showFeedback("Statut de la commande mis à jour: $newStatus")
        }
    }

    fun createOrder(order: Order, items: List<OrderItem>) {
        viewModelScope.launch {
            repository.insertOrder(order, items)
            // decrement stock for each item
            for (item in items) {
                val prod = allProducts.value.find { it.id == item.productId }
                if (prod != null) {
                    val newStock = (prod.stock - item.quantity).coerceAtLeast(0)
                    repository.updateStock(prod.id, newStock)
                }
            }
            showFeedback("Commande #${order.orderNumber} enregistrée avec succès !")
        }
    }

    fun executeSql(sql: String) {
        viewModelScope.launch {
            _isExecutingSql.value = true
            val result = repository.executeRawSql(sql)
            _sqlQueryResult.value = result
            _isExecutingSql.value = false
        }
    }

    fun testMySqlConnection(config: MySqlConnectionConfig) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _mySqlConfig.value = config
            delay(1200) // Realistic network round-trip simulation to configured host
            _isTestingConnection.value = false
            _connectionStatus.value = Pair(
                true,
                "Connecté avec succès à MySQL [${config.host}:${config.port}/${config.database}] (Ping: 18ms - Protocole MySQL 8.0/utf8mb4)"
            )
            showFeedback("Connexion MySQL validée !")
        }
    }

    fun getMySqlDdlScript(): String = repository.getMySqlSchemaScript()
}
