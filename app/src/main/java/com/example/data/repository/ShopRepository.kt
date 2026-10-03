package com.example.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.data.database.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SqlQueryResult(
    val isSuccess: Boolean,
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val message: String = "",
    val executionTimeMs: Long = 0,
    val rowsAffected: Int = 0
)

data class MySqlConnectionConfig(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "boutique_ecommerce",
    val user: String = "root",
    val password: String = "root123",
    val useSsl: Boolean = false,
    val isConnected: Boolean = true,
    val lastPingMs: Long = 24
)

class ShopRepository(private val database: AppDatabase) {
    val allProducts: Flow<List<Product>> = database.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = database.productDao().getLowStockProducts(5)
    val allCategories: Flow<List<Category>> = database.categoryDao().getAllCategories()
    val allOrders: Flow<List<Order>> = database.orderDao().getAllOrders()
    val allCustomers: Flow<List<Customer>> = database.customerDao().getAllCustomers()
    val totalRevenue: Flow<Double?> = database.orderDao().getTotalRevenue()
    val totalOrdersCount: Flow<Int> = database.orderDao().countOrders()
    val totalProductsCount: Flow<Int> = database.productDao().countProducts()
    val totalCustomersCount: Flow<Int> = database.customerDao().countCustomers()

    fun getOrderItems(orderId: Long): Flow<List<OrderItem>> = database.orderDao().getOrderItems(orderId)

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        database.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        database.productDao().updateProduct(product)
    }

    suspend fun updateStock(productId: Long, newStock: Int) = withContext(Dispatchers.IO) {
        database.productDao().updateStock(productId, newStock)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        database.productDao().deleteProduct(product)
    }

    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        database.customerDao().insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        database.customerDao().updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        database.customerDao().deleteCustomer(customer)
    }

    suspend fun insertOrder(order: Order, items: List<OrderItem>) = withContext(Dispatchers.IO) {
        val orderId = database.orderDao().insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        database.orderDao().insertOrderItems(itemsWithId)
        orderId
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) = withContext(Dispatchers.IO) {
        database.orderDao().updateOrderStatus(orderId, status)
    }

    suspend fun executeRawSql(sql: String): SqlQueryResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val trimmed = sql.trim()
        val isSelect = trimmed.startsWith("SELECT", ignoreCase = true) ||
                       trimmed.startsWith("PRAGMA", ignoreCase = true) ||
                       trimmed.startsWith("EXPLAIN", ignoreCase = true)

        try {
            if (isSelect) {
                val db = database.openHelper.readableDatabase
                val cursor = db.query(SimpleSQLiteQuery(trimmed))
                val columns = mutableListOf<String>()
                for (i in 0 until cursor.columnCount) {
                    columns.add(cursor.getColumnName(i))
                }
                val rows = mutableListOf<List<String>>()
                cursor.use {
                    while (it.moveToNext()) {
                        val row = mutableListOf<String>()
                        for (i in 0 until it.columnCount) {
                            row.add(it.getString(i) ?: "NULL")
                        }
                        rows.add(row)
                    }
                }
                val execTime = System.currentTimeMillis() - startTime
                SqlQueryResult(
                    isSuccess = true,
                    columns = columns,
                    rows = rows,
                    message = "${rows.size} ligne(s) retournée(s)",
                    executionTimeMs = execTime,
                    rowsAffected = rows.size
                )
            } else {
                val db = database.openHelper.writableDatabase
                db.execSQL(trimmed)
                val execTime = System.currentTimeMillis() - startTime
                SqlQueryResult(
                    isSuccess = true,
                    message = "Requête SQL exécutée avec succès.",
                    executionTimeMs = execTime,
                    rowsAffected = 1
                )
            }
        } catch (e: Exception) {
            val execTime = System.currentTimeMillis() - startTime
            SqlQueryResult(
                isSuccess = false,
                message = "Erreur SQL: ${e.localizedMessage}",
                executionTimeMs = execTime
            )
        }
    }

    fun getMySqlSchemaScript(): String {
        return """
-- ========================================================
-- BASE DE DONNÉES MYSQL POUR BOUTIQUE EN LIGNE (ECOMMERCE)
-- Script compatible MySQL 5.7+ / 8.0+ & MariaDB
-- Encodage : UTF-8 (utf8mb4)
-- ========================================================

CREATE DATABASE IF NOT EXISTS boutique_ecommerce
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE boutique_ecommerce;

-- 1. Table des Catégories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'category',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Table des Produits
CREATE TABLE IF NOT EXISTS produits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    promo_price DECIMAL(10, 2) NULL,
    stock INT NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(150),
    sku VARCHAR(60) UNIQUE NOT NULL,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_produit_categorie FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Table des Clients
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    city VARCHAR(100),
    total_orders INT DEFAULT 0,
    total_spent DECIMAL(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Table des Commandes
CREATE TABLE IF NOT EXISTS commandes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_email VARCHAR(150),
    total_amount DECIMAL(12, 2) NOT NULL,
    status ENUM('EN_ATTENTE', 'PAYEE', 'EXPEDIEE', 'LIVREE', 'ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE',
    payment_method VARCHAR(80) DEFAULT 'Carte Bancaire',
    shipping_address TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_commande_client FOREIGN KEY (customer_id) 
        REFERENCES clients(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Table des Lignes de Commande (Items)
CREATE TABLE IF NOT EXISTS lignes_commande (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_ligne_commande FOREIGN KEY (order_id) 
        REFERENCES commandes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_produit FOREIGN KEY (product_id) 
        REFERENCES produits(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Index pour optimiser les performances des requêtes
CREATE INDEX idx_produits_cat ON produits(category_id);
CREATE INDEX idx_produits_stock ON produits(stock);
CREATE INDEX idx_commandes_statut ON commandes(status);
CREATE INDEX idx_commandes_date ON commandes(created_at);

-- Données initiales
INSERT INTO categories (id, name, description) VALUES
(1, 'High-Tech & Informatique', 'Ordinateurs, smartphones et périphériques'),
(2, 'Mode & Vêtements', 'Habits, vestes et chaussures'),
(3, 'Maison & Cuisine', 'Électroménager, déco et mobilier');

INSERT INTO produits (name, description, price, promo_price, stock, category_id, category_name, sku, is_active) VALUES
('PC Portable Pro Ultra 15"', 'Processeur i7, 16Go RAM, 512Go SSD', 1099.00, 999.00, 8, 1, 'High-Tech & Informatique', 'TECH-001', TRUE),
('Smartphone Apex 5G 256Go', 'Écran OLED 120Hz, triple capteur photo', 799.00, 749.00, 14, 1, 'High-Tech & Informatique', 'TECH-002', TRUE),
('Écouteurs Sans Fil Pro ANC', 'Réduction active du bruit, autonomie 32h', 129.50, NULL, 25, 1, 'High-Tech & Informatique', 'TECH-003', TRUE),
('Montre Connectée Sport GPS', 'Cardiofréquencemètre optique, étanche 50m', 189.00, 169.00, 3, 1, 'High-Tech & Informatique', 'ACC-001', TRUE);
        """.trimIndent()
    }
}
